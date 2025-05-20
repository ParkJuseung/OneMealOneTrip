package com.test.foodtrip.domain.chat.controller;

import com.test.foodtrip.domain.chat.dto.ChatRoomCreateRequestDTO;
import com.test.foodtrip.domain.chat.dto.ChatRoomDetailResponseDTO;
import com.test.foodtrip.domain.chat.dto.ChatRoomEditRequestDTO;
import com.test.foodtrip.domain.chat.dto.ChatRoomListResponseDTO;
import com.test.foodtrip.domain.chat.service.ChatRoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chatrooms")
public class ChatRoomRestController {

    private final ChatRoomService chatRoomService;

    // 전체 채팅방 목록 조회
    @GetMapping
    public List<ChatRoomListResponseDTO> getAllRooms() {
        Long currentUserId = 999L; // ✅ 임시 사용자 ID 하드코딩
        return chatRoomService.getAllRooms(currentUserId);
    }

    // 채팅방 생성
    @PostMapping
    public Long createRoom(@RequestBody ChatRoomCreateRequestDTO dto) {
        Long currentUserId = 999L; // ✅ 임시 사용자 ID 하드코딩
        return chatRoomService.createChatRoom(dto, currentUserId);
    }

    // 채팅방 상세 조회
    @GetMapping("/{id}")
    public ChatRoomDetailResponseDTO getRoomDetail(@PathVariable Long id) {
        Long currentUserId = 999L; // ✅ 임시 사용자 ID 하드코딩
        return chatRoomService.getRoomDetail(id, currentUserId);
    }

    // 채팅방 삭제(논리삭제)
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteRoom(@PathVariable Long id) {
        Long userId = 999L; // 테스트용 사용자
        try {
            chatRoomService.deleteRoom(id, userId);
            return ResponseEntity.ok().build();
        } catch (IllegalStateException | IllegalArgumentException e) {
            return ResponseEntity.status(403).body(e.getMessage());
        }
    }

    // 채팅방 상세내용 수정(논리삭제)
    @PostMapping("/{id}/edit-log")
    public ResponseEntity<?> editChatroom(@PathVariable Long id,
                                          @RequestBody ChatRoomEditRequestDTO dto) {
        Long userId = 999L; // 하드코딩된 mock 사용자 ID
        chatRoomService.editRoom(id, dto, userId);
        return ResponseEntity.ok().build();
    }

}


