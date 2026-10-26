package com.security.gateway.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Document(collection = "evaluation_runs")
public class EvaluationRun {

    @Id
    private String id;

    @Indexed(unique = true)
    private String runId;

    @Indexed
    private Instant createdAt = Instant.now();

    private String datasetName;
    private String datasetVersion;
    private String splitStrategy;
    private int seed;
    private int testSampleCount;
    private int normalSampleCount;
    private int anomalousSampleCount;
    private Map<String, Integer> labelMapping;
    private String modelVersion;
    private Map<String, Object> thresholdConfig;
    private Map<String, Object> metrics;
    private String reportPath;
    private List<String> notes;

    public EvaluationRun() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getRunId() { return runId; }
    public void setRunId(String runId) { this.runId = runId; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public String getDatasetName() { return datasetName; }
    public void setDatasetName(String datasetName) { this.datasetName = datasetName; }
    public String getDatasetVersion() { return datasetVersion; }
    public void setDatasetVersion(String datasetVersion) { this.datasetVersion = datasetVersion; }
    public String getSplitStrategy() { return splitStrategy; }
    public void setSplitStrategy(String splitStrategy) { this.splitStrategy = splitStrategy; }
    public int getSeed() { return seed; }
    public void setSeed(int seed) { this.seed = seed; }
    public int getTestSampleCount() { return testSampleCount; }
    public void setTestSampleCount(int testSampleCount) { this.testSampleCount = testSampleCount; }
    public int getNormalSampleCount() { return normalSampleCount; }
    public void setNormalSampleCount(int normalSampleCount) { this.normalSampleCount = normalSampleCount; }
    public int getAnomalousSampleCount() { return anomalousSampleCount; }
    public void setAnomalousSampleCount(int anomalousSampleCount) { this.anomalousSampleCount = anomalousSampleCount; }
    public Map<String, Integer> getLabelMapping() { return labelMapping; }
    public void setLabelMapping(Map<String, Integer> labelMapping) { this.labelMapping = labelMapping; }
    public String getModelVersion() { return modelVersion; }
    public void setModelVersion(String modelVersion) { this.modelVersion = modelVersion; }
    public Map<String, Object> getThresholdConfig() { return thresholdConfig; }
    public void setThresholdConfig(Map<String, Object> thresholdConfig) { this.thresholdConfig = thresholdConfig; }
    public Map<String, Object> getMetrics() { return metrics; }
    public void setMetrics(Map<String, Object> metrics) { this.metrics = metrics; }
    public String getReportPath() { return reportPath; }
    public void setReportPath(String reportPath) { this.reportPath = reportPath; }
    public List<String> getNotes() { return notes; }
    public void setNotes(List<String> notes) { this.notes = notes; }
}
