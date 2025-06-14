package com.test.foodtrip.domain.chat.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import java.util.List;

//채팅방 수정 요청 시 클라이언트가 보내는 데이터 구조
@Getter
@Builder
@Schema(description = "채팅방 수정 요청 DTO")
public class ChatRoomEditRequestDTO {
    @Schema(description = "채팅방 제목", example = "업데이트된 맛집 채팅방")
    private String title;         // 수정할 제목
    @Schema(description = "공지사항 제목", example = "업데이트된 공지사항")
    private String notice;        // 공지사항 텍스트
    @Schema(description = "설명글", example = "채팅방 설명이 업데이트되었습니다.")
    private String description;   // 설명
    @Schema(description = "해시태그 목록", example = "['한식', '맛집']")
    private List<String> hashtags; // 해시태그 목록
}
