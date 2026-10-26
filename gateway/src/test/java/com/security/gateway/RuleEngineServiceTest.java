package com.security.gateway;

import com.security.gateway.config.GatewayProperties;
import com.security.gateway.domain.Severity;
import com.security.gateway.dto.AnalyzeRequest;
import com.security.gateway.dto.MatchedRuleDto;
import com.security.gateway.repository.AuditEventRepository;
import com.security.gateway.repository.SecurityRuleRepository;
import com.security.gateway.service.RuleEngineService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

public class RuleEngineServiceTest {

    private RuleEngineService ruleService;

    @BeforeEach
    public void setup() {
        GatewayProperties properties = new GatewayProperties();
        SecurityRuleRepository ruleRepo = mock(SecurityRuleRepository.class);
        AuditEventRepository auditRepo = mock(AuditEventRepository.class);

        ruleService = new RuleEngineService(properties, ruleRepo, auditRepo);
        ruleService.initDefaultRules();
    }

    @Test
    public void testBenignRequestProducesNoMatches() {
        AnalyzeRequest request = new AnalyzeRequest("GET", "/api/products", "category=electronics&page=1", Map.of(), "");
        List<MatchedRuleDto> matches = ruleService.evaluate(request, 2.0);
        assertTrue(matches.isEmpty(), "Benign request should not trigger any rule matches");
    }

    @Test
    public void testSqlInjectionDetected() {
        AnalyzeRequest request = new AnalyzeRequest(
                "GET",
                "/api/products",
                "id=1%27%20UNION%20SELECT%20username,%20password%20FROM%20users--",
                Map.of(),
                ""
        );
        List<MatchedRuleDto> matches = ruleService.evaluate(request, 2.0);
        assertFalse(matches.isEmpty());
        assertTrue(matches.stream().anyMatch(m -> m.getRuleId().equals("SQLI-001")));
        assertTrue(matches.stream().anyMatch(m -> m.getSeverity() == Severity.CRITICAL));
    }

    @Test
    public void testXssDetected() {
        AnalyzeRequest request = new AnalyzeRequest(
                "POST",
                "/api/comments",
                "",
                Map.of(),
                "{\"comment\": \"<script>alert('XSS Attack')</script>\"}"
        );
        List<MatchedRuleDto> matches = ruleService.evaluate(request, 2.0);
        assertFalse(matches.isEmpty());
        assertTrue(matches.stream().anyMatch(m -> m.getRuleId().equals("XSS-001")));
    }

    @Test
    public void testPathTraversalDetected() {
        AnalyzeRequest request = new AnalyzeRequest(
                "GET",
                "/static/view",
                "file=../../../../etc/passwd",
                Map.of(),
                ""
        );
        List<MatchedRuleDto> matches = ruleService.evaluate(request, 2.0);
        assertFalse(matches.isEmpty());
        assertTrue(matches.stream().anyMatch(m -> m.getRuleId().equals("TRAV-001")));
    }

    @Test
    public void testRateLimitDetected() {
        AnalyzeRequest request = new AnalyzeRequest("GET", "/api/status", "", Map.of(), "");
        List<MatchedRuleDto> matches = ruleService.evaluate(request, 150.0); // 150 req/min > 60
        assertFalse(matches.isEmpty());
        assertTrue(matches.stream().anyMatch(m -> m.getRuleId().equals("RATE-001")));
    }
}
