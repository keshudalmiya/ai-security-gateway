package com.security.gateway.controller;

import com.security.gateway.domain.SecurityRule;
import com.security.gateway.dto.RuleUpdateRequest;
import com.security.gateway.service.RuleEngineService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/rules")
@Tag(name = "Rules", description = "Security rule inspection and configuration")
public class RulesController {

    private final RuleEngineService ruleService;

    public RulesController(RuleEngineService ruleService) {
        this.ruleService = ruleService;
    }

    @GetMapping
    @Operation(summary = "List all active detection rules, categories, and severities")
    public ResponseEntity<List<SecurityRule>> getRules() {
        return ResponseEntity.ok(ruleService.getAllRules());
    }

    @PatchMapping("/{ruleId}")
    @Operation(summary = "Enable or disable a specific detection rule")
    public ResponseEntity<SecurityRule> updateRule(
            @PathVariable("ruleId") String ruleId,
            @RequestBody RuleUpdateRequest updateRequest,
            @RequestHeader(name = "X-Admin-Actor", defaultValue = "security-admin") String actor) {
        return ruleService.toggleRule(ruleId, updateRequest.isEnabled(), actor)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
