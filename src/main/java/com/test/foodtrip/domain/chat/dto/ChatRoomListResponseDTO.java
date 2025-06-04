package com.test.foodtrip.domain.chat.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class ChatRoomListResponseDTO {

	private Long id; // 채팅방 ID (링크 이동 등에 사용)
	private String title; // 채팅방 제목
	private String thumbnailImageUrl; // 썸네일 이미지 (옵션)
	private LocalDateTime createdAt; // 생성일 (정렬용 등)
	private List<String> hashtags; // 해시태그 리스트

	private int likeCount; // ❤️ 좋아요 수
	private int participantCount; // 참여자 수
	private String ownerNickname; // 방장 닉네임
	private boolean liked; // 좋아요 여부
}
