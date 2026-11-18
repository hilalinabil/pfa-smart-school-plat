package com.pfaPrjt.platform.gateway.filter;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;

/**
 * Gives every request a request ID and makes it visible everywhere:
 * <ul>
 *   <li>reuses the incoming {@code X-Request-Id} (normally set by Nginx), or generates one;</li>
 *   <li>forwards it to the downstream service;</li>
 *   <li>returns it in the response;</li>
 *   <li>stores it as an exchange attribute (read by the error handler);</li>
 *   <li>logs one access line per request with the MDC key {@code requestId}.</li>
 * </ul>
 */
@Component
public class RequestIdFilter implements GlobalFilter, Ordered {

    public static final String HEADER = "X-Request-Id";
    public static final String MDC_KEY = "requestId";
    public static final String ATTRIBUTE = RequestIdFilter.class.getName() + ".requestId";

    private static final Logger log = LoggerFactory.getLogger(RequestIdFilter.class);
    private static final int MAX_LENGTH = 128;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String requestId = resolveRequestId(exchange.getRequest());
        long start = System.nanoTime();

        ServerHttpRequest request = exchange.getRequest().mutate()
                .headers(headers -> headers.set(HEADER, requestId))
                .build();
        exchange.getResponse().getHeaders().set(HEADER, requestId);
        exchange.getAttributes().put(ATTRIBUTE, requestId);

        ServerWebExchange mutated = exchange.mutate().request(request).build();
        return chain.filter(mutated)
                .doFinally(signal -> logAccess(mutated, requestId, start));
    }

    /** Runs first, so every other filter and the error handler see the ID. */
    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

    public static String resolveRequestId(ServerHttpRequest request) {
        String incoming = request.getHeaders().getFirst(HEADER);
        if (StringUtils.hasText(incoming) && incoming.length() <= MAX_LENGTH) {
            return incoming.trim();
        }
        return UUID.randomUUID().toString();
    }

    private void logAccess(ServerWebExchange exchange, String requestId, long start) {
        try (MDC.MDCCloseable ignored = MDC.putCloseable(MDC_KEY, requestId)) {
            log.info("{} {} -> {} ({} ms)",
                    exchange.getRequest().getMethod(),
                    exchange.getRequest().getPath().value(),
                    exchange.getResponse().getStatusCode() != null ? exchange.getResponse().getStatusCode().value() : "-",
                    (System.nanoTime() - start) / 1_000_000);
        }
    }
}
