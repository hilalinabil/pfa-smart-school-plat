package com.pfaPrjt.platform.gateway;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;

import reactor.core.publisher.Mono;

/** Checks the routes loaded from infrastructure/config-repo/api-gateway.yml. */
@SpringBootTest
class RouteConfigurationTests {

    @Autowired
    private RouteLocator routeLocator;

    @Test
    void sampleRouteTargetsSampleServiceThroughLoadBalancer() {
        Route route = routeLocator.getRoutes()
                .filter(r -> r.getId().equals("sample-service"))
                .blockFirst();

        assertThat(route).isNotNull();
        assertThat(route.getUri()).isEqualTo(URI.create("lb://sample-service"));
        assertThat(matches(route, "/api/v1/sample/ping")).isTrue();
        assertThat(matches(route, "/api/v1/documents")).isFalse();
    }

    @Test
    void allBusinessRoutesFromReadmeAreDefined() {
        assertThat(routeLocator.getRoutes().map(Route::getId).collectList().block())
                .contains("identity-service", "user-service", "document-service", "request-service",
                        "assistant-service", "notification-service", "audit-service", "reporting-service");
    }

    private static boolean matches(Route route, String path) {
        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get(path));
        return Boolean.TRUE.equals(Mono.from(route.getPredicate().apply(exchange)).block());
    }
}
