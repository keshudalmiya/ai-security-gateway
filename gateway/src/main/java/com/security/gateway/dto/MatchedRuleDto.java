package com.security.gateway.dto;

import com.security.gateway.domain.Severity;

public class MatchedRuleDto {
    private String ruleId;
    private String name;
    private Severity severity;
    private boolean matched;
    private String reason;

    public MatchedRuleDto() {}

    public MatchedRuleDto(String ruleId, String name, Severity severity, boolean matched, String reason) {
        this.ruleId = ruleId;
        this.name = name;
        this.severity = severity;
        this.matched = matched;
        this.reason = reason;
    }

    public String getRuleId() { return ruleId; }
    public void setRuleId(String ruleId) { this.ruleId = ruleId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Severity getSeverity() { return severity; }
    public void setSeverity(Severity severity) { this.severity = severity; }
    public boolean isMatched() { return matched; }
    public void setMatched(boolean matched) { this.matched = matched; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
