package com.test.foodtrip.domain.chat.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * 클라이언트 → 서버로 채팅 메시지 전송 시 사용하는 DTO
 */
@Getter 
@Setter

//DTO를 기반으로 ChatMessage.create()를 호출해서 저장
//구독 중인 사용자들에게 실시간으로 브로드캐스트
public class ChatMessageSendRequestDTO {
    private Long chatRoomId;     // 어떤 채팅방에서
    private Long userId;         // 누가 (로그인된 사용자)
    private String content;      // 어떤 메시지를 보냈는지
    private String messageType;  // "TEXT", "IMAGE", "LOCATION" 등
}