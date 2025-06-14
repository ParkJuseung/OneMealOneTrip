package com.test.foodtrip.domain.chat.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "채팅방 상세 응답 DTO")
public class ChatRoomDetailResponseDTO {
    @Schema(description = "채팅방 ID", example = "1")
    private Long id;
    @Schema(description = "채팅방 제목", example = "맛집 채팅방")
    private String title;
    @Schema(description = "썸네일 이미지 URL", example = "https://example.com/chatroom-thumbnail.jpg")
    private String thumbnailImageUrl;
    @Schema(description = "생성일", example = "2025-06-08T14:00:00")
    private LocalDateTime createdAt;

    @Schema(description = "공지사항 내용", example = "환영합니다!")
    private String notice;         // 공지사항 내용
    @Schema(description = "공지사항 설명", example = "맛집 정보를 공유하는 채팅방입니다")
    private String description;    // 공지사항 설명
    @Schema(description = "해시태그 텍스트 목록", example = "['한식', '서울']")
    private List<String> hashtags; // 해시태그 텍스트 목록
    @Schema(description = "내 역할", example = "OWNER")
    private String myRole; // 예: "OWNER", "JOINED", "OUTSIDER"
    @Schema(description = "좋아요 수", example = "12")
    private int likeCount;
    @Schema(description = "참여자 수", example = "5")
    private int participantCount;
    @Schema(description = "채팅방 소유자 닉네임", example = "맛집탐방러")
    private String ownerNickname;
    @Schema(description = "좋아요 여부", example = "true")
    private boolean liked;

}
