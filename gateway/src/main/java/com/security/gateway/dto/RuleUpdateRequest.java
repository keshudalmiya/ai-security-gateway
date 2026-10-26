package com.security.gateway.dto;

public class RuleUpdateRequest {
    private boolean enabled;

    public RuleUpdateRequest() {}

    public RuleUpdateRequest(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
}
