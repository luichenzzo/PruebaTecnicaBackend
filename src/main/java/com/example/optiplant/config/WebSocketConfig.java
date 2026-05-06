package com.example.optiplant.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * Configures STOMP-over-WebSocket endpoints and in-memory topic brokering.
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    /**
     * Enables topic and queue destinations for outbound WebSocket messages.
     *
     * @param config broker registry to configure
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Simple in-memory broker for topic subscriptions
        config.enableSimpleBroker("/topic", "/queue");
        config.setApplicationDestinationPrefixes("/app");
    }

    /**
     * Registers the WebSocket handshake endpoint used by browser clients.
     *
     * @param registry STOMP endpoint registry
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // SockJS fallback endpoint
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }
}

