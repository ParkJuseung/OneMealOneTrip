package com.test.foodtrip.domain.chat.websocket;

import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

//WebSocket 텍스트 메시지를 처리하는 핸들러 클래스
public class ChatWebSocketHandler extends TextWebSocketHandler {

	// 클라이언트로부터 텍스트 메시지를 수신했을 때 실행되는 메서드
    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
    	 // 클라이언트가 보낸 실제 메시지 내용을 문자열로 추출
        String payload = message.getPayload();
        // 서버 콘솔에 수신한 메시지를 출력 (디버깅용)
        System.out.println("📩 받은 메시지: " + payload);

        // 받은 메시지를 그대로 다시 클라이언트에게 전송 (Echo 응답)
        session.sendMessage(new TextMessage("서버 응답: " + payload));
    }
}
