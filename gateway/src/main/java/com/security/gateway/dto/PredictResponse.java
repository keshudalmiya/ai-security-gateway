package com.security.gateway.dto;

public class PredictResponse {
    private String status;
    private boolean isAnomaly;
    private double anomalyScore;
    private String modelVersion;
    private double latencyMs;
    private String error;

    public PredictResponse() {}

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

    public String getError() { return error; }
    public void setError(String error) { this.error = error; }
}
