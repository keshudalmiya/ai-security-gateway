package com.security.gateway.controller;

import com.security.gateway.config.GatewayProperties;
import com.security.gateway.dto.SystemHealthResponse;
import com.security.gateway.service.EventPersistenceService;
import com.security.gateway.service.MlClientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.management.ManagementFactory;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/health")
@Tag(name = "Health", description = "System health and dependency diagnostics")
public class HealthController {

    private final GatewayProperties properties;
    private final MlClientService mlClient;
    private final EventPersistenceService persistenceService;

    public HealthController(GatewayProperties properties,
                            MlClientService mlClient,
                            EventPersistenceService persistenceService) {
        this.properties = properties;
        this.mlClient = mlClient;
        this.persistenceService = persistenceService;
    }

    @GetMapping
    @Operation(summary = "Get system health status for gateway, ML service, and database")
    public ResponseEntity<SystemHealthResponse> getHealth() {
        SystemHealthResponse resp = new SystemHealthResponse();
        resp.setTimestamp(Instant.now().toString());

        // 1. Gateway Status
        Map<String, Object> gw = new HashMap<>();
        gw.put("status", "UP");
        gw.put("service", "spring-boot-security-gateway");
        gw.put("version", "1.0.0");
        long uptimeMs = ManagementFactory.getRuntimeMXBean().getUptime();
        gw.put("uptimeSeconds", Math.round(uptimeMs / 1000.0));
        resp.setGateway(gw);

        // 2. ML Service Status
        Map<String, Object> mlStatus = mlClient.checkHealth();
        resp.setMlService(mlStatus);

        // 3. Database Status
        Map<String, Object> db = new HashMap<>();
        boolean mongoUp = persistenceService.isMongoOperational();
        db.put("status", mongoUp ? "UP" : "DEGRADED");
        db.put("type", "MongoDB (Atlas/Local)");
        db.put("fallbackActive", !mongoUp);
        resp.setDatabase(db);

        // 4. Configuration summary
        Map<String, Object> cfg = new HashMap<>();
        cfg.put("riskLowMax", properties.getRiskLowMax());
        cfg.put("riskMediumMax", properties.getRiskMediumMax());
        cfg.put("maxRequestBodyBytes", properties.getMaxRequestBodyBytes());
        cfg.put("mlTimeoutMs", properties.getMlTimeoutMs());
        cfg.put("demoUpstreamUrl", properties.getDemoUpstreamUrl());
        resp.setConfig(cfg);

        // Overall status
        boolean mlHealthy = "OK".equalsIgnoreCase((String) mlStatus.get("status"));
        if (mongoUp && mlHealthy) {
            resp.setStatus("HEALTHY");
        } else if (mongoUp || mlHealthy) {
            resp.setStatus("DEGRADED");
        } else {
            resp.setStatus("CRITICAL");
        }

        return ResponseEntity.ok(resp);
    }
}
