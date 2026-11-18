package com.pfaPrjt.platform.gateway.filter;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;

/**
 * Extension point for Phase 3. DISABLED: the bean only exists when
 * {@code gateway.jwt-pre-check.enabled=true}, and even then it does nothing yet.
 *
 * <p>TODO(phase-3): reject requests early, with a uniform 401, when the bearer token is
 * missing, expired, or badly signed (signature verified against the Identity &amp; Access
 * Service JWKS endpoint, cached). Public paths such as sign-in must stay open.
 *
 * <p>This is only a coarse first filter. Every service still validates the token itself
 * and performs the full permission, scope and ownership checks (README §8). The gateway
 * must never be the only place where authorization happens.
 */
@Component
@ConditionalOnProperty(name = "gateway.jwt-pre-check.enabled", havingValue = "true")
public class JwtPreCheckFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        return chain.filter(exchange);
    }

    /** Right after RequestIdFilter, so rejections still carry a request ID. */
    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 10;
    }
}
