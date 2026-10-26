package com.security.gateway.service;

import com.security.gateway.domain.Decision;
import com.security.gateway.domain.DetectorMode;
import com.security.gateway.domain.RequestEvent;
import com.security.gateway.dto.*;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class RequestAnalysisService {

    private final FeatureExtractorService featureExtractor;
    private final RuleEngineService ruleEngine;
    private final MlClientService mlClient;
    private final HybridDecisionEngine hybridEngine;
    private final EventPersistenceService persistenceService;

    public RequestAnalysisService(FeatureExtractorService featureExtractor,
                                  RuleEngineService ruleEngine,
                                  MlClientService mlClient,
                                  HybridDecisionEngine hybridEngine,
                                  EventPersistenceService persistenceService) {
        this.featureExtractor = featureExtractor;
        this.ruleEngine = ruleEngine;
        this.mlClient = mlClient;
        this.hybridEngine = hybridEngine;
        this.persistenceService = persistenceService;
    }

    public AnalyzeResponse analyze(AnalyzeRequest request, double clientRequestRate) {
        long tStart = System.nanoTime();
        String requestId = UUID.randomUUID().toString();
        Instant now = Instant.now();

        // 1. Feature Extraction
        FeaturePayload features = featureExtractor.extract(request, clientRequestRate);

        // 2. Rule-Based Evaluation
        List<MatchedRuleDto> matchedRules = ruleEngine.evaluate(request, clientRequestRate);

        // 3. Machine Learning Inference
        MlPredictionDto mlPrediction = mlClient.predict(features);

        // 4. Hybrid Risk Decision Engine
        HybridDecisionEngine.DecisionResult result = hybridEngine.decide(matchedRules, mlPrediction);

        double totalLatencyMs = (System.nanoTime() - tStart) / 1_000_000.0;

        // 5. Build and Persist Sanitized Security Event
        RequestEvent event = new RequestEvent();
        event.setRequestId(requestId);
        event.setCreatedAt(now);
        event.setMethod(request.getMethod());
        event.setPath(request.getPath());
        event.setQuerySummary(sanitizeQuery(request.getQuery()));
        event.setRequestLength(features.getRequestLength());
        event.setUrlLength(features.getUrlLength());
        event.setParameterCount(features.getParameterCount());
        event.setSpecialCharCount(features.getSpecialCharCount());
        event.setHeaderCount(features.getHeaderCount());
        event.setBodyLength(features.getBodyLength());

        event.setDecision(result.getDecision());
        event.setRiskLevel(result.getRiskLevel());
        event.setRiskScore(result.getRiskScore());
        event.setReasons(result.getReasons());

        List<RequestEvent.MatchedRuleItem> domainRuleItems = new ArrayList<>();
        for (MatchedRuleDto r : matchedRules) {
            domainRuleItems.add(new RequestEvent.MatchedRuleItem(
                    r.getRuleId(), r.getName(), r.getSeverity(), r.isMatched(), r.getReason()));
        }
        event.setMatchedRules(domainRuleItems);

        RequestEvent.MlResultItem mlItem = new RequestEvent.MlResultItem();
        mlItem.setStatus(mlPrediction.getStatus());
        mlItem.setAnomaly(mlPrediction.isAnomaly());
        mlItem.setAnomalyScore(mlPrediction.getAnomalyScore());
        mlItem.setModelVersion(mlPrediction.getModelVersion());
        mlItem.setLatencyMs(mlPrediction.getLatencyMs());
        event.setMl(mlItem);

        event.setDetectorMode(DetectorMode.HYBRID);
        event.setTotalLatencyMs(totalLatencyMs);
        event.setFeatureSchemaVersion("v1");
        event.setCorrelationId(requestId);

        persistenceService.save(event);

        // 6. Return Structured Response
        return new AnalyzeResponse(
                requestId,
                result.getDecision(),
                result.getRiskLevel(),
                result.getRiskScore(),
                result.getReasons(),
                matchedRules,
                mlPrediction,
                Math.round(totalLatencyMs * 100.0) / 100.0,
                now.toString()
        );
    }

    private String sanitizeQuery(String query) {
        if (query == null || query.isBlank()) return "";
        // Redact authorization tokens or passwords if present
        return query.replaceAll("(?i)(password|token|secret|key)=[^&]+", "$1=[REDACTED]");
    }
}
