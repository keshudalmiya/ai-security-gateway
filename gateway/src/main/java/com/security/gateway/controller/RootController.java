package com.security.gateway.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@Tag(name = "Root", description = "Gateway root service information and entrypoint")
public class RootController {

    @GetMapping("/")
    @Operation(summary = "Gateway service metadata and API directory")
    public ResponseEntity<Map<String, Object>> getRootInfo() {
        Map<String, Object> info = new LinkedHashMap<>();
        info.put("service", "ai-security-gateway");
        info.put("status", "UP");
        info.put("version", "1.0.0");
        info.put("description", "A Lightweight Adaptive Hybrid Framework for Web API Attack Detection Using Rule-Based and Machine Learning Techniques");
        info.put("docs", "/api/v1/swagger-ui.html");
        info.put("apiPrefix", "/api/v1");

        Map<String, String> endpoints = new LinkedHashMap<>();
        endpoints.put("health", "/api/v1/health");
        endpoints.put("analyze", "/api/v1/analyze");
        endpoints.put("requests", "/api/v1/requests");
        endpoints.put("metrics", "/api/v1/metrics/summary");
        endpoints.put("rules", "/api/v1/rules");
        endpoints.put("evaluation", "/api/v1/evaluation/latest");
        endpoints.put("demoUpstream", "/api/v1/demo/upstream");
        endpoints.put("swaggerUi", "/api/v1/swagger-ui.html");
        endpoints.put("apiDocs", "/api/v1/v3/api-docs");
        info.put("endpoints", endpoints);

        return ResponseEntity.ok(info);
    }

    @GetMapping("/favicon.ico")
    @Operation(summary = "Favicon endpoint returning 204 No Content")
    public ResponseEntity<Void> favicon() {
        return ResponseEntity.noContent().build();
    }
}
