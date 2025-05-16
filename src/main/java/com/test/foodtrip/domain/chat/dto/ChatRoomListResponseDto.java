package com.test.foodtrip.domain.chat.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ChatRoomListResponseDto {

    private Long id;
    private String title;
    private String thumbnailImageUrl; // null 허용
    private LocalDateTime createdAt;
}
