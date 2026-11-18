package com.pfaPrjt.platform.gateway.filter;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;

class RequestIdFilterTests {

    private final RequestIdFilter filter = new RequestIdFilter();

    @Test
    void propagatesExistingRequestId() {
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/v1/sample/ping").header(RequestIdFilter.HEADER, "test-123"));
        AtomicReference<ServerWebExchange> downstream = new AtomicReference<>();

        filter.filter(exchange, capture(downstream)).block();

        assertThat(downstream.get().getRequest().getHeaders().getFirst(RequestIdFilter.HEADER)).isEqualTo("test-123");
        assertThat(exchange.getResponse().getHeaders().getFirst(RequestIdFilter.HEADER)).isEqualTo("test-123");
    }

    @Test
    void generatesRequestIdWhenMissing() {
        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/api/v1/sample/ping"));
        AtomicReference<ServerWebExchange> downstream = new AtomicReference<>();

        filter.filter(exchange, capture(downstream)).block();

        String forwarded = downstream.get().getRequest().getHeaders().getFirst(RequestIdFilter.HEADER);
        assertThat(forwarded).isNotBlank();
        assertThat(exchange.getResponse().getHeaders().getFirst(RequestIdFilter.HEADER)).isEqualTo(forwarded);
    }

    private static GatewayFilterChain capture(AtomicReference<ServerWebExchange> downstream) {
        return exchange -> {
            downstream.set(exchange);
            return Mono.empty();
        };
    }
}
