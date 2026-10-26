package com.security.gateway.service;

import com.security.gateway.config.GatewayProperties;
import com.security.gateway.domain.Decision;
import com.security.gateway.domain.RiskLevel;
import com.security.gateway.domain.Severity;
import com.security.gateway.dto.MatchedRuleDto;
import com.security.gateway.dto.MlPredictionDto;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class HybridDecisionEngine {

    private final GatewayProperties properties;

    public HybridDecisionEngine(GatewayProperties properties) {
        this.properties = properties;
    }

    public static class DecisionResult {
        private final Decision decision;
        private final RiskLevel riskLevel;
        private final int riskScore;
        private final List<String> reasons;

        public DecisionResult(Decision decision, RiskLevel riskLevel, int riskScore, List<String> reasons) {
            this.decision = decision;
            this.riskLevel = riskLevel;
            this.riskScore = riskScore;
            this.reasons = reasons;
        }

        public Decision getDecision() { return decision; }
        public RiskLevel getRiskLevel() { return riskLevel; }
        public int getRiskScore() { return riskScore; }
        public List<String> getReasons() { return reasons; }
    }

    public DecisionResult decide(List<MatchedRuleDto> matchedRules, MlPredictionDto mlResult) {
        List<String> reasons = new ArrayList<>();

        // Check for CRITICAL rule override
        boolean hasCritical = false;
        int maxRuleScore = 0;

        for (MatchedRuleDto rule : matchedRules) {
            reasons.add(String.format("Rule matched: [%s] %s (%s)", rule.getRuleId(), rule.getName(), rule.getSeverity()));
            if (rule.getSeverity() == Severity.CRITICAL) {
                hasCritical = true;
                maxRuleScore = 100;
            } else if (rule.getSeverity() == Severity.HIGH) {
                maxRuleScore = Math.max(maxRuleScore, 80);
            } else if (rule.getSeverity() == Severity.MEDIUM) {
                maxRuleScore = Math.max(maxRuleScore, 50);
            } else if (rule.getSeverity() == Severity.LOW) {
                maxRuleScore = Math.max(maxRuleScore, 20);
            }
        }

        if (hasCritical) {
            reasons.add("Decision policy: Critical security rule match enforces mandatory BLOCK.");
            return new DecisionResult(Decision.BLOCK, RiskLevel.HIGH, 100, reasons);
        }

        // Handle ML status & Anomaly score
        boolean mlAvailable = "OK".equalsIgnoreCase(mlResult.getStatus());
        double anomalyScore = mlResult.getAnomalyScore();

        if (mlAvailable) {
            if (mlResult.isAnomaly()) {
                reasons.add(String.format("Isolation Forest anomaly detected (score: %.2f, model: %s).",
                        anomalyScore, mlResult.getModelVersion()));
            } else {
                reasons.add(String.format("Isolation Forest evaluated request as normal (score: %.2f).", anomalyScore));
            }
        } else {
            reasons.add(String.format("ML Service status [%s]: Applied fail-safe inspection fallback.", mlResult.getStatus()));
        }

        // Compute Hybrid Risk Score
        int combinedScore;
        if (mlAvailable) {
            // Weighted combination: 60% rule indicators, 40% ML anomaly score
            double weightedRule = maxRuleScore * 0.60;
            double weightedMl = (anomalyScore * 100.0) * 0.40;
            combinedScore = Math.min(100, (int) Math.round(weightedRule + weightedMl));
        } else {
            if (maxRuleScore > 0) {
                combinedScore = maxRuleScore;
            } else {
                // Fail-safe default policy for uncertain requests when ML is unavailable: Flag with medium risk
                combinedScore = 45;
                reasons.add("Fail-safe policy: Flagged for administrator review due to ML unavailability.");
            }
        }

        // Apply Policy Thresholds
        int lowMax = properties.getRiskLowMax();
        int mediumMax = properties.getRiskMediumMax();

        Decision decision;
        RiskLevel riskLevel;

        if (combinedScore <= lowMax) {
            decision = Decision.ALLOW;
            riskLevel = RiskLevel.LOW;
            reasons.add(String.format("Risk score %d is within Low risk threshold (0 - %d). Allowed.", combinedScore, lowMax));
        } else if (combinedScore <= mediumMax) {
            decision = Decision.FLAG;
            riskLevel = RiskLevel.MEDIUM;
            reasons.add(String.format("Risk score %d is within Medium risk threshold (%d - %d). Flagged for review.",
                    combinedScore, lowMax + 1, mediumMax));
        } else {
            decision = Decision.BLOCK;
            riskLevel = RiskLevel.HIGH;
            reasons.add(String.format("Risk score %d exceeds High risk threshold (> %d). Blocked by policy.",
                    combinedScore, mediumMax));
        }

        return new DecisionResult(decision, riskLevel, combinedScore, reasons);
    }
}
