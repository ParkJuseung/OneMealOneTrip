package com.test.foodtrip.domain.chat.controller;

import com.test.foodtrip.common.aws.S3Service;
import com.test.foodtrip.domain.chat.dto.*;
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

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chatrooms")
public class ChatRoomRestController {

    private final ChatRoomService chatRoomService;
    private final UserRepository userRepository;
    private final S3Service s3Service;


    // 현재 로그인한 유저의 userId 가져오기
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
    public ChatRoomListPageResponseDTO getAllRooms(
            @RequestParam("offset") int offset,
            @RequestParam("limit") int limit,
            @RequestParam(value = "keyword", required = false) String keyword
    ) {
        Long userId = getCurrentUserId();
        return chatRoomService.getAllRoomsWithPagination(offset, limit, getCurrentUserId(), keyword);
    }

    // 인기 채팅방 목록 조회
    @GetMapping("/popular")
    public ChatRoomListPageResponseDTO getPopularRooms(
            @RequestParam("offset") int offset,
            @RequestParam("limit") int limit,
            @RequestParam(value = "keyword", required = false) String keyword
    ) {
        Long userId = getCurrentUserId();
        return chatRoomService.getPopularRoomsWithPagination(offset, limit, getCurrentUserId(), keyword);
    }

    // 내가 참여중인 채팅방 목록 조회
    @GetMapping("/mine")
    public ChatRoomListPageResponseDTO getMyRooms(
            @RequestParam("offset") int offset,
            @RequestParam("limit") int limit,
            @RequestParam(value = "keyword", required = false) String keyword
    ) {
        Long userId = getCurrentUserId();
        return chatRoomService.getMyRoomsWithPagination(userId, offset, limit, keyword);
    }

    // 채팅방 생성
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Long createRoom(@ModelAttribute ChatRoomCreateRequestDTO dto) {
        MultipartFile thumbnailImage = dto.getThumbnailImage();

        if (thumbnailImage != null && !thumbnailImage.isEmpty()) {
            try {
                String imageUrl = s3Service.upload(thumbnailImage, "chatroom");
                dto.setThumbnailImageUrl(imageUrl);
            } catch (IOException e) {
                throw new RuntimeException("S3 업로드 중 오류 발생", e);
            }
        }

        return chatRoomService.createChatRoom(dto, getCurrentUserId());
    }



    @PostMapping("/{chatRoomId}/like")
    public ResponseEntity<Map<String, Object>> toggleLike(@PathVariable Long chatRoomId) {
        User user = userRepository.findById(getCurrentUserId())
                .orElseThrow(() -> new RuntimeException("사용자 정보를 찾을 수 없습니다."));

        // ✅ 좋아요 상태 + 수가 포함된 Map 반환
        Map<String, Object> result = chatRoomService.toggleLike(chatRoomId, user);

        return ResponseEntity.ok(result);
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
                                          @RequestParam(value = "thumbnailImage", required = false) MultipartFile thumbnailImage,
                                          @RequestParam(value = "resetThumbnail", defaultValue = "false") boolean resetThumbnail) {

        ChatRoomEditRequestDTO dto = ChatRoomEditRequestDTO.builder()
                .title(title)
                .notice(notice)
                .description(description)
                .hashtags(hashtags != null ? hashtags : List.of())
                .build();

        chatRoomService.editRoom(id, dto, thumbnailImage, resetThumbnail, getCurrentUserId());

        return ResponseEntity.ok().build();
    }


}
