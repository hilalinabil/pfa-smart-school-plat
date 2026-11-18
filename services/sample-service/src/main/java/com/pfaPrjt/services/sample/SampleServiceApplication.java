package com.pfaPrjt.services.sample;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * THROWAWAY SERVICE. It exists only to validate the platform: Config Server,
 * Eureka registration, gateway routing and client-side load balancing across
 * two replicas.
 *
 * <p>Delete this module (and its route, Compose service and config-repo file)
 * when identity-service is created in Phase 3.
 */
@SpringBootApplication
public class SampleServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(SampleServiceApplication.class, args);
    }
}
