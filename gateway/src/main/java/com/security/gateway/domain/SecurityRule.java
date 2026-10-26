package com.security.gateway.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "security_rules")
public class SecurityRule {

    @Id
    private String id;

    @Indexed(unique = true)
    private String ruleId;

    private String name;
    private RuleCategory category;
    private String description;
    private Severity severity;
    private boolean enabled = true;
    private int version = 1;

    private Instant updatedAt = Instant.now();
    private String updatedBy = "system";

    public SecurityRule() {}

    public SecurityRule(String ruleId, String name, RuleCategory category, String description, Severity severity, boolean enabled) {
        this.ruleId = ruleId;
        this.name = name;
        this.category = category;
        this.description = description;
        this.severity = severity;
        this.enabled = enabled;
        this.updatedAt = Instant.now();
        this.updatedBy = "system";
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getRuleId() { return ruleId; }
    public void setRuleId(String ruleId) { this.ruleId = ruleId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public RuleCategory getCategory() { return category; }
    public void setCategory(RuleCategory category) { this.category = category; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Severity getSeverity() { return severity; }
    public void setSeverity(Severity severity) { this.severity = severity; }
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public int getVersion() { return version; }
    public void setVersion(int version) { this.version = version; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
    public String getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }
}
