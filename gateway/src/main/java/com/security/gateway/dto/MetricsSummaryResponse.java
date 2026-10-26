package com.security.gateway.dto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MetricsSummaryResponse {
    private long totalRequests;
    private long allowed;
    private long flagged;
    private long blocked;
    private String mlServiceStatus = "OK";
    private Map<String, Long> riskDistribution = new HashMap<>();
    private List<TimeSeriesPoint> timeSeries = new ArrayList<>();

    public static class TimeSeriesPoint {
        private String timestamp;
        private long total;
        private long allowed;
        private long flagged;
        private long blocked;

        public TimeSeriesPoint() {}

        public TimeSeriesPoint(String timestamp, long total, long allowed, long flagged, long blocked) {
            this.timestamp = timestamp;
            this.total = total;
            this.allowed = allowed;
            this.flagged = flagged;
            this.blocked = blocked;
        }

        public String getTimestamp() { return timestamp; }
        public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
        public long getTotal() { return total; }
        public void setTotal(long total) { this.total = total; }
        public long getAllowed() { return allowed; }
        public void setAllowed(long allowed) { this.allowed = allowed; }
        public long getFlagged() { return flagged; }
        public void setFlagged(long flagged) { this.flagged = flagged; }
        public long getBlocked() { return blocked; }
        public void setBlocked(long blocked) { this.blocked = blocked; }
    }

    public MetricsSummaryResponse() {
        riskDistribution.put("LOW", 0L);
        riskDistribution.put("MEDIUM", 0L);
        riskDistribution.put("HIGH", 0L);
    }

    public long getTotalRequests() { return totalRequests; }
    public void setTotalRequests(long totalRequests) { this.totalRequests = totalRequests; }

    public long getAllowed() { return allowed; }
    public void setAllowed(long allowed) { this.allowed = allowed; }

    public long getFlagged() { return flagged; }
    public void setFlagged(long flagged) { this.flagged = flagged; }

    public long getBlocked() { return blocked; }
    public void setBlocked(long blocked) { this.blocked = blocked; }

    public String getMlServiceStatus() { return mlServiceStatus; }
    public void setMlServiceStatus(String mlServiceStatus) { this.mlServiceStatus = mlServiceStatus; }

    public Map<String, Long> getRiskDistribution() { return riskDistribution; }
    public void setRiskDistribution(Map<String, Long> riskDistribution) { this.riskDistribution = riskDistribution; }

    public List<TimeSeriesPoint> getTimeSeries() { return timeSeries; }
    public void setTimeSeries(List<TimeSeriesPoint> timeSeries) { this.timeSeries = timeSeries; }
}
