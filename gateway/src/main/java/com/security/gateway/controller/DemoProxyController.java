package com.security.gateway.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/demo/upstream")
@Tag(name = "Demo Upstream", description = "Mock protected backend API service for demo proxy forwarding")
public class DemoProxyController {

    @RequestMapping(value = "/**")
    @Operation(summary = "Protected demo upstream mock endpoint")
    public ResponseEntity<Map<String, Object>> mockUpstream() {
        return ResponseEntity.ok(Map.of(
                "status", "SUCCESS",
                "message", "Request reached protected upstream application safely.",
                "timestamp", Instant.now().toString(),
                "data", Map.of(
                        "system", "Protected Demo E-Commerce API",
                        "items", List.of(
                                Map.of("id", 101, "name", "Enterprise Cloud Security Subscription", "price", 499.00),
                                Map.of("id", 102, "name", "AI WAF Gateway Appliance", "price", 1250.00),
                                Map.of("id", 103, "name", "Threat Intelligence Feed License", "price", 299.00)
                        )
                )
        ));
    }
}
