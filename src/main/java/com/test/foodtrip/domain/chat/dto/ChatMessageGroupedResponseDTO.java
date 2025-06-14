package com.test.foodtrip.domain.chat.dto;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "날짜 기준으로 구분된 채팅 메시지 목록 응답 DTO")
public class ChatMessageGroupedResponseDTO {
    @Schema(description = "이전 메시지 목록")
    private List<ChatMessageResponseDTO> beforeMessages;
    @Schema(description = "이후 메시지 목록")
    private List<ChatMessageResponseDTO> afterMessages;
    @Schema(description = "마지막 읽은 메시지 ID", example = "105")
    private Long lastReadMessageId;
}