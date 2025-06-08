package com.test.foodtrip.domain.chat.controller;

import com.test.foodtrip.common.aws.S3Service;
import com.test.foodtrip.domain.chat.dto.*;
import com.test.foodtrip.domain.chat.service.ChatRoomService;
import com.test.foodtrip.domain.user.entity.User;
import com.test.foodtrip.domain.user.repository.UserRepository;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
    @Operation(
            summary = "전체 채팅방 목록 조회",
            description = "페이징 정보와 검색 키워드를 바탕으로 전체 채팅방 목록을 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패")
    })
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
    @Operation(
            summary = "인기 채팅방 목록 조회",
            description = "좋아요 수 등 인기 기준에 따라 채팅방 목록을 페이징 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패")
    })
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
    @Operation(
            summary = "내가 참여 중인 채팅방 목록 조회",
            description = "현재 로그인한 사용자가 참여한 채팅방 목록을 페이징하여 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패")
    })
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
    @Operation(
            summary = "채팅방 생성",
            description = "채팅방 제목, 설명, 해시태그, 공지사항, 썸네일 이미지를 포함해 새로운 채팅방을 생성합니다.\n\n" +
                    "multipart/form-data 형식으로 데이터를 전송해야 하며, 이미지 파일은 thumbnailImage 필드로 첨부됩니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "생성된 채팅방 ID 반환"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "500", description = "서버 오류 또는 S3 업로드 실패")
    })
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


    @Operation(
            summary = "채팅방 좋아요 토글",
            description = "채팅방에 좋아요 또는 좋아요 취소를 수행합니다. 로그인된 사용자의 ID는 내부에서 추출됩니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "현재 좋아요 상태 및 총 좋아요 수 반환"),
            @ApiResponse(responseCode = "404", description = "사용자 또는 채팅방을 찾을 수 없음"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @PostMapping("/{chatRoomId}/like")
    public ResponseEntity<Map<String, Object>> toggleLike(@PathVariable Long chatRoomId) {
        User user = userRepository.findById(getCurrentUserId())
                .orElseThrow(() -> new RuntimeException("사용자 정보를 찾을 수 없습니다."));

        // ✅ 좋아요 상태 + 수가 포함된 Map 반환
        Map<String, Object> result = chatRoomService.toggleLike(chatRoomId, user);

        return ResponseEntity.ok(result);
    }

    // 채팅방 상세 조회
    @Operation(
            summary = "채팅방 상세 조회",
            description = "채팅방 ID를 이용하여 채팅방의 상세 정보를 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "채팅방을 찾을 수 없음"),
            @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @GetMapping("/{id}")
    public ChatRoomDetailResponseDTO getRoomDetail(@PathVariable("id") Long id) {
        return chatRoomService.getRoomDetail(id, getCurrentUserId());
    }

    // 채팅방 삭제(논리삭제)
    @Operation(
            summary = "채팅방 삭제 (논리 삭제)",
            description = "채팅방 ID를 기반으로 해당 채팅방을 논리적으로 삭제합니다. 방장만 삭제할 수 있습니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "삭제 성공"),
            @ApiResponse(responseCode = "403", description = "삭제 권한 없음 또는 상태 불일치"),
            @ApiResponse(responseCode = "404", description = "채팅방을 찾을 수 없음")
    })
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
    @Operation(
            summary = "채팅방 정보 수정",
            description = "채팅방 제목, 공지사항, 설명, 해시태그, 썸네일 이미지를 수정합니다.\n" +
                    "multipart/form-data로 전송해야 하며, 썸네일 초기화를 원할 경우 resetThumbnail을 true로 지정하세요."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수정 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "403", description = "수정 권한 없음")
    })
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
