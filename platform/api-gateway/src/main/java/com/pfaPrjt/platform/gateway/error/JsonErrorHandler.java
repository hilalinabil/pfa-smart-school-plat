package com.pfaPrjt.platform.gateway.error;

import java.net.ConnectException;
import java.net.NoRouteToHostException;
import java.time.Instant;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.boot.webflux.error.ErrorWebExceptionHandler;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.HttpMessageWriter;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.reactive.result.view.ViewResolver;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;

import com.pfaPrjt.platform.gateway.filter.RequestIdFilter;

import reactor.core.publisher.Mono;

/**
 * Turns every gateway error into the uniform JSON body
 * {@code { timestamp, status, error, message, path, requestId }}.
 * Never returns a stack trace or an HTML page.
 *
 * <p>Ordered before Spring Boot's default handler (order -1).
 */
@Component
@Order(-2)
public class JsonErrorHandler implements ErrorWebExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(JsonErrorHandler.class);

    private final ServerResponse.Context responseContext;

    public JsonErrorHandler(ServerCodecConfigurer codecs) {
        List<HttpMessageWriter<?>> writers = codecs.getWriters();
        this.responseContext = new ServerResponse.Context() {
            @Override
            public List<HttpMessageWriter<?>> messageWriters() {
                return writers;
            }

            @Override
            public List<ViewResolver> viewResolvers() {
                return List.of();
            }
        };
    }

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        if (exchange.getResponse().isCommitted()) {
            return Mono.error(ex);
        }

        HttpStatus status = statusOf(ex);
        String requestId = requestIdOf(exchange);
        String path = exchange.getRequest().getPath().value();
        logError(status, path, requestId, ex);

        ErrorResponse body = new ErrorResponse(
                Instant.now().toString(),
                status.value(),
                status.getReasonPhrase(),
                messageOf(status, ex),
                path,
                requestId);

        exchange.getResponse().getHeaders().set(RequestIdFilter.HEADER, requestId);
        return ServerResponse.status(status)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .flatMap(response -> response.writeTo(exchange, responseContext));
    }

    static HttpStatus statusOf(Throwable ex) {
        if (ex instanceof ResponseStatusException rse) {
            HttpStatus status = HttpStatus.resolve(rse.getStatusCode().value());
            return status != null ? status : HttpStatus.INTERNAL_SERVER_ERROR;
        }
        if (isUnreachableInstance(ex) || isUnreachableInstance(ex.getCause())) {
            // An instance is still in the registry but is gone (stopped or crashed).
            return HttpStatus.SERVICE_UNAVAILABLE;
        }
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }

    private static boolean isUnreachableInstance(Throwable ex) {
        return ex instanceof ConnectException || ex instanceof NoRouteToHostException;
    }

    private static String messageOf(HttpStatus status, Throwable ex) {
        return switch (status) {
            case NOT_FOUND -> "No route matches this path.";
            case SERVICE_UNAVAILABLE -> "The target service is unavailable. Try again later.";
            case GATEWAY_TIMEOUT -> "The target service did not respond in time.";
            case INTERNAL_SERVER_ERROR -> "Unexpected gateway error.";
            default -> ex instanceof ResponseStatusException rse && rse.getReason() != null
                    ? rse.getReason()
                    : status.getReasonPhrase();
        };
    }

    /** Unknown paths never reach the global filters, so fall back to the incoming header. */
    private static String requestIdOf(ServerWebExchange exchange) {
        String requestId = exchange.getAttribute(RequestIdFilter.ATTRIBUTE);
        return requestId != null ? requestId : RequestIdFilter.resolveRequestId(exchange.getRequest());
    }

    private static void logError(HttpStatus status, String path, String requestId, Throwable ex) {
        try (MDC.MDCCloseable ignored = MDC.putCloseable(RequestIdFilter.MDC_KEY, requestId)) {
            if (status == HttpStatus.INTERNAL_SERVER_ERROR) {
                log.error("Gateway error on {}", path, ex);
            } else {
                log.warn("Gateway returned {} for {}: {}", status.value(), path, ex.getMessage());
            }
        }
    }
}
