package com.test.foodtrip.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import com.test.foodtrip.domain.chat.websocket.ChatWebSocketHandler;

@Configuration
@EnableWebSocket // 웹소켓 기능 사용
public class WebSocketConfig implements WebSocketConfigurer {

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
    	//이 코드에서 "/ws/chat"이 엔드포인트. ( WebSocket 시작점)
    	//핸들러(WebSocketHandler) 웹소켓 메시지를 실제로 받고, 처리하고, 응답하는 클래스
        registry.addHandler(new ChatWebSocketHandler(), "/ws/chat")
                .setAllowedOrigins("*"); // CORS 허용
    }
}
