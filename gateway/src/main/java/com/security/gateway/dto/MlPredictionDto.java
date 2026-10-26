package com.security.gateway.dto;

public class MlPredictionDto {
    private String status = "NOT_RUN"; // OK, UNAVAILABLE, ERROR, NOT_RUN
    private boolean isAnomaly;
    private double anomalyScore;
    private String modelVersion = "none";
    private double latencyMs;

    public MlPredictionDto() {}

    public MlPredictionDto(String status, boolean isAnomaly, double anomalyScore, String modelVersion, double latencyMs) {
        this.status = status;
        this.isAnomaly = isAnomaly;
        this.anomalyScore = anomalyScore;
        this.modelVersion = modelVersion;
        this.latencyMs = latencyMs;
    }

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
