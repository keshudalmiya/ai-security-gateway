package com.security.gateway.service;

import com.security.gateway.config.GatewayProperties;
import com.security.gateway.dto.FeaturePayload;
import com.security.gateway.dto.MlPredictionDto;
import com.security.gateway.dto.PredictRequest;
import com.security.gateway.dto.PredictResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Service
public class MlClientService {

    private static final Logger logger = LoggerFactory.getLogger(MlClientService.class);

    private final GatewayProperties properties;
    private final RestTemplate restTemplate;

    public MlClientService(GatewayProperties properties, RestTemplateBuilder builder) {
        this.properties = properties;
        this.restTemplate = builder
                .setConnectTimeout(Duration.ofMillis(properties.getMlTimeoutMs()))
                .setReadTimeout(Duration.ofMillis(properties.getMlTimeoutMs()))
                .build();
    }

    public MlPredictionDto predict(FeaturePayload features) {
        String url = properties.getMlServiceUrl() + "/predict";
        PredictRequest request = new PredictRequest("v1", features);

        long t0 = System.nanoTime();
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<PredictRequest> entity = new HttpEntity<>(request, headers);

            ResponseEntity<PredictResponse> response = restTemplate.postForEntity(url, entity, PredictResponse.class);

            double localLatencyMs = (System.nanoTime() - t0) / 1_000_000.0;

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                PredictResponse body = response.getBody();
                return new MlPredictionDto(
                        body.getStatus() != null ? body.getStatus() : "OK",
                        body.isAnomaly(),
                        body.getAnomalyScore(),
                        body.getModelVersion() != null ? body.getModelVersion() : "iforest-csic-v1",
                        body.getLatencyMs() > 0 ? body.getLatencyMs() : localLatencyMs
                );
            } else {
                logger.warn("ML Service returned non-2xx status: {}", response.getStatusCode());
                return new MlPredictionDto("ERROR", false, 0.50, "uncalibrated-fallback", localLatencyMs);
            }
        } catch (ResourceAccessException e) {
            double localLatencyMs = (System.nanoTime() - t0) / 1_000_000.0;
            logger.warn("ML Service connection timeout or unavailable at {}: {}", url, e.getMessage());
            return new MlPredictionDto("UNAVAILABLE", false, 0.50, "none", localLatencyMs);
        } catch (Exception e) {
            double localLatencyMs = (System.nanoTime() - t0) / 1_000_000.0;
            logger.error("Error communicating with ML service: {}", e.getMessage());
            return new MlPredictionDto("ERROR", false, 0.50, "none", localLatencyMs);
        }
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> checkHealth() {
        String url = properties.getMlServiceUrl() + "/health";
        Map<String, Object> status = new HashMap<>();
        try {
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return (Map<String, Object>) response.getBody();
            } else {
                status.put("status", "DEGRADED");
                status.put("service", "ml-service");
                status.put("modelLoaded", false);
                status.put("error", "Returned HTTP " + response.getStatusCode());
            }
        } catch (Exception e) {
            status.put("status", "UNAVAILABLE");
            status.put("service", "ml-service");
            status.put("modelLoaded", false);
            status.put("error", e.getMessage());
        }
        return status;
    }
}
