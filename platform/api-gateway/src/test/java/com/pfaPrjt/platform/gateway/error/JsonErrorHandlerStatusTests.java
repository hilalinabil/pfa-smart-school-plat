package com.pfaPrjt.platform.gateway.error;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.ConnectException;
import java.net.NoRouteToHostException;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

class JsonErrorHandlerStatusTests {

    @Test
    void keepsStatusOfResponseStatusException() {
        assertThat(JsonErrorHandler.statusOf(new ResponseStatusException(HttpStatus.GATEWAY_TIMEOUT)))
                .isEqualTo(HttpStatus.GATEWAY_TIMEOUT);
    }

    @Test
    void mapsUnreachableInstanceTo503() {
        assertThat(JsonErrorHandler.statusOf(new ConnectException("refused"))).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
        assertThat(JsonErrorHandler.statusOf(new NoRouteToHostException("gone"))).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
    }

    @Test
    void mapsAnythingElseTo500() {
        assertThat(JsonErrorHandler.statusOf(new IllegalStateException("boom"))).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
