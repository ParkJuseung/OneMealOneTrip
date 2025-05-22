package com.test.foodtrip.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.*;

@Configuration
@EnableWebSocketMessageBroker  // 이게 핵심! STOMP를 사용한 WebSocket을 활성화
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    // 1. 클라이언트가 메시지 보낼 때 prefix
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic"); // 구독 주소 prefix
        config.setApplicationDestinationPrefixes("/app"); // 클라이언트가 보낼 주소 prefix
    }

    // 2. STOMP 웹소켓 엔드포인트 등록 (클라이언트가 연결할 URL)
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws-stomp") // 웹소켓 연결 URL (예: ws://localhost:8090/ws-stomp)
                .setAllowedOriginPatterns("*") // CORS 허용
                .withSockJS(); // SockJS fallback 활성화 버전 낮은 브라우저에서 적용 가능
    }
}
