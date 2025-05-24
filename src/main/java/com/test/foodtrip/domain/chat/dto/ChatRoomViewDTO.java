package com.test.foodtrip.domain.chat.dto;


import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ChatRoomViewDTO {
	// 채팅방 정보를 보여주는 DTO Thymeleaf 같은 HTML 뷰를 렌더링하기 위해 전달되는 데이터 구조
	//@NoArgsConstructor도 필요 없음 (뷰에만 쓰니까)
    private Long chatRoomId; // 채팅방
    private Long userId; // 유저
    private String nickname; // 사용자 별명
    private String chatRoomTitle; // 채팅방 제목
}