package com.hrms.notification.config;

import com.hrms.security.service.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import java.security.Principal;

@Slf4j
@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final JwtService jwtService;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic", "/queue");
        config.setApplicationDestinationPrefixes("/app");
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws").setAllowedOriginPatterns("*").withSockJS();
    }

    /**
     * STOMP CONNECT frames must carry an Authorization: Bearer <jwt> header. We validate
     * the JWT and bind the user as the session Principal so /user/{userId}/queue/...
     * destinations route correctly.
     */
    @Override
    public void configureClientInboundChannel(@NonNull ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(@NonNull Message<?> message, @NonNull MessageChannel channel) {
                StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
                if (accessor == null || !StompCommand.CONNECT.equals(accessor.getCommand())) return message;

                String authz = accessor.getFirstNativeHeader("Authorization");
                if (authz == null || !authz.startsWith("Bearer ")) {
                    throw new IllegalArgumentException("Missing or invalid Authorization header");
                }
                String token = authz.substring(7);
                try {
                    if (!jwtService.isValid(token)) {
                        throw new IllegalArgumentException("Invalid JWT");
                    }
                    String userId = jwtService.getUserId(token);
                    accessor.setUser(new StompPrincipal(userId));
                    log.debug("WebSocket CONNECT authorised for user {}", userId);
                } catch (Exception e) {
                    throw new IllegalArgumentException("JWT validation failed: " + e.getMessage());
                }
                return message;
            }
        });
    }

    public record StompPrincipal(String name) implements Principal {
        @Override public String getName() { return name; }
    }
}
