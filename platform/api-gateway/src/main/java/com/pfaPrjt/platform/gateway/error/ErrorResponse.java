package com.pfaPrjt.platform.gateway.error;

/**
 * Uniform error body returned by the gateway for every error it produces
 * (unknown route, unavailable service, timeout, unexpected failure).
 */
public record ErrorResponse(
        String timestamp,
        int status,
        String error,
        String message,
        String path,
        String requestId) {
}
