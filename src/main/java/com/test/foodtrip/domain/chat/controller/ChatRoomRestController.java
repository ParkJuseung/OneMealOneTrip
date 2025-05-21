package com.test.foodtrip.domain.chat.controller;

import com.test.foodtrip.domain.chat.dto.ChatRoomCreateRequestDTO;
import com.test.foodtrip.domain.chat.dto.ChatRoomDetailResponseDTO;
import com.test.foodtrip.domain.chat.dto.ChatRoomEditRequestDTO;
import com.test.foodtrip.domain.chat.dto.ChatRoomListResponseDTO;
import com.test.foodtrip.domain.chat.service.ChatRoomService;
import com.test.foodtrip.domain.user.entity.User;
import com.test.foodtrip.domain.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chatrooms")
public class ChatRoomRestController {

    private final ChatRoomService chatRoomService;
    private final UserRepository userRepository;

    // ✅ 현재 로그인한 유저의 userId 가져오기
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String email = oAuth2User.getAttribute("email");

        User user = userRepository.findBySocialEmail(email)
                .orElseThrow(() -> new RuntimeException("로그인된 유저 정보를 찾을 수 없습니다."));

        return user.getId();
    }

    // 전체 채팅방 목록 조회
    @GetMapping
    public List<ChatRoomListResponseDTO> getAllRooms() {
        return chatRoomService.getAllRooms(getCurrentUserId());
    }

    // 채팅방 생성
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Long createRoom(@ModelAttribute ChatRoomCreateRequestDTO dto) {
        return chatRoomService.createChatRoom(dto, getCurrentUserId());
    }

    // 채팅방 상세 조회
    @GetMapping("/{id}")
    public ChatRoomDetailResponseDTO getRoomDetail(@PathVariable("id") Long id) {
        return chatRoomService.getRoomDetail(id, getCurrentUserId());
    }

    // 채팅방 삭제(논리삭제)
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteRoom(@PathVariable("id") Long id) {
        try {
            chatRoomService.deleteRoom(id, getCurrentUserId());
            return ResponseEntity.ok().build();
        } catch (IllegalStateException | IllegalArgumentException e) {
            return ResponseEntity.status(403).body(e.getMessage());
        }
    }

    // 채팅방 상세내용 수정
    @PostMapping("/{id}/edit-log")
    public ResponseEntity<?> editChatroom(@PathVariable("id") Long id,
                                          @RequestParam("title") String title,
                                          @RequestParam(value = "notice", required = false) String notice,
                                          @RequestParam(value = "description", required = false) String description,
                                          @RequestParam(value = "hashtags", required = false) List<String> hashtags,
                                          @RequestParam(value = "thumbnailImage", required = false) MultipartFile thumbnailImage) {

        ChatRoomEditRequestDTO dto = ChatRoomEditRequestDTO.builder()
                .title(title)
                .notice(notice)
                .description(description)
                .hashtags(hashtags != null ? hashtags : List.of())
                .build();

        chatRoomService.editRoom(id, dto, thumbnailImage, getCurrentUserId());
        return ResponseEntity.ok().build();
    }

}
