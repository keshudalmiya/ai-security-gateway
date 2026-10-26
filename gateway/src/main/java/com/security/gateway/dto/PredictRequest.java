package com.security.gateway.dto;

public class PredictRequest {
    private String featureSchemaVersion = "v1";
    private FeaturePayload features;

    public PredictRequest() {}

    public PredictRequest(String featureSchemaVersion, FeaturePayload features) {
        this.featureSchemaVersion = featureSchemaVersion;
        this.features = features;
    }

    public String getFeatureSchemaVersion() { return featureSchemaVersion; }
    public void setFeatureSchemaVersion(String featureSchemaVersion) { this.featureSchemaVersion = featureSchemaVersion; }

    public FeaturePayload getFeatures() { return features; }
    public void setFeatures(FeaturePayload features) { this.features = features; }
}
