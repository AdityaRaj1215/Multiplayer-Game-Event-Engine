package com.gameengine.gateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * WebSocket configuration using STOMP protocol.
 * 
 * Endpoints:
 * - WebSocket endpoint: /ws
 * - Client subscriptions: /topic/room/{roomId}
 * - Client sends to: /app/player/action
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Value("${websocket.allowed-origins:*}")
    private String allowedOrigins;

    @Value("${websocket.endpoint:/ws}")
    private String endpoint;

    /**
     * Configures the message broker.
     * Uses simple in-memory broker for topic subscriptions.
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Enable simple broker for topic subscriptions
        // Clients can subscribe to: /topic/room/{roomId}
        config.enableSimpleBroker("/topic");
        
        // Prefix for messages from client to server
        // Clients send to: /app/player/action
        config.setApplicationDestinationPrefixes("/app");
        
        // User destination prefix (for private messages)
        config.setUserDestinationPrefix("/user");
    }

    /**
     * Registers STOMP endpoints.
     * Clients connect to /ws endpoint with SockJS fallback.
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Register WebSocket endpoint with SockJS fallback
        registry.addEndpoint(endpoint)
                .setAllowedOriginPatterns(allowedOrigins.split(","))
                .withSockJS()
                .setHeartbeatTime(10000)  // 10 seconds
                .setDisconnectDelay(30000); // 30 seconds
    }
}

