package com.test.foodtrip.domain.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder

//채팅방에 입장하려는 사용자가 서버에 보내는 요청 정보를 담는 객체
public class ChatroomEnterRequestDTO {

	   private Long roomId;               // 채팅방 ID
	   private Long userId;               // 사용자 ID
	   private String role;               // 기본: MEMBER (OWNER/MANAGER/MEMBER)
	   private String profileImageUrl;    // 채팅방 전용 프로필 이미지
}
