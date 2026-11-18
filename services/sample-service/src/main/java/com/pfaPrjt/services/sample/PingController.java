package com.pfaPrjt.services.sample;

import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.cloud.client.serviceregistry.Registration;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
class PingController {

    private static final Logger log = LoggerFactory.getLogger(PingController.class);

    private final String instanceId;

    /** Uses the Eureka instance ID, so the answer matches the Eureka dashboard. */
    PingController(ObjectProvider<Registration> registration) {
        Registration current = registration.getIfAvailable();
        this.instanceId = current != null ? current.getInstanceId() : "local";
    }

    @GetMapping("/api/v1/sample/ping")
    PingResponse ping(@RequestHeader(name = RequestIdMdcFilter.HEADER, required = false) String requestId) {
        log.info("Ping handled by {}", instanceId);
        return new PingResponse("sample-service", instanceId, requestId, Instant.now().toString());
    }

    record PingResponse(String service, String instanceId, String requestId, String timestamp) {
    }
}
