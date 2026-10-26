package com.security.gateway.dto;

import com.security.gateway.domain.Decision;
import com.security.gateway.domain.RiskLevel;

import java.util.ArrayList;
import java.util.List;

public class AnalyzeResponse {
    private String requestId;
    private Decision decision;
    private RiskLevel riskLevel;
    private int riskScore;
    private List<String> reasons = new ArrayList<>();
    private List<MatchedRuleDto> matchedRules = new ArrayList<>();
    private MlPredictionDto ml = new MlPredictionDto();
    private double latencyMs;
    private String createdAt;

    public AnalyzeResponse() {}

    public AnalyzeResponse(String requestId, Decision decision, RiskLevel riskLevel, int riskScore,
                           List<String> reasons, List<MatchedRuleDto> matchedRules,
                           MlPredictionDto ml, double latencyMs, String createdAt) {
        this.requestId = requestId;
        this.decision = decision;
        this.riskLevel = riskLevel;
        this.riskScore = riskScore;
        this.reasons = reasons != null ? reasons : new ArrayList<>();
        this.matchedRules = matchedRules != null ? matchedRules : new ArrayList<>();
        this.ml = ml != null ? ml : new MlPredictionDto();
        this.latencyMs = latencyMs;
        this.createdAt = createdAt;
    }

    public String getRequestId() { return requestId; }
    public void setRequestId(String requestId) { this.requestId = requestId; }

    public Decision getDecision() { return decision; }
    public void setDecision(Decision decision) { this.decision = decision; }

    public RiskLevel getRiskLevel() { return riskLevel; }
    public void setRiskLevel(RiskLevel riskLevel) { this.riskLevel = riskLevel; }

    public int getRiskScore() { return riskScore; }
    public void setRiskScore(int riskScore) { this.riskScore = riskScore; }

    public List<String> getReasons() { return reasons; }
    public void setReasons(List<String> reasons) { this.reasons = reasons; }

    public List<MatchedRuleDto> getMatchedRules() { return matchedRules; }
    public void setMatchedRules(List<MatchedRuleDto> matchedRules) { this.matchedRules = matchedRules; }

    public MlPredictionDto getMl() { return ml; }
    public void setMl(MlPredictionDto ml) { this.ml = ml; }

    public double getLatencyMs() { return latencyMs; }
    public void setLatencyMs(double latencyMs) { this.latencyMs = latencyMs; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
