package com.test.foodtrip.domain.chat.dto;

import java.util.List;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ChatMessageGroupedResponseDTO {
    private List<ChatMessageResponseDTO> beforeMessages;
    private List<ChatMessageResponseDTO> afterMessages;
    private Long lastReadMessageId;
}