package com.pfaPrjt.platform.gateway.error;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

/**
 * Real gateway with the real routes and no service instances: an unknown path
 * gives a JSON 404 and a known route without instances gives a JSON 503.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
class JsonErrorHandlerTests {

    @Autowired
    private WebTestClient client;

    @Test
    void unavailableServiceReturnsUniformJson503() {
        client.get().uri("/api/v1/documents")
                .header("X-Request-Id", "test-503")
                .exchange()
                .expectStatus().isEqualTo(503)
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                .expectHeader().valueEquals("X-Request-Id", "test-503")
                .expectBody()
                .jsonPath("$.timestamp").isNotEmpty()
                .jsonPath("$.status").isEqualTo(503)
                .jsonPath("$.error").isEqualTo("Service Unavailable")
                .jsonPath("$.message").isNotEmpty()
                .jsonPath("$.path").isEqualTo("/api/v1/documents")
                .jsonPath("$.requestId").isEqualTo("test-503");
    }

    @Test
    void unknownRouteReturnsUniformJson404() {
        client.get().uri("/api/v1/does-not-exist")
                .header("X-Request-Id", "test-404")
                .accept(MediaType.TEXT_HTML)
                .exchange()
                .expectStatus().isNotFound()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.status").isEqualTo(404)
                .jsonPath("$.error").isEqualTo("Not Found")
                .jsonPath("$.path").isEqualTo("/api/v1/does-not-exist")
                .jsonPath("$.requestId").isEqualTo("test-404");
    }
}
