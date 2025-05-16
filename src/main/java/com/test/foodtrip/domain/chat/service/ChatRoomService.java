package com.test.foodtrip.domain.chat.service;

import com.test.foodtrip.domain.chat.dto.ChatRoomListResponseDto;
import com.test.foodtrip.domain.chat.entity.ChatRoom;
import com.test.foodtrip.domain.chat.repository.ChatRoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;
    

    // 채팅방 전체 목록 조회
    public List<ChatRoomListResponseDto> getAllRooms() {
        List<ChatRoom> chatRooms = chatRoomRepository.findAll();
        
     // ✅ 여기서 로그 찍기
        System.out.println("채팅방 개수: " + chatRooms.size());

        return chatRooms.stream()
                .map(room -> ChatRoomListResponseDto.builder()
                        .id(room.getId())
                        .title(room.getTitle())
                        .thumbnailImageUrl(room.getThumbnailImageUrl())
                        .createdAt(room.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
    }
}
