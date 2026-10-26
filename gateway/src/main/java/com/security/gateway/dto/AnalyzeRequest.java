package com.security.gateway.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.HashMap;
import java.util.Map;

public class AnalyzeRequest {

    @NotBlank(message = "HTTP method is required")
    @Pattern(regexp = "^(GET|POST|PUT|PATCH|DELETE|OPTIONS|HEAD)$", message = "Invalid HTTP method")
    private String method = "GET";

    @NotBlank(message = "Path is required")
    @Pattern(regexp = "^/.*", message = "Path must start with /")
    @Size(max = 2048, message = "Path exceeds maximum allowed length")
    private String path = "/";

    @Size(max = 4096, message = "Query string exceeds maximum allowed length")
    private String query = "";

    private Map<String, String> headers = new HashMap<>();

    @Size(max = 1048576, message = "Request body exceeds maximum allowed size (1MB)")
    private String body = "";

    public AnalyzeRequest() {}

    public AnalyzeRequest(String method, String path, String query, Map<String, String> headers, String body) {
        this.method = method;
        this.path = path;
        this.query = query != null ? query : "";
        this.headers = headers != null ? headers : new HashMap<>();
        this.body = body != null ? body : "";
    }

    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }
    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }
    public String getQuery() { return query; }
    public void setQuery(String query) { this.query = query; }
    public Map<String, String> getHeaders() { return headers; }
    public void setHeaders(Map<String, String> headers) { this.headers = headers; }
    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }
}
