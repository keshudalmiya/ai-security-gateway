package com.security.gateway.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "request_events")
@CompoundIndexes({
    @CompoundIndex(name = "decision_createdAt_idx", def = "{'decision': 1, 'createdAt': -1}"),
    @CompoundIndex(name = "riskLevel_createdAt_idx", def = "{'riskLevel': 1, 'createdAt': -1}")
})
public class RequestEvent {

    @Id
    private String id;

    @Indexed(unique = true)
    private String requestId;

    @Indexed
    private Instant createdAt = Instant.now();

    private String method;
    private String path;
    private String querySummary;
    private int requestLength;
    private int urlLength;
    private int parameterCount;
    private int specialCharCount;
    private int headerCount;
    private int bodyLength;

    private Decision decision;
    private RiskLevel riskLevel;
    private int riskScore;

    private List<String> reasons = new ArrayList<>();
    private List<MatchedRuleItem> matchedRules = new ArrayList<>();
    private MlResultItem ml = new MlResultItem();

    private DetectorMode detectorMode = DetectorMode.HYBRID;
    private double totalLatencyMs;
    private String featureSchemaVersion = "v1";
    private String correlationId;

    public RequestEvent() {}

    // Inner Helper Classes
    public static class MatchedRuleItem {
        private String ruleId;
        private String name;
        private Severity severity;
        private boolean matched;
        private String reason;

        public MatchedRuleItem() {}

        public MatchedRuleItem(String ruleId, String name, Severity severity, boolean matched, String reason) {
            this.ruleId = ruleId;
            this.name = name;
            this.severity = severity;
            this.matched = matched;
            this.reason = reason;
        }

        public String getRuleId() { return ruleId; }
        public void setRuleId(String ruleId) { this.ruleId = ruleId; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public Severity getSeverity() { return severity; }
        public void setSeverity(Severity severity) { this.severity = severity; }
        public boolean isMatched() { return matched; }
        public void setMatched(boolean matched) { this.matched = matched; }
        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
    }

    public static class MlResultItem {
        private String status = "NOT_RUN"; // OK, UNAVAILABLE, ERROR, NOT_RUN
        private boolean isAnomaly;
        private double anomalyScore;
        private String modelVersion = "none";
        private double latencyMs;

        public MlResultItem() {}

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public boolean isAnomaly() { return isAnomaly; }
        public void setAnomaly(boolean anomaly) { isAnomaly = anomaly; }
        public double getAnomalyScore() { return anomalyScore; }
        public void setAnomalyScore(double anomalyScore) { this.anomalyScore = anomalyScore; }
        public String getModelVersion() { return modelVersion; }
        public void setModelVersion(String modelVersion) { this.modelVersion = modelVersion; }
        public double getLatencyMs() { return latencyMs; }
        public void setLatencyMs(double latencyMs) { this.latencyMs = latencyMs; }
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getRequestId() { return requestId; }
    public void setRequestId(String requestId) { this.requestId = requestId; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }
    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }
    public String getQuerySummary() { return querySummary; }
    public void setQuerySummary(String querySummary) { this.querySummary = querySummary; }
    public int getRequestLength() { return requestLength; }
    public void setRequestLength(int requestLength) { this.requestLength = requestLength; }
    public int getUrlLength() { return urlLength; }
    public void setUrlLength(int urlLength) { this.urlLength = urlLength; }
    public int getParameterCount() { return parameterCount; }
    public void setParameterCount(int parameterCount) { this.parameterCount = parameterCount; }
    public int getSpecialCharCount() { return specialCharCount; }
    public void setSpecialCharCount(int specialCharCount) { this.specialCharCount = specialCharCount; }
    public int getHeaderCount() { return headerCount; }
    public void setHeaderCount(int headerCount) { this.headerCount = headerCount; }
    public int getBodyLength() { return bodyLength; }
    public void setBodyLength(int bodyLength) { this.bodyLength = bodyLength; }
    public Decision getDecision() { return decision; }
    public void setDecision(Decision decision) { this.decision = decision; }
    public RiskLevel getRiskLevel() { return riskLevel; }
    public void setRiskLevel(RiskLevel riskLevel) { this.riskLevel = riskLevel; }
    public int getRiskScore() { return riskScore; }
    public void setRiskScore(int riskScore) { this.riskScore = riskScore; }
    public List<String> getReasons() { return reasons; }
    public void setReasons(List<String> reasons) { this.reasons = reasons; }
    public List<MatchedRuleItem> getMatchedRules() { return matchedRules; }
    public void setMatchedRules(List<MatchedRuleItem> matchedRules) { this.matchedRules = matchedRules; }
    public MlResultItem getMl() { return ml; }
    public void setMl(MlResultItem ml) { this.ml = ml; }
    public DetectorMode getDetectorMode() { return detectorMode; }
    public void setDetectorMode(DetectorMode detectorMode) { this.detectorMode = detectorMode; }
    public double getTotalLatencyMs() { return totalLatencyMs; }
    public void setTotalLatencyMs(double totalLatencyMs) { this.totalLatencyMs = totalLatencyMs; }
    public String getFeatureSchemaVersion() { return featureSchemaVersion; }
    public void setFeatureSchemaVersion(String featureSchemaVersion) { this.featureSchemaVersion = featureSchemaVersion; }
    public String getCorrelationId() { return correlationId; }
    public void setCorrelationId(String correlationId) { this.correlationId = correlationId; }
}
