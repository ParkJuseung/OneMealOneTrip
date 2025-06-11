package com.test.foodtrip.domain.chat.dto;

import java.time.format.DateTimeFormatter;

import com.test.foodtrip.domain.chat.entity.ChatMessage;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "채팅 메시지 응답 DTO")
public class ChatMessageResponseDTO {

    @Schema(description = "메시지 ID", example = "101")
    private Long messageId;

    @Schema(description = "보낸 유저 ID", example = "1001")
    private Long userId;

    @Schema(description = "채팅방 ID", example = "2001")
    private Long chatRoomId;

    @Schema(description = "보낸 유저의 닉네임", example = "맛집러")
    private String senderNickname;

    @Schema(description = "보낸 유저의 역할 (OWNER, MEMBER)", example = "OWNER")
    private String senderRole; // ✅ 방장 판단용

    @Schema(description = "메시지 내용", example = "안녕하세요! 오늘 점심 뭐 먹을까요?")
    private String content;

    @Schema(description = "메시지 타입", example = "TEXT")
    private String messageType;

    @Schema(description = "작성 시간 (HH:mm)", example = "14:57")
    private String createdAt; // ✅ 문자열 형태로 포맷

    /**
     * Entity → DTO 변환 메서드
     */
    public static ChatMessageResponseDTO fromEntity(ChatMessage message, String senderRole) {
        return ChatMessageResponseDTO.builder()
                .messageId(message.getId())
                .chatRoomId(message.getChatRoom().getId())
                .userId(message.getUser().getId())
                .senderNickname(message.getUser().getNickname())
                .senderRole(senderRole)
                .content(message.getMessageContent())
                .messageType(message.getMessageType())
                .createdAt(message.getCreatedAt().format(DateTimeFormatter.ofPattern("HH:mm")))
                .build();
    }
}
