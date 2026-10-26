package com.security.gateway.controller;

import com.security.gateway.domain.EvaluationRun;
import com.security.gateway.service.EvaluationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/evaluation")
@Tag(name = "Evaluation", description = "Multi-detector research evaluation benchmark metrics")
public class EvaluationController {

    private final EvaluationService evaluationService;

    public EvaluationController(EvaluationService evaluationService) {
        this.evaluationService = evaluationService;
    }

    @GetMapping("/latest")
    @Operation(summary = "Get the latest reproducible evaluation metrics comparing Rule, ML, and Hybrid")
    public ResponseEntity<?> getLatestEvaluation() {
        Optional<EvaluationRun> latest = evaluationService.getLatestEvaluation();
        if (latest.isPresent()) {
            return ResponseEntity.ok(latest.get());
        } else {
            return ResponseEntity.ok(Map.of(
                    "status", "NOT_EVALUATED",
                    "message", "No evaluation results available yet. Run ml/scripts/evaluate.py to compute reproducible benchmarks."
            ));
        }
    }
}
