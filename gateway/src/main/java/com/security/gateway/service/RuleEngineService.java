package com.security.gateway.service;

import com.security.gateway.config.GatewayProperties;
import com.security.gateway.domain.AuditEvent;
import com.security.gateway.domain.RuleCategory;
import com.security.gateway.domain.SecurityRule;
import com.security.gateway.domain.Severity;
import com.security.gateway.dto.AnalyzeRequest;
import com.security.gateway.dto.MatchedRuleDto;
import com.security.gateway.repository.AuditEventRepository;
import com.security.gateway.repository.SecurityRuleRepository;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

@Service
public class RuleEngineService {

    private static final Logger logger = LoggerFactory.getLogger(RuleEngineService.class);

    private final GatewayProperties properties;
    private final SecurityRuleRepository ruleRepository;
    private final AuditEventRepository auditRepository;

    // In-memory cache for ultra-fast rule inspection and offline fallback
    private final Map<String, SecurityRule> activeRules = new ConcurrentHashMap<>();

    // Compiled Regex Patterns
    private static final Pattern SQLI_PATTERN_1 = Pattern.compile("(?i)(\\b(union|select|insert|update|delete|drop|alter|truncate)\\b.*\\b(from|into|table|database)\\b)");
    private static final Pattern SQLI_PATTERN_2 = Pattern.compile("(?i)('|\"|\\b)(or|and)\\b\\s+['\"\\d\\w]+\\s*=\\s*['\"\\d\\w]+");
    private static final Pattern SQLI_PATTERN_3 = Pattern.compile("(?i)(--|/\\*|\\*/|#|;\\s*drop\\b|;\\s*select\\b)");
    private static final Pattern SQLI_PATTERN_4 = Pattern.compile("(?i)(\\bwaitfor\\s+delay\\b|\\bxp_cmdshell\\b|\\bbenchmark\\s*\\()");

    private static final Pattern XSS_PATTERN_1 = Pattern.compile("(?i)(<script\\b[^>]*>.*?</script>)");
    private static final Pattern XSS_PATTERN_2 = Pattern.compile("(?i)(<[^>]+(onload|onerror|onclick|onmouseover|onfocus)\\s*=)");
    private static final Pattern XSS_PATTERN_3 = Pattern.compile("(?i)(javascript\\s*:\\s*[^\\s]+)");
    private static final Pattern XSS_PATTERN_4 = Pattern.compile("(?i)(alert\\s*\\(|confirm\\s*\\(|prompt\\s*\\()");

    private static final Pattern TRAV_PATTERN_1 = Pattern.compile("(?i)(\\.\\./|\\.\\.\\\\|\\.\\.%2f|\\.\\.%5c)");
    private static final Pattern TRAV_PATTERN_2 = Pattern.compile("(?i)(/etc/(passwd|shadow|group|hosts))");
    private static final Pattern TRAV_PATTERN_3 = Pattern.compile("(?i)(\\b(win\\.ini|boot\\.ini|system32)\\b)");

    public RuleEngineService(GatewayProperties properties,
                             SecurityRuleRepository ruleRepository,
                             AuditEventRepository auditRepository) {
        this.properties = properties;
        this.ruleRepository = ruleRepository;
        this.auditRepository = auditRepository;
    }

    @PostConstruct
    public void initDefaultRules() {
        List<SecurityRule> defaults = List.of(
                new SecurityRule("SQLI-001", "SQL Injection Indicator", RuleCategory.SQL_INJECTION,
                        "Detects dangerous SQL injection syntax patterns (UNION, DROP, tautologies)", Severity.CRITICAL, true),
                new SecurityRule("XSS-001", "Cross-Site Scripting Indicator", RuleCategory.XSS,
                        "Detects suspicious script tags, javascript: URIs, and event handlers", Severity.HIGH, true),
                new SecurityRule("TRAV-001", "Path Traversal Indicator", RuleCategory.PATH_TRAVERSAL,
                        "Detects directory traversal sequences and sensitive system file references", Severity.HIGH, true),
                new SecurityRule("REQ-SIZE-001", "Oversized Payload Indicator", RuleCategory.REQUEST_VALIDATION,
                        "Detects payloads exceeding configured maximum size bounds", Severity.MEDIUM, true),
                new SecurityRule("RATE-001", "Excessive Request Rate Indicator", RuleCategory.RATE_LIMIT,
                        "Detects client request rates exceeding allowed requests-per-minute threshold", Severity.MEDIUM, true),
                new SecurityRule("METHOD-001", "Suspicious HTTP Method Indicator", RuleCategory.REQUEST_VALIDATION,
                        "Detects unsupported or dangerous HTTP methods outside standard REST specification", Severity.HIGH, true)
        );

        for (SecurityRule r : defaults) {
            activeRules.put(r.getRuleId(), r);
        }

        // Try syncing with MongoDB if reachable
        try {
            for (SecurityRule r : defaults) {
                if (ruleRepository.findByRuleId(r.getRuleId()).isEmpty()) {
                    ruleRepository.save(r);
                }
            }
            List<SecurityRule> fromDb = ruleRepository.findAllByOrderByRuleIdAsc();
            for (SecurityRule r : fromDb) {
                activeRules.put(r.getRuleId(), r);
            }
            logger.info("Synchronized {} security rules with MongoDB.", activeRules.size());
        } catch (Exception e) {
            logger.warn("MongoDB unavailable for rule sync. Running with {} in-memory default rules.", activeRules.size());
        }
    }

    public List<MatchedRuleDto> evaluate(AnalyzeRequest request, double requestRate1m) {
        List<MatchedRuleDto> matches = new ArrayList<>();

        String path = request.getPath() != null ? request.getPath() : "/";
        String query = request.getQuery() != null ? request.getQuery() : "";
        String body = request.getBody() != null ? request.getBody() : "";
        String method = request.getMethod() != null ? request.getMethod().trim().toUpperCase() : "GET";

        String decodedPath = safeUrlDecode(path);
        String decodedQuery = safeUrlDecode(query);
        String decodedBody = safeUrlDecode(body);
        String combined = decodedPath + " " + decodedQuery + " " + decodedBody;

        // 1. SQL Injection Check
        if (isRuleEnabled("SQLI-001")) {
            if (SQLI_PATTERN_1.matcher(combined).find() ||
                SQLI_PATTERN_2.matcher(combined).find() ||
                SQLI_PATTERN_3.matcher(combined).find() ||
                SQLI_PATTERN_4.matcher(combined).find()) {
                SecurityRule r = activeRules.get("SQLI-001");
                matches.add(new MatchedRuleDto("SQLI-001", r.getName(), r.getSeverity(), true,
                        "Suspicious SQL syntax pattern detected in request target or payload."));
            }
        }

        // 2. XSS Check
        if (isRuleEnabled("XSS-001")) {
            if (XSS_PATTERN_1.matcher(combined).find() ||
                XSS_PATTERN_2.matcher(combined).find() ||
                XSS_PATTERN_3.matcher(combined).find() ||
                XSS_PATTERN_4.matcher(combined).find()) {
                SecurityRule r = activeRules.get("XSS-001");
                matches.add(new MatchedRuleDto("XSS-001", r.getName(), r.getSeverity(), true,
                        "Suspicious script markup or dynamic script execution indicator detected."));
            }
        }

        // 3. Path Traversal Check
        if (isRuleEnabled("TRAV-001")) {
            if (TRAV_PATTERN_1.matcher(combined).find() ||
                TRAV_PATTERN_2.matcher(combined).find() ||
                TRAV_PATTERN_3.matcher(combined).find()) {
                SecurityRule r = activeRules.get("TRAV-001");
                matches.add(new MatchedRuleDto("TRAV-001", r.getName(), r.getSeverity(), true,
                        "Directory escape sequence or sensitive path reference detected."));
            }
        }

        // 4. Request Size Check
        if (isRuleEnabled("REQ-SIZE-001")) {
            int bodyBytes = body.getBytes(StandardCharsets.UTF_8).length;
            if (bodyBytes > properties.getMaxRequestBodyBytes() || (path.length() + query.length()) > 2048) {
                SecurityRule r = activeRules.get("REQ-SIZE-001");
                matches.add(new MatchedRuleDto("REQ-SIZE-001", r.getName(), r.getSeverity(), true,
                        String.format("Payload size (%d bytes) exceeds configured limit (%d bytes).",
                                bodyBytes, properties.getMaxRequestBodyBytes())));
            }
        }

        // 5. Rate Limit Check
        if (isRuleEnabled("RATE-001")) {
            if (requestRate1m > properties.getRateLimitPerMinute()) {
                SecurityRule r = activeRules.get("RATE-001");
                matches.add(new MatchedRuleDto("RATE-001", r.getName(), r.getSeverity(), true,
                        String.format("Client rate (%.1f req/min) exceeds threshold (%d req/min).",
                                requestRate1m, properties.getRateLimitPerMinute())));
            }
        }

        // 6. HTTP Method Check
        if (isRuleEnabled("METHOD-001")) {
            Set<String> allowed = Set.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS", "HEAD");
            if (!allowed.contains(method)) {
                SecurityRule r = activeRules.get("METHOD-001");
                matches.add(new MatchedRuleDto("METHOD-001", r.getName(), r.getSeverity(), true,
                        "Unsupported or dangerous HTTP method: " + method));
            }
        }

        return matches;
    }

    public List<SecurityRule> getAllRules() {
        return new ArrayList<>(activeRules.values());
    }

    public Optional<SecurityRule> toggleRule(String ruleId, boolean enabled, String actor) {
        SecurityRule rule = activeRules.get(ruleId);
        if (rule == null) {
            return Optional.empty();
        }

        boolean prev = rule.isEnabled();
        rule.setEnabled(enabled);
        rule.setUpdatedAt(Instant.now());
        rule.setUpdatedBy(actor != null ? actor : "admin");
        activeRules.put(ruleId, rule);

        // Record audit event
        AuditEvent audit = new AuditEvent(
                actor != null ? actor : "admin",
                enabled ? "RULE_ENABLED" : "RULE_DISABLED",
                "RULE",
                ruleId,
                Map.of("previousEnabled", prev, "newEnabled", enabled),
                UUID.randomUUID().toString()
        );

        try {
            ruleRepository.save(rule);
            auditRepository.save(audit);
        } catch (Exception e) {
            logger.warn("Could not persist rule update/audit to MongoDB: {}. Updated in-memory state.", e.getMessage());
        }

        return Optional.of(rule);
    }

    private boolean isRuleEnabled(String ruleId) {
        SecurityRule r = activeRules.get(ruleId);
        return r != null && r.isEnabled();
    }

    private String safeUrlDecode(String input) {
        if (input == null || input.isEmpty()) return "";
        try {
            return URLDecoder.decode(input, StandardCharsets.UTF_8);
        } catch (Exception e) {
            return input;
        }
    }
}
