package com.test.foodtrip.domain.chat.repository;

import com.test.foodtrip.domain.chat.dto.ChatRoomListResponseDTO;

import java.util.List;

public interface ChatRoomRepositoryCustom {
    List<ChatRoomListResponseDTO> findAllRooms(int offset, int limit, Long userId);
    List<ChatRoomListResponseDTO> findPopularRooms(int offset, int limit);
    List<ChatRoomListResponseDTO> findMyRooms(Long userId, int offset, int limit);
}
