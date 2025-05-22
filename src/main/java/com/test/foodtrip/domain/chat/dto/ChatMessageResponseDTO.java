package com.test.foodtrip.domain.chat.dto;

import lombok.Builder;
import lombok.Getter;



//프론트(구독자)에게 전송할 메시지 구조를 담는 DTO
@Getter
@Builder
public class ChatMessageResponseDTO {
	private Long userId; // 유저
    private Long chatRoomId; // 어느 채팅방에 속한 메시지인지
    private String senderNickname; // 보낸 유저의 닉네임
    private String content; // 메시지 내용
    private String messageType; // 메시지 타입
}
