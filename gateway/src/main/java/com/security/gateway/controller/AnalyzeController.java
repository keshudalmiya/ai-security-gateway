package com.security.gateway.controller;

import com.security.gateway.dto.AnalyzeRequest;
import com.security.gateway.dto.AnalyzeResponse;
import com.security.gateway.service.RequestAnalysisService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/analyze")
@Tag(name = "Analysis", description = "HTTP request inspection and hybrid risk analysis")
public class AnalyzeController {

    private final RequestAnalysisService analysisService;

    public AnalyzeController(RequestAnalysisService analysisService) {
        this.analysisService = analysisService;
    }

    @PostMapping
    @Operation(summary = "Inspect and classify an HTTP request using Rule-Based and ML detectors")
    public ResponseEntity<AnalyzeResponse> analyzeRequest(
            @Valid @RequestBody AnalyzeRequest request,
            @RequestParam(name = "rate", defaultValue = "1.0") double clientRate) {
        AnalyzeResponse response = analysisService.analyze(request, clientRate);
        return ResponseEntity.ok(response);
    }
}
