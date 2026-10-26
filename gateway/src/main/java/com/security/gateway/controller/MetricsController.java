package com.security.gateway.controller;

import com.security.gateway.dto.MetricsSummaryResponse;
import com.security.gateway.service.EventPersistenceService;
import com.security.gateway.service.MlClientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/metrics")
@Tag(name = "Metrics", description = "Operational metrics, volumes, and risk distribution")
public class MetricsController {

    private final EventPersistenceService persistenceService;
    private final MlClientService mlClient;

    public MetricsController(EventPersistenceService persistenceService, MlClientService mlClient) {
        this.persistenceService = persistenceService;
        this.mlClient = mlClient;
    }

    @GetMapping("/summary")
    @Operation(summary = "Get aggregated traffic statistics, decision counts, and time series")
    public ResponseEntity<MetricsSummaryResponse> getSummary() {
        Map<String, Object> mlHealth = mlClient.checkHealth();
        String mlStatus = (String) mlHealth.getOrDefault("status", "UNKNOWN");

        MetricsSummaryResponse summary = persistenceService.getMetricsSummary(mlStatus);
        return ResponseEntity.ok(summary);
    }
}
