package com.gameengine.engine.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.databind.type.MapType;
import com.fasterxml.jackson.databind.type.SimpleType;
import com.gameengine.engine.model.Bullet;
import com.gameengine.engine.model.GameState;
import com.gameengine.engine.model.Player;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Redis configuration for game state storage.
 */
@Configuration
public class RedisConfig {

    @Value("${spring.data.redis.host}")
    private String redisHost;

    @Value("${spring.data.redis.port:6379}")
    private int redisPort;

    @Value("${spring.data.redis.password:}")
    private String redisPassword;

    @Bean
    public JedisConnectionFactory jedisConnectionFactory() {
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
        config.setHostName(redisHost);
        config.setPort(redisPort);
        if (redisPassword != null && !redisPassword.isEmpty()) {
            config.setPassword(redisPassword);
        }
        return new JedisConnectionFactory(config);
    }

    @Bean
    public RedisTemplate<String, GameState> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, GameState> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // Key serializer - String
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());

        // Value serializer - JSON for GameState
        Jackson2JsonRedisSerializer<GameState> serializer = new Jackson2JsonRedisSerializer<>(GameState.class);
        
        // Configure ObjectMapper for proper serialization
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        objectMapper.activateDefaultTyping(
                objectMapper.getPolymorphicTypeValidator(),
                ObjectMapper.DefaultTyping.NON_FINAL
        );
        
        // Register custom type information for nested types
        MapType playerMapType = objectMapper.getTypeFactory().constructMapType(
                java.util.Map.class,
                SimpleType.constructUnsafe(String.class),
                SimpleType.constructUnsafe(Player.class)
        );
        
        CollectionType bulletListType = objectMapper.getTypeFactory().constructCollectionType(
                java.util.List.class,
                Bullet.class
        );
        
        serializer.setObjectMapper(objectMapper);
        
        template.setValueSerializer(serializer);
        template.setHashValueSerializer(serializer);
        
        template.afterPropertiesSet();
        return template;
    }
}


