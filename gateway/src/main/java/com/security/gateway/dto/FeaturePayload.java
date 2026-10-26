package com.security.gateway.dto;

public class FeaturePayload {
    private int requestLength;
    private int urlLength;
    private int parameterCount;
    private int specialCharCount;
    private int httpMethodCode;
    private int headerCount;
    private int bodyLength;
    private double requestRate1m = 1.0;

    public FeaturePayload() {}

    public FeaturePayload(int requestLength, int urlLength, int parameterCount, int specialCharCount,
                          int httpMethodCode, int headerCount, int bodyLength, double requestRate1m) {
        this.requestLength = requestLength;
        this.urlLength = urlLength;
        this.parameterCount = parameterCount;
        this.specialCharCount = specialCharCount;
        this.httpMethodCode = httpMethodCode;
        this.headerCount = headerCount;
        this.bodyLength = bodyLength;
        this.requestRate1m = requestRate1m;
    }

    public int getRequestLength() { return requestLength; }
    public void setRequestLength(int requestLength) { this.requestLength = requestLength; }

    public int getUrlLength() { return urlLength; }
    public void setUrlLength(int urlLength) { this.urlLength = urlLength; }

    public int getParameterCount() { return parameterCount; }
    public void setParameterCount(int parameterCount) { this.parameterCount = parameterCount; }

    public int getSpecialCharCount() { return specialCharCount; }
    public void setSpecialCharCount(int specialCharCount) { this.specialCharCount = specialCharCount; }

    public int getHttpMethodCode() { return httpMethodCode; }
    public void setHttpMethodCode(int httpMethodCode) { this.httpMethodCode = httpMethodCode; }

    public int getHeaderCount() { return headerCount; }
    public void setHeaderCount(int headerCount) { this.headerCount = headerCount; }

    public int getBodyLength() { return bodyLength; }
    public void setBodyLength(int bodyLength) { this.bodyLength = bodyLength; }

    public double getRequestRate1m() { return requestRate1m; }
    public void setRequestRate1m(double requestRate1m) { this.requestRate1m = requestRate1m; }
}
