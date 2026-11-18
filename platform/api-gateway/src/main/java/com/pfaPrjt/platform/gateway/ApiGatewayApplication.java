package com.pfaPrjt.platform.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Internal API gateway. Receives /api/** traffic from Nginx and routes it to
 * services by service ID (lb://service-id), resolved through Eureka.
 *
 * <p>Routes are configuration, not code: see infrastructure/config-repo/api-gateway.yml.
 */
@SpringBootApplication
public class ApiGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }
}
