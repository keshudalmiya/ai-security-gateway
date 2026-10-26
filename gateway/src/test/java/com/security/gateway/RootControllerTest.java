package com.security.gateway;

import com.security.gateway.controller.GlobalExceptionHandler;
import com.security.gateway.controller.RootController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class RootControllerTest {

    private MockMvc mockMvc;

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new RootController())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    public void testGetRootReturnsServiceInfo() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.service").value("ai-security-gateway"))
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.version").value("1.0.0"))
                .andExpect(jsonPath("$.docs").value("/api/v1/swagger-ui.html"))
                .andExpect(jsonPath("$.apiPrefix").value("/api/v1"))
                .andExpect(jsonPath("$.endpoints.health").value("/api/v1/health"))
                .andExpect(jsonPath("$.endpoints.analyze").value("/api/v1/analyze"))
                .andExpect(jsonPath("$.endpoints.metrics").value("/api/v1/metrics/summary"));
    }

    @Test
    public void testGetFaviconReturnsNoContent() throws Exception {
        mockMvc.perform(get("/favicon.ico"))
                .andExpect(status().isNoContent());
    }
}
