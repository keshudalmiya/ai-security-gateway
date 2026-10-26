package com.security.gateway.dto;

import java.util.Map;

public class ApiErrorResponse {
    private ErrorDetails error;

    public ApiErrorResponse() {}

    public ApiErrorResponse(String code, String message, String correlationId, Map<String, String> fieldErrors) {
        this.error = new ErrorDetails(code, message, correlationId, fieldErrors);
    }

    public static class ErrorDetails {
        private String code;
        private String message;
        private String correlationId;
        private Map<String, String> fieldErrors;

        public ErrorDetails() {}

        public ErrorDetails(String code, String message, String correlationId, Map<String, String> fieldErrors) {
            this.code = code;
            this.message = message;
            this.correlationId = correlationId;
            this.fieldErrors = fieldErrors;
        }

        public String getCode() { return code; }
        public void setCode(String code) { this.code = code; }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }

        public String getCorrelationId() { return correlationId; }
        public void setCorrelationId(String correlationId) { this.correlationId = correlationId; }

        public Map<String, String> getFieldErrors() { return fieldErrors; }
        public void setFieldErrors(Map<String, String> fieldErrors) { this.fieldErrors = fieldErrors; }
    }

    public ErrorDetails getError() { return error; }
    public void setError(ErrorDetails error) { this.error = error; }
}
