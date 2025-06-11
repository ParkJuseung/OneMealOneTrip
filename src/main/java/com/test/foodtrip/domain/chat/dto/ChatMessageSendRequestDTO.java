package com.test.foodtrip.domain.chat.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * 클라이언트 → 서버로 채팅 메시지 전송 시 사용하는 DTO
 */
@Getter 
@Setter

//DTO를 기반으로 ChatMessage.create()를 호출해서 저장
//구독 중인 사용자들에게 실시간으로 브로드캐스트
@Schema(description = "채팅 메시지 전송 요청 DTO")
public class ChatMessageSendRequestDTO {
    @Schema(description = "채팅방 ID", example = "2001")
    private Long chatRoomId;     // 어떤 채팅방에서
    @Schema(description = "보낸 사용자 ID", example = "1001")
    private Long userId;         // 누가 (로그인된 사용자)
    @Schema(description = "메시지 내용", example = "오늘 점심 뭐 드셨어요?")
    private String content;      // 어떤 메시지를 보냈는지
    @Schema(description = "메시지 타입", example = "TEXT")
    private String messageType;  // "TEXT", "IMAGE", "LOCATION" 등
}