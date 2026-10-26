package com.security.gateway.dto;

import java.util.HashMap;
import java.util.Map;

public class SystemHealthResponse {
    private String status = "OK";
    private Map<String, Object> gateway = new HashMap<>();
    private Map<String, Object> mlService = new HashMap<>();
    private Map<String, Object> database = new HashMap<>();
    private Map<String, Object> config = new HashMap<>();
    private String timestamp;

    public SystemHealthResponse() {}

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Map<String, Object> getGateway() { return gateway; }
    public void setGateway(Map<String, Object> gateway) { this.gateway = gateway; }

    public Map<String, Object> getMlService() { return mlService; }
    public void setMlService(Map<String, Object> mlService) { this.mlService = mlService; }

    public Map<String, Object> getDatabase() { return database; }
    public void setDatabase(Map<String, Object> database) { this.database = database; }

    public Map<String, Object> getConfig() { return config; }
    public void setConfig(Map<String, Object> config) { this.config = config; }

    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
}
