package com.pfaPrjt.services.sample;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class SampleServiceApplicationTests {

    @Autowired
    private MockMvc mvc;

    @Test
    void contextLoads() {
    }

    @Test
    void pingEchoesRequestId() throws Exception {
        mvc.perform(get("/api/v1/sample/ping").header("X-Request-Id", "test-123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.service").value("sample-service"))
                .andExpect(jsonPath("$.instanceId").isNotEmpty())
                .andExpect(jsonPath("$.requestId").value("test-123"))
                .andExpect(jsonPath("$.timestamp").isNotEmpty());
    }
}
