package com.test.foodtrip.domain.chat.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@Schema(description = "채팅방 목록 응답 DTO")
public class ChatRoomListResponseDTO {
	@Schema(description = "채팅방 ID", example = "101")
	private Long id; // 채팅방 ID (링크 이동 등에 사용)
	@Schema(description = "채팅방 제목", example = "맛집 채팅방")
	private String title; // 채팅방 제목
	@Schema(description = "썸네일 이미지 URL", example = "https://example.com/image.jpg")
	private String thumbnailImageUrl; // 썸네일 이미지 (옵션)
	@Schema(description = "생성일", example = "2025-06-08T12:00:00")
	private LocalDateTime createdAt; // 생성일 (정렬용 등)
	@Schema(description = "해시태그 리스트", example = "['한식', '서울']")
	private List<String> hashtags; // 해시태그 리스트
	@Schema(description = "좋아요 수", example = "15")
	private int likeCount; // ❤️ 좋아요 수
	@Schema(description = "참여자 수", example = "7")
	private int participantCount; // 참여자 수
	@Schema(description = "방장 닉네임", example = "맛집러")
	private String ownerNickname; // 방장 닉네임
	@Schema(description = "좋아요 여부", example = "true")
	private boolean liked; // 좋아요 여부
}
