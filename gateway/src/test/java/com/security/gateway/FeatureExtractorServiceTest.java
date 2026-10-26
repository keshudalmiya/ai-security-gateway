package com.security.gateway;

import com.security.gateway.dto.AnalyzeRequest;
import com.security.gateway.dto.FeaturePayload;
import com.security.gateway.service.FeatureExtractorService;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class FeatureExtractorServiceTest {

    private final FeatureExtractorService extractor = new FeatureExtractorService();

    @Test
    public void testFeatureExtractionStandardGet() {
        AnalyzeRequest req = new AnalyzeRequest("GET", "/api/products", "id=10&cat=books", Map.of("Host", "localhost"), "");
        FeaturePayload f = extractor.extract(req, 1.0);

        assertEquals(0, f.getHttpMethodCode()); // GET is 0
        assertEquals(29, f.getUrlLength()); // /api/products?id=10&cat=books is 29 chars
        assertEquals(2, f.getParameterCount()); // 2 parameters
        assertEquals(0, f.getBodyLength());
        assertEquals(1, f.getHeaderCount());
        assertTrue(f.getRequestLength() > f.getUrlLength());
    }

    @Test
    public void testFeatureExtractionSuspiciousSpecialChars() {
        AnalyzeRequest req = new AnalyzeRequest(
                "POST",
                "/login",
                "",
                Map.of(),
                "{\"user\": \"admin'--\", \"pass\": \"<script>\"}"
        );
        FeaturePayload f = extractor.extract(req, 5.0);

        assertEquals(1, f.getHttpMethodCode()); // POST is 1
        assertTrue(f.getSpecialCharCount() >= 5); // contains ', -, <, >, {, }
        assertEquals(5.0, f.getRequestRate1m());
    }
}
