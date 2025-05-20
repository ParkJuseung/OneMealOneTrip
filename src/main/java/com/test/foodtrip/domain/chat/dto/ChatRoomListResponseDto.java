package com.test.foodtrip.domain.chat.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class ChatRoomListResponseDTO {

    private Long id;
    private String title;
    private String thumbnailImageUrl; // null 허용
    private LocalDateTime createdAt;
    private List<String> hashtags;
}
