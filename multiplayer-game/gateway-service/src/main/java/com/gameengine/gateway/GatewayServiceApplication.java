package com.gameengine.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;

/**
 * Main Spring Boot application for the Gateway Service.
 * 
 * This service is responsible for:
 * - Accepting WebSocket connections from clients
 * - Receiving player actions and publishing to Kafka
 * - Consuming game state updates and broadcasting to clients
 * - Providing REST endpoints for debugging
 */
@SpringBootApplication
@EnableKafka
public class GatewayServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(GatewayServiceApplication.class, args);
    }
}

