package com.security.gateway.service;

import com.security.gateway.dto.AnalyzeRequest;
import com.security.gateway.dto.FeaturePayload;
import org.springframework.stereotype.Service;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
public class FeatureExtractorService {

    private static final Set<Character> SUSPICIOUS_CHARS = Set.of(
            '<', '>', '\'', '"', '\\', ';', '%', '(', ')', '=', '*', '-', '|', '&', '`', '$', '!', '^', '{', '}', '[', ']'
    );

    private static final Map<String, Integer> METHOD_MAP = Map.of(
            "GET", 0,
            "POST", 1,
            "PUT", 2,
            "PATCH", 3,
            "DELETE", 4,
            "OPTIONS", 5,
            "HEAD", 6
    );

    public FeaturePayload extract(AnalyzeRequest request, double requestRate1m) {
        String method = request.getMethod() != null ? request.getMethod().trim().toUpperCase() : "GET";
        int methodCode = METHOD_MAP.getOrDefault(method, 7);

        String path = request.getPath() != null ? request.getPath() : "/";
        String query = request.getQuery() != null ? request.getQuery() : "";
        String body = request.getBody() != null ? request.getBody() : "";
        Map<String, String> headers = request.getHeaders() != null ? request.getHeaders() : Collections.emptyMap();

        // 1. URL Length (path + query)
        String fullUrl = path + (query.isEmpty() ? "" : "?" + query);
        int urlLength = fullUrl.length();

        // 2. Body Length
        byte[] bodyBytes = body.getBytes(StandardCharsets.UTF_8);
        int bodyLength = bodyBytes.length;

        // 3. Request Length
        int headerSize = 0;
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            headerSize += entry.getKey().length() + (entry.getValue() != null ? entry.getValue().length() : 0) + 4;
        }
        int requestLength = method.length() + 1 + urlLength + 11 + headerSize + bodyLength;

        // 4. Parameter Count
        int paramCount = 0;
        if (!query.isEmpty()) {
            paramCount += countQueryParameters(query);
        }
        if (!body.isEmpty()) {
            if (body.contains("=") && (body.contains("&") || !body.trim().startsWith("{"))) {
                paramCount += countQueryParameters(body);
            } else if (body.trim().startsWith("{") && body.trim().endsWith("}")) {
                paramCount += countJsonKeys(body);
            }
        }

        // 5. Special Character Count
        String combined = fullUrl + body;
        int specialCharCount = 0;
        for (char c : combined.toCharArray()) {
            if (SUSPICIOUS_CHARS.contains(c)) {
                specialCharCount++;
            }
        }

        // 6. Header Count
        int headerCount = headers.size();

        return new FeaturePayload(
                requestLength,
                urlLength,
                paramCount,
                specialCharCount,
                methodCode,
                headerCount,
                bodyLength,
                Math.max(1.0, requestRate1m)
        );
    }

    private int countQueryParameters(String queryString) {
        if (queryString == null || queryString.isEmpty()) return 0;
        String[] pairs = queryString.split("&");
        int count = 0;
        for (String pair : pairs) {
            if (!pair.trim().isEmpty()) count++;
        }
        return count;
    }

    private int countJsonKeys(String json) {
        int count = 0;
        int idx = 0;
        while ((idx = json.indexOf("\":", idx)) != -1) {
            count++;
            idx += 2;
        }
        return count;
    }
}
