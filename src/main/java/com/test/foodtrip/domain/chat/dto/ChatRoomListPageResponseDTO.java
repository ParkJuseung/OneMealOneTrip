package com.test.foodtrip.domain.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class ChatRoomListPageResponseDTO {
    private List<ChatRoomListResponseDTO> rooms;
    private boolean hasMore;
}
