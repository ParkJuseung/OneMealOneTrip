package com.test.foodtrip.domain.chat.dto;

import com.test.foodtrip.domain.chat.entity.ChatMessage;

import lombok.Builder;
import lombok.Getter;



//프론트(구독자)에게 전송할 메시지 응답 DTO
@Getter
@Builder
public class ChatMessageResponseDTO {
	private Long messageId; // 메시지
	private Long userId; // 유저
    private Long chatRoomId; // 어느 채팅방에 속한 메시지인지
    private String senderNickname; // 보낸 유저의 닉네임
    private String content; // 메시지 내용
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
