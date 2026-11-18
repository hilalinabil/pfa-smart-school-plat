package com.pfaPrjt.platform.config;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class ConfigServerApplicationTests {

    @Autowired
    private MockMvc mvc;

    @Test
    void contextLoads() {
    }

    @Test
    void servesGatewayConfigurationFromConfigRepo() throws Exception {
        mvc.perform(get("/api-gateway/dev"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("api-gateway"))
                .andExpect(content().string(containsString("api-gateway.yml")))
                .andExpect(content().string(containsString("application.yml")))
                .andExpect(content().string(containsString("lb://sample-service")));
    }
}
