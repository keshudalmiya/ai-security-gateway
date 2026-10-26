package com.security.gateway.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.security.gateway.domain.EvaluationRun;
import com.security.gateway.repository.EvaluationRunRepository;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.*;

@Service
public class EvaluationService {

    private static final Logger logger = LoggerFactory.getLogger(EvaluationService.class);

    private final EvaluationRunRepository evaluationRepository;
    private final ObjectMapper objectMapper;

    private EvaluationRun cachedEvaluation;

    public EvaluationService(EvaluationRunRepository evaluationRepository, ObjectMapper objectMapper) {
        this.evaluationRepository = evaluationRepository;
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    public void loadInitialEvaluation() {
        // 1. Try loading from MongoDB
        try {
            Optional<EvaluationRun> dbRun = evaluationRepository.findTopByOrderByCreatedAtDesc();
            if (dbRun.isPresent()) {
                this.cachedEvaluation = dbRun.get();
                logger.info("Loaded latest evaluation run from MongoDB: {}", cachedEvaluation.getRunId());
                return;
            }
        } catch (Exception e) {
            logger.warn("Could not query evaluation_runs from MongoDB: {}", e.getMessage());
        }

        // 2. Try loading from ml/reports/evaluation_results.json or classpath
        Path reportPath = Paths.get("..", "ml", "reports", "evaluation_results.json").toAbsolutePath().normalize();
        if (!Files.exists(reportPath)) {
            reportPath = Paths.get("ml", "reports", "evaluation_results.json").toAbsolutePath().normalize();
        }

        String json = null;
        if (Files.exists(reportPath)) {
            try {
                json = Files.readString(reportPath);
                logger.info("Loaded real evaluation metrics from filesystem: {}", reportPath);
            } catch (Exception e) {
                logger.warn("Failed to read from filesystem: {}", e.getMessage());
            }
        }

        if (json == null) {
            try (InputStream is = getClass().getClassLoader().getResourceAsStream("evaluation_results.json")) {
                if (is != null) {
                    json = new String(is.readAllBytes());
                    logger.info("Loaded real evaluation metrics from classpath: evaluation_results.json");
                }
            } catch (Exception e) {
                logger.warn("Failed to read evaluation_results.json from classpath: {}", e.getMessage());
            }
        }

        if (json != null) {
            try {
                @SuppressWarnings("unchecked")
                Map<String, Object> map = objectMapper.readValue(json, Map.class);

                EvaluationRun run = new EvaluationRun();
                run.setRunId((String) map.getOrDefault("runId", "eval-" + System.currentTimeMillis()));
                run.setCreatedAt(Instant.now());
                run.setDatasetName((String) map.getOrDefault("datasetName", "CSIC 2010 HTTP Dataset"));
                run.setDatasetVersion((String) map.getOrDefault("datasetVersion", "v1"));
                run.setSplitStrategy((String) map.getOrDefault("splitStrategy", "held-out-test-set"));
                run.setSeed(((Number) map.getOrDefault("seed", 42)).intValue());
                run.setTestSampleCount(((Number) map.getOrDefault("testSampleCount", 3000)).intValue());
                run.setNormalSampleCount(((Number) map.getOrDefault("normalSampleCount", 1500)).intValue());
                run.setAnomalousSampleCount(((Number) map.getOrDefault("anomalousSampleCount", 1500)).intValue());
                run.setModelVersion((String) map.getOrDefault("modelVersion", "iforest-csic-v1"));

                @SuppressWarnings("unchecked")
                Map<String, Integer> labelMap = (Map<String, Integer>) map.get("labelMapping");
                run.setLabelMapping(labelMap);

                @SuppressWarnings("unchecked")
                Map<String, Object> thresholdCfg = (Map<String, Object>) map.get("thresholdConfig");
                run.setThresholdConfig(thresholdCfg);

                @SuppressWarnings("unchecked")
                Map<String, Object> metrics = (Map<String, Object>) map.get("metrics");
                run.setMetrics(metrics);

                @SuppressWarnings("unchecked")
                List<String> notes = (List<String>) map.get("notes");
                run.setNotes(notes);

                this.cachedEvaluation = run;

                // Attempt to persist to MongoDB
                try {
                    evaluationRepository.save(run);
                } catch (Exception ignored) {}

            } catch (Exception e) {
                logger.error("Failed to parse evaluation_results.json: {}", e.getMessage());
            }
        } else {
            logger.info("No offline evaluation results report found on filesystem or classpath.");
        }
    }

    public Optional<EvaluationRun> getLatestEvaluation() {
        if (cachedEvaluation != null) {
            return Optional.of(cachedEvaluation);
        }
        try {
            return evaluationRepository.findTopByOrderByCreatedAtDesc();
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}
