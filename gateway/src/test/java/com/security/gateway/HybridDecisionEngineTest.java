package com.security.gateway;

import com.security.gateway.config.GatewayProperties;
import com.security.gateway.domain.Decision;
import com.security.gateway.domain.RiskLevel;
import com.security.gateway.domain.Severity;
import com.security.gateway.dto.MatchedRuleDto;
import com.security.gateway.dto.MlPredictionDto;
import com.security.gateway.service.HybridDecisionEngine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class HybridDecisionEngineTest {

    private HybridDecisionEngine engine;
    private GatewayProperties properties;

    @BeforeEach
    public void setup() {
        properties = new GatewayProperties();
        properties.setRiskLowMax(29);
        properties.setRiskMediumMax(69);
        engine = new HybridDecisionEngine(properties);
    }

    @Test
    public void testBenignRequestAllowed() {
        // No rules matched, ML anomaly score is low (0.05)
        MlPredictionDto ml = new MlPredictionDto("OK", false, 0.05, "iforest-csic-v1", 2.0);
        HybridDecisionEngine.DecisionResult res = engine.decide(Collections.emptyList(), ml);

        assertEquals(Decision.ALLOW, res.getDecision());
        assertEquals(RiskLevel.LOW, res.getRiskLevel());
        assertTrue(res.getRiskScore() <= 29);
    }

    @Test
    public void testCriticalRuleEnforcesMandatoryBlock() {
        // Critical SQLi rule matched
        MatchedRuleDto criticalRule = new MatchedRuleDto("SQLI-001", "SQL Injection", Severity.CRITICAL, true, "Critical match");
        MlPredictionDto ml = new MlPredictionDto("OK", false, 0.10, "iforest-csic-v1", 2.0);

        HybridDecisionEngine.DecisionResult res = engine.decide(List.of(criticalRule), ml);

        assertEquals(Decision.BLOCK, res.getDecision(), "Critical rule MUST force BLOCK decision");
        assertEquals(RiskLevel.HIGH, res.getRiskLevel());
        assertEquals(100, res.getRiskScore());
        assertTrue(res.getReasons().stream().anyMatch(r -> r.contains("Critical security rule match enforces mandatory BLOCK")));
    }

    @Test
    public void testMlAnomalyFlagsSuspiciousRequest() {
        // No explicit rule match, but ML detected unusual structure (anomaly score 0.85)
        MlPredictionDto ml = new MlPredictionDto("OK", true, 0.85, "iforest-csic-v1", 3.5);

        HybridDecisionEngine.DecisionResult res = engine.decide(Collections.emptyList(), ml);

        // 0.85 * 100 * 0.40 = 34 -> within 30-69 -> FLAG
        assertEquals(Decision.FLAG, res.getDecision());
        assertEquals(RiskLevel.MEDIUM, res.getRiskLevel());
        assertTrue(res.getRiskScore() >= 30 && res.getRiskScore() <= 69);
    }

    @Test
    public void testMlServiceUnavailableFollowsFailSafePolicy() {
        // ML service unavailable, no rules matched -> fail-safe policy FLAG
        MlPredictionDto ml = new MlPredictionDto("UNAVAILABLE", false, 0.50, "none", 0.0);

        HybridDecisionEngine.DecisionResult res = engine.decide(Collections.emptyList(), ml);

        assertEquals(Decision.FLAG, res.getDecision(), "ML outage with uninspected request must follow fail-safe FLAG policy");
        assertEquals(RiskLevel.MEDIUM, res.getRiskLevel());
        assertTrue(res.getReasons().stream().anyMatch(r -> r.contains("fail-safe")));
    }

    @Test
    public void testHighSeverityRuleWithNormalMlResultsInFlag() {
        // High-severity rule (such as Path Traversal TRAV-001) matched on a structurally normal request (ML score 0.40)
        MatchedRuleDto travRule = new MatchedRuleDto("TRAV-001", "Path Traversal Indicator", Severity.HIGH, true, "Path traversal sequence detected");
        MlPredictionDto ml = new MlPredictionDto("OK", false, 0.40, "iforest-csic-v1", 2.5);

        HybridDecisionEngine.DecisionResult res = engine.decide(List.of(travRule), ml);

        // Calculation: (80 * 0.60) + (40 * 0.40) = 48 + 16 = 64
        assertEquals(64, res.getRiskScore(), "HIGH rule (80) with ML score 0.40 must compute to risk score 64");
        assertEquals(Decision.FLAG, res.getDecision(), "Risk score 64 must be categorized as FLAG (threshold 30-69)");
        assertEquals(RiskLevel.MEDIUM, res.getRiskLevel());
        assertTrue(res.getReasons().stream().anyMatch(r -> r.contains("TRAV-001")));
    }

    @Test
    public void testHighSeverityRuleWithAnomalousMlResultsInBlock() {
        // High-severity rule (TRAV-001) corroborated by anomalous ML features (score 0.75)
        MatchedRuleDto travRule = new MatchedRuleDto("TRAV-001", "Path Traversal Indicator", Severity.HIGH, true, "Path traversal sequence detected");
        MlPredictionDto ml = new MlPredictionDto("OK", true, 0.75, "iforest-csic-v1", 3.0);

        HybridDecisionEngine.DecisionResult res = engine.decide(List.of(travRule), ml);

        // Calculation: (80 * 0.60) + (75 * 0.40) = 48 + 30 = 78
        assertEquals(78, res.getRiskScore(), "HIGH rule (80) with ML score 0.75 must compute to risk score 78");
        assertEquals(Decision.BLOCK, res.getDecision(), "Risk score 78 exceeds high threshold (> 69) and must BLOCK");
        assertEquals(RiskLevel.HIGH, res.getRiskLevel());
    }
}
