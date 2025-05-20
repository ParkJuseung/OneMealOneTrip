package com.test.foodtrip.domain.chat.dto;

import lombok.Getter;
import java.util.List;

@Getter
public class ChatRoomCreateRequestDTO {
    private String title;
    private List<String> hashtags;
    private String notice;
    private String description;
    private String thumbnailImageUrl; // 향후 확장 가능
}
