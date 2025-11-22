package com.gameengine.engine.config;

import com.gameengine.engine.model.StateUpdate;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

/**
 * Kafka producer configuration for publishing state updates.
 */
@Configuration
public class KafkaProducerConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Bean
    public ProducerFactory<String, StateUpdate> stateUpdateProducerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        
        // Basic Kafka properties
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        
        // Reliability and idempotence
        configProps.put(ProducerConfig.ACKS_CONFIG, "all");
        configProps.put(ProducerConfig.RETRIES_CONFIG, 3);
        configProps.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
        configProps.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, 5);
        
        // Performance
        configProps.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "snappy");
        configProps.put(ProducerConfig.BATCH_SIZE_CONFIG, 16384);
        configProps.put(ProducerConfig.LINGER_MS_CONFIG, 10);
        configProps.put(ProducerConfig.BUFFER_MEMORY_CONFIG, 33554432);
        
        // Type mapping for JSON serializer
        configProps.put(JsonSerializer.ADD_TYPE_INFO_HEADERS, false);
        Map<String, Class<?>> typeMappings = new HashMap<>();
        typeMappings.put("gameStateUpdate", StateUpdate.class);
        configProps.put(JsonSerializer.TYPE_MAPPINGS, typeMappings);

        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean(name = "stateUpdateKafkaTemplate")
    public KafkaTemplate<String, StateUpdate> stateUpdateKafkaTemplate() {
        return new KafkaTemplate<>(stateUpdateProducerFactory());
    }
}

