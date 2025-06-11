package com.test.foodtrip.domain.chat.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@Schema(description = "채팅방 목록 페이지 응답 DTO")
public class ChatRoomListPageResponseDTO {
    @Schema(description = "채팅방 목록")
    private List<ChatRoomListResponseDTO> rooms;
    @Schema(description = "다음 페이지 존재 여부", example = "true")
    private boolean hasMore;
}
