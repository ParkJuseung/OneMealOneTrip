package com.test.foodtrip.domain.chat.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@Schema(description = "채팅방 생성 요청 DTO")
public class ChatRoomCreateRequestDTO {
    @Schema(description = "채팅방 제목", example = "맛집 채팅방")
    private String title;
    @Schema(description = "해시태그 목록", example = "[\"서울\", \"한식\"]")
    private List<String> hashtags = new ArrayList<>();
    @Schema(description = "공지사항 제목", example = "환영합니다")
    private String notice;
    @Schema(description = "설명글", example = "이 채팅방은 음식 여행을 좋아하는 분들을 위한 공간입니다.")
    private String description;

    @Schema(description = "썸네일 이미지 파일")
    private MultipartFile thumbnailImage;
    @Schema(description = "썸네일 이미지 URL")
    private String thumbnailImageUrl;
}
