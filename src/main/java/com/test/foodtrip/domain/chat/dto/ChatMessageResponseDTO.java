package com.test.foodtrip.domain.chat.dto;

import com.test.foodtrip.domain.chat.entity.ChatMessage;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;



//프론트(구독자)에게 전송할 메시지 응답 DTO
@Getter
@Builder
@Schema(description = "채팅 메시지 응답 DTO")
public class ChatMessageResponseDTO {
    @Schema(description = "메시지 ID", example = "101")
    private Long messageId; // 메시지
    @Schema(description = "보낸 유저 ID", example = "1001")
    private Long userId; // 유저
    @Schema(description = "채팅방 ID", example = "2001")
    private Long chatRoomId; // 어느 채팅방에 속한 메시지인지
    @Schema(description = "보낸 유저의 닉네임", example = "맛집러")
    private String senderNickname; // 보낸 유저의 닉네임
    @Schema(description = "메시지 내용", example = "안녕하세요! 오늘 점심 뭐 먹을까요?")
    private String content; // 메시지 내용
    @Schema(description = "메시지 타입", example = "TEXT")
    private String messageType; // 메시지 타입
    
    /**
     * Entity → DTO 변환 메서드
     * ChatMessage 객체를 ChatMessageResponseDTO 형태로 변환할 때 사용
     *
     * 사용 예:
     * ChatMessage message = ...;
     * ChatMessageResponseDTO dto = ChatMessageResponseDTO.fromEntity(message);
     */
    public static ChatMessageResponseDTO fromEntity(ChatMessage message) {
        return ChatMessageResponseDTO.builder()
                .messageId(message.getId())
                .chatRoomId(message.getChatRoom().getId())
                .userId(message.getUser().getId())
                .senderNickname(message.getUser().getNickname())
                .content(message.getMessageContent())
                .messageType(message.getMessageType())
                .build();
    }
}
