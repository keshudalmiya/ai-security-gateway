package com.security.gateway.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "gateway")
public class GatewayProperties {
    private String mlServiceUrl = "http://127.0.0.1:8000";
    private int mlTimeoutMs = 1500;
    private int riskLowMax = 29;
    private int riskMediumMax = 69;
    private int maxRequestBodyBytes = 65536;
    private int rateLimitPerMinute = 60;
    private String demoUpstreamUrl = "http://localhost:8080/api/v1/demo/upstream";

    public String getMlServiceUrl() { return mlServiceUrl; }
    public void setMlServiceUrl(String mlServiceUrl) { this.mlServiceUrl = mlServiceUrl; }

    public int getMlTimeoutMs() { return mlTimeoutMs; }
    public void setMlTimeoutMs(int mlTimeoutMs) { this.mlTimeoutMs = mlTimeoutMs; }

    public int getRiskLowMax() { return riskLowMax; }
    public void setRiskLowMax(int riskLowMax) { this.riskLowMax = riskLowMax; }

    public int getRiskMediumMax() { return riskMediumMax; }
    public void setRiskMediumMax(int riskMediumMax) { this.riskMediumMax = riskMediumMax; }

    public int getMaxRequestBodyBytes() { return maxRequestBodyBytes; }
    public void setMaxRequestBodyBytes(int maxRequestBodyBytes) { this.maxRequestBodyBytes = maxRequestBodyBytes; }

    public int getRateLimitPerMinute() { return rateLimitPerMinute; }
    public void setRateLimitPerMinute(int rateLimitPerMinute) { this.rateLimitPerMinute = rateLimitPerMinute; }

    public String getDemoUpstreamUrl() { return demoUpstreamUrl; }
    public void setDemoUpstreamUrl(String demoUpstreamUrl) { this.demoUpstreamUrl = demoUpstreamUrl; }
}
