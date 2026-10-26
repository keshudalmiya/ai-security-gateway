package com.security.gateway.controller;

import com.security.gateway.domain.Decision;
import com.security.gateway.domain.RequestEvent;
import com.security.gateway.domain.RiskLevel;
import com.security.gateway.service.EventPersistenceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/requests")
@Tag(name = "Requests", description = "Query and inspect persisted security events")
public class RequestsController {

    private final EventPersistenceService persistenceService;

    public RequestsController(EventPersistenceService persistenceService) {
        this.persistenceService = persistenceService;
    }

    @GetMapping
    @Operation(summary = "Search and filter analyzed request events with pagination")
    public ResponseEntity<Page<RequestEvent>> getRequests(
            @RequestParam(name = "decision", required = false) Decision decision,
            @RequestParam(name = "riskLevel", required = false) RiskLevel riskLevel,
            @RequestParam(name = "method", required = false) String method,
            @RequestParam(name = "query", required = false) String query,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size) {

        int boundedSize = Math.max(1, Math.min(size, 100));
        Pageable pageable = PageRequest.of(Math.max(0, page), boundedSize);

        Page<RequestEvent> results = persistenceService.findFiltered(decision, riskLevel, method, query, pageable);
        return ResponseEntity.ok(results);
    }

    @GetMapping("/{requestId}")
    @Operation(summary = "Get detailed analysis report and evidence for a specific request ID")
    public ResponseEntity<RequestEvent> getRequestDetail(@PathVariable("requestId") String requestId) {
        return persistenceService.findByRequestId(requestId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
