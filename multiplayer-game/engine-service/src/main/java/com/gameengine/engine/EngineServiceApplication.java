package com.gameengine.engine;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Main Spring Boot application for the Engine Service.
 * 
 * This service is responsible for:
 * - Consuming player events from Kafka
 * - Applying deterministic game logic
 * - Storing authoritative game state in Redis
 * - Publishing state updates to Kafka
 */
@SpringBootApplication
@EnableKafka
@EnableScheduling
public class EngineServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(EngineServiceApplication.class, args);
    }
}



