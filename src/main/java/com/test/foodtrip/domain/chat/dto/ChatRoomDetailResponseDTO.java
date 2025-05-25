package com.test.foodtrip.domain.chat.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ChatRoomDetailResponseDTO {
    private Long id;
    private String title;
    private String thumbnailImageUrl;
    private LocalDateTime createdAt;

    private String notice;         // 공지사항 내용
    private String description;    // 공지사항 설명

    private List<String> hashtags; // 해시태그 텍스트 목록

    private String myRole; // 예: "OWNER", "JOINED", "OUTSIDER"
    private int likeCount;
    private int participantCount;
    private String ownerNickname;

    private boolean liked;

}
