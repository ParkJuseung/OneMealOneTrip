package com.test.foodtrip.domain.chat.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
public class ChatRoomCreateRequestDTO {
    private String title;
    private List<String> hashtags = new ArrayList<>();
    private String notice;
    private String description;
    private MultipartFile thumbnailImage;
}
