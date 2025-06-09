package com.test.foodtrip.domain.chat.service;

import com.test.foodtrip.common.aws.S3Service;
import com.test.foodtrip.domain.chat.dto.*;
import com.test.foodtrip.domain.chat.entity.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import com.test.foodtrip.domain.chat.repository.ChatRoomRepository;
import com.test.foodtrip.domain.chat.repository.ChatroomLikeRepository;
import com.test.foodtrip.domain.chat.repository.ChatroomNoticeRepository;
import com.test.foodtrip.domain.chat.repository.ChatroomUserRepository;
import com.test.foodtrip.domain.chat.repository.HashtagRepository;
import com.test.foodtrip.domain.user.entity.User;
import com.test.foodtrip.domain.user.repository.UserRepository;
import com.test.foodtrip.domain.chat.repository.ChatRoomHashtagRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatRoomService {

    private final S3Service s3Service;
    private final FileUploadService fileUploadService;

    private final ChatRoomRepository chatRoomRepository;
    private final HashtagRepository hashtagRepository;
    private final ChatroomUserRepository chatroomUserRepository;
    private final ChatroomNoticeRepository chatroomNoticeRepository;
    private final ChatRoomHashtagRepository chatRoomHashtagRepository;
    private final ChatroomLikeRepository chatroomLikeRepository;
    private final UserRepository userRepository;

    private static final String DEFAULT_THUMBNAIL_URL = "/images/chat/default-thumbnail-100x100.png";



    @PersistenceContext
    private EntityManager entityManager;

    private List<ChatRoomListResponseDTO> mapRoomsToDTOs(List<ChatRoom> rooms, Long userId) {
        User currentUser = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자 정보를 찾을 수 없습니다."));

        return rooms.stream()
                .map(room -> {
                    List<String> hashtags = chatRoomHashtagRepository.findByChatRoomId(room.getId())
                            .stream()
                            .map(link -> link.getHashtag().getTagText())
                            .toList();

                    int likeCount = chatroomLikeRepository.countByChatRoom_IdAndIsActive(room.getId(), "Y").intValue();

                    int participantCount = chatroomUserRepository.countByChatRoomId(room.getId());
                    String ownerNickname = chatroomUserRepository
                            .findByChatRoomIdAndRole(room.getId(), "OWNER")
                            .map(u -> u.getUser().getNickname())
                            .orElse("알수없음");

                    boolean liked = chatroomLikeRepository.findByChatRoomAndUser(room, currentUser)
                            .map(like -> "Y".equals(like.getIsActive()))
                            .orElse(false);

                    return ChatRoomListResponseDTO.builder()
                            .id(room.getId())
                            .title(room.getTitle())
                            .thumbnailImageUrl(room.getThumbnailImageUrl())
                            .createdAt(room.getCreatedAt())
                            .hashtags(hashtags)
                            .likeCount(likeCount)
                            .participantCount(participantCount)
                            .ownerNickname(ownerNickname)
                            .liked(liked)
                            .build();
                })
                .toList();
    }



    //채팅방 생성 처리
    @Transactional
    public Long createChatRoom(ChatRoomCreateRequestDTO dto, Long currentUserId) {
    	 //  userId를 바탕으로 User 객체 조회
    	User user = userRepository.findById(currentUserId)
    		    .orElseThrow(() -> new RuntimeException("유저를 찾을 수 없습니다."));

        // 1. 썸네일 처리
        String thumbnailUrl = null;
        MultipartFile thumbnailFile = dto.getThumbnailImage();
        if (thumbnailFile != null && !thumbnailFile.isEmpty()) {
            if (!fileUploadService.isImageFile(thumbnailFile)) {
                throw new IllegalArgumentException("업로드 실패: 지원하지 않는 파일 형식입니다. jpg 또는 png만 허용됩니다.");
            }
            try {
                thumbnailUrl = s3Service.upload(thumbnailFile, "chatroom");
            } catch (IOException e) {
                throw new RuntimeException("S3 업로드 중 오류 발생", e);
            }
        }

        // 2. 채팅방 생성
        ChatRoom chatRoom = ChatRoom.builder()
                .title(dto.getTitle())
                .user(user)
                .thumbnailImageUrl(thumbnailUrl)
                .build();
        chatRoom = chatRoomRepository.save(chatRoom);
        chatRoomRepository.flush();
        System.out.println("저장된 ChatRoom ID: " + chatRoom.getId()); // 로그 추가

        // 3. 해시태그 처리 (null 체크 포함)
        List<String> tags = dto.getHashtags();
        if (tags == null) tags = List.of(); // null 방지

        List<Hashtag> hashtags = tags.stream()
                .map(tag -> hashtagRepository.findByTagText(tag)
                        .orElseGet(() -> hashtagRepository.save(
                                Hashtag.builder().tagText(tag).build())))
                .toList();

        for (Hashtag tag : hashtags) {
            System.out.println("💡 해시태그 ID: " + tag.getId() + ", 텍스트: " + tag.getTagText());

            ChatRoomHashtag rel = ChatRoomHashtag.of(chatRoom, tag);
            System.out.println("🔗 연결 시도: chatRoomId=" + chatRoom.getId() + ", hashtagId=" + tag.getId());

            chatRoomHashtagRepository.save(rel);
        }

        // 4. 공지사항/설명 히스토리 저장
        boolean hasNotice = dto.getNotice() != null && !dto.getNotice().isBlank();
        boolean hasDescription = dto.getDescription() != null && !dto.getDescription().isBlank();

        if (hasNotice || hasDescription) {
            ChatroomNoticeHistory notice = ChatroomNoticeHistory.builder()
                    .chatRoom(chatRoom)
                    .content(dto.getNotice())
                    .description(dto.getDescription())
                    .createdAt(LocalDateTime.now())
                    .build();
            chatroomNoticeRepository.save(notice);
        }

        // 5. 참여자 정보 저장 (방장 등록)
        User currentUser = entityManager.getReference(User.class, currentUserId);

        ChatroomUser chatroomUser = ChatroomUser.builder()
                .chatRoom(chatRoom)
                .user(currentUser)
                .role("OWNER")
                .status("JOINED")
                .joinedAt(LocalDateTime.now())
                .statusUpdatedAt(LocalDateTime.now())
                .build();
        chatroomUserRepository.save(chatroomUser);

        return chatRoom.getId();
    }

    // 채팅방 상세 조회
    @Transactional(readOnly = true)
    public ChatRoomDetailResponseDTO getRoomDetail(Long id, Long currentUserId) {
        ChatRoom chatRoom = chatRoomRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("채팅방이 존재하지 않습니다. ID: " + id));

        // 최신 공지사항 (nullable)
        ChatroomNoticeHistory latestNotice = chatroomNoticeRepository
                .findTopByChatRoomOrderByCreatedAtDesc(chatRoom)
                .orElse(null);

        // 현재 사용자의 참여 여부 및 역할 확인
        ChatroomUser myMembership = chatroomUserRepository
                .findByChatRoomIdAndUserId(chatRoom.getId(), currentUserId)
                .orElse(null);

        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new IllegalArgumentException("사용자 정보가 없습니다."));

        boolean liked = chatroomLikeRepository.findByChatRoomAndUser(chatRoom, currentUser)
                .map(like -> "Y".equals(like.getIsActive()))
                .orElse(false);

        // ❗ 여기 수정됨: 기본값을 "OWNER" → null 로 변경
        String role = (myMembership != null) ? myMembership.getRole() : null;

        // 해시태그 추출
        List<ChatRoomHashtag> hashtagLinks = chatRoomHashtagRepository.findByChatRoomId(chatRoom.getId());
        List<String> hashtags = hashtagLinks.stream()
                .map(link -> link.getHashtag().getTagText())
                .collect(Collectors.toList());

        // 추가 정보
        int likeCount = chatroomLikeRepository.countByChatRoom_Id(id);
        int participantCount = chatroomUserRepository.countByChatRoomId(id);
        String ownerNickname = chatroomUserRepository
                .findByChatRoomIdAndRole(id, "OWNER")
                .map(user -> user.getUser().getNickname())
                .orElse("알수없음");

        return ChatRoomDetailResponseDTO.builder()
                .id(chatRoom.getId())
                .title(chatRoom.getTitle())
                .thumbnailImageUrl(chatRoom.getThumbnailImageUrl())
                .createdAt(chatRoom.getCreatedAt())
                .notice(latestNotice != null ? latestNotice.getContent() : null)
                .description(latestNotice != null ? latestNotice.getDescription() : null)
                .hashtags(hashtags)
                .myRole(role) // null이면 수정/삭제 버튼 안 보이게 처리
                .likeCount(likeCount)
                .participantCount(participantCount)
                .ownerNickname(ownerNickname)
                .liked(liked)
                .build();
    }

    @Transactional
    public void editRoom(Long roomId, ChatRoomEditRequestDTO dto, MultipartFile thumbnailImage, boolean resetThumbnail, Long currentUserId) {
        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("채팅방이 존재하지 않습니다. ID: " + roomId));

        // 채팅방 제목 변경 (방장만 가능하게 나중에 제어 가능)
        chatRoom.updateTitle(dto.getTitle());

        // 1. 기존 hashtag 관계 제거
        chatRoomHashtagRepository.deleteByChatRoomId(roomId);

        // 2. 새로운 태그 생성 및 저장
        List<Hashtag> newTags = dto.getHashtags().stream()
                .map(tag -> hashtagRepository.findByTagText(tag)
                        .orElseGet(() -> hashtagRepository.save(
                                Hashtag.builder().tagText(tag).build())))
                .toList();

        // 3. 조인 테이블 직접 삽입
        for (Hashtag tag : newTags) {
            ChatRoomHashtag rel = ChatRoomHashtag.of(chatRoom, tag);
            chatRoomHashtagRepository.save(rel);
        }


        // 공지사항/설명 히스토리 새로 저장
        boolean hasNotice = dto.getNotice() != null && !dto.getNotice().isBlank();
        boolean hasDescription = dto.getDescription() != null && !dto.getDescription().isBlank();

        if (hasNotice || hasDescription) {
            ChatroomNoticeHistory history = ChatroomNoticeHistory.builder()
                    .chatRoom(chatRoom)
                    .content(dto.getNotice())
                    .description(dto.getDescription())
                    .createdAt(LocalDateTime.now())
                    .build();
            chatroomNoticeRepository.save(history);
            chatroomNoticeRepository.flush();
        }

        // 썸네일 이미지 처리
        if (resetThumbnail) {
            chatRoom.updateThumbnailImageUrl(DEFAULT_THUMBNAIL_URL);
        } else if (thumbnailImage != null && !thumbnailImage.isEmpty()) {
            if (!fileUploadService.isImageFile(thumbnailImage)) {
                throw new IllegalArgumentException("업로드 실패: 지원하지 않는 파일 형식입니다. jpg 또는 png만 허용됩니다.");
            }
            try {
                String newUrl = s3Service.upload(thumbnailImage, "chatroom");
                chatRoom.updateThumbnailImageUrl(newUrl);
            } catch (IOException e) {
                throw new RuntimeException("S3 업로드 중 오류 발생", e);
            }
        }
    }

    // 채팅방 삭제
    public void deleteRoom(Long chatRoomId, Long userId) {
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new IllegalArgumentException("채팅방을 찾을 수 없습니다."));

        ChatroomUser user = chatroomUserRepository.findByChatRoomIdAndUserId(chatRoomId, userId)
                .orElseThrow(() -> new IllegalStateException("해당 채팅방에 속한 사용자가 아닙니다."));

        if (!user.getRole().equals("OWNER")) {
            throw new IllegalStateException("삭제 권한이 없습니다.");
        }

        chatRoom.markAsDeleted(); // isDeleted = 'Y'
        chatRoomRepository.save(chatRoom);
    }

    @Transactional
    public Map<String, Object> toggleLike(Long chatRoomId, User user) {
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new IllegalArgumentException("채팅방을 찾을 수 없습니다."));

        boolean liked;

        Optional<ChatroomLike> existingOpt = chatroomLikeRepository.findByChatRoomAndUser(chatRoom, user);

        if (existingOpt.isPresent()) {
            ChatroomLike existing = existingOpt.get();
            boolean isNowLiked = "N".equals(existing.getIsActive());
            existing.setIsActive(isNowLiked ? "Y" : "N");
            chatroomLikeRepository.save(existing);
            liked = isNowLiked;
        } else {
            ChatroomLike newLike = ChatroomLike.builder()
                    .chatRoom(chatRoom)
                    .user(user)
                    .isActive("Y")
                    .build();
            chatroomLikeRepository.save(newLike);
            liked = true;  // ✅ 새로 누른 경우는 무조건 true
        }

        Long likeCount = chatroomLikeRepository.countByChatRoom_IdAndIsActive(chatRoomId, "Y");

        Map<String, Object> result = new HashMap<>();
        result.put("liked", liked);
        result.put("likeCount", likeCount);

        return result;
    }


    //좋아요 수 가져오기
    @Transactional(readOnly = true)
    public Long getLikeCount(Long chatRoomId) {
        return chatroomLikeRepository.countByChatRoom_IdAndIsActive(chatRoomId, "Y");
    }


    // 전체 채팅방
    public ChatRoomListPageResponseDTO getAllRoomsWithPagination(int offset, int limit, Long userId, String keyword) {
        List<ChatRoom> chatRooms = chatRoomRepository.findByIsDeletedOrderByCreatedAtDesc("N");

        // 🔍 검색 필터링
        if (keyword != null && !keyword.isBlank()) {
            String lowerKeyword = keyword.toLowerCase();
            chatRooms = chatRooms.stream()
                    .filter(room -> {
                        String title = room.getTitle().toLowerCase();
                        String ownerNickname = chatroomUserRepository
                                .findByChatRoomIdAndRole(room.getId(), "OWNER")
                                .map(user -> user.getUser().getNickname().toLowerCase())
                                .orElse("");

                        List<String> hashtags = chatRoomHashtagRepository.findByChatRoomId(room.getId())
                                .stream()
                                .map(link -> link.getHashtag().getTagText().toLowerCase())
                                .toList();

                        return title.contains(lowerKeyword)
                                || ownerNickname.contains(lowerKeyword)
                                || hashtags.stream().anyMatch(tag -> tag.contains(lowerKeyword));
                    })
                    .toList();
        }

        List<ChatRoom> paged = chatRooms.stream().skip(offset).limit(limit).toList();
        List<ChatRoomListResponseDTO> rooms = mapRoomsToDTOs(paged, userId);
        boolean hasMore = chatRooms.size() > (offset + rooms.size());

        return ChatRoomListPageResponseDTO.builder()
                .rooms(rooms)
                .hasMore(hasMore)
                .build();
    }


    // 인기 채팅방
    public ChatRoomListPageResponseDTO getPopularRoomsWithPagination(int offset, int limit, Long userId, String keyword) {
        List<ChatRoom> allRooms = chatRoomRepository.findByIsDeleted("N");

        // keyword 필터링
        if (keyword != null && !keyword.isBlank()) {
            String lowerKeyword = keyword.toLowerCase();
            allRooms = allRooms.stream()
                    .filter(room -> {
                        String title = room.getTitle().toLowerCase();
                        String ownerNickname = chatroomUserRepository
                                .findByChatRoomIdAndRole(room.getId(), "OWNER")
                                .map(user -> user.getUser().getNickname().toLowerCase())
                                .orElse("");

                        List<String> hashtags = chatRoomHashtagRepository.findByChatRoomId(room.getId())
                                .stream()
                                .map(link -> link.getHashtag().getTagText().toLowerCase())
                                .toList();

                        return title.contains(lowerKeyword)
                                || ownerNickname.contains(lowerKeyword)
                                || hashtags.stream().anyMatch(tag -> tag.contains(lowerKeyword));
                    })
                    .collect(Collectors.toList());
        }

        // 좋아요순 + 최근순 정렬
        List<ChatRoom> sorted = allRooms.stream()
                .sorted((a, b) -> {
                    int likeDiff = chatroomLikeRepository.countByChatRoom_Id(b.getId()) -
                            chatroomLikeRepository.countByChatRoom_Id(a.getId());
                    return (likeDiff != 0) ? likeDiff : b.getCreatedAt().compareTo(a.getCreatedAt());
                })
                .toList();

        List<ChatRoom> paged = sorted.stream().skip(offset).limit(limit).toList();
        List<ChatRoomListResponseDTO> rooms = mapRoomsToDTOs(paged, userId);
        boolean hasMore = sorted.size() > (offset + rooms.size());

        return ChatRoomListPageResponseDTO.builder()
                .rooms(rooms)
                .hasMore(hasMore)
                .build();
    }



    // 나의 채팅방
    @Transactional(readOnly = true)
    public ChatRoomListPageResponseDTO getMyRoomsWithPagination(Long userId, int offset, int limit, String keyword) {
        List<ChatroomUser> memberships = chatroomUserRepository.findByUserIdAndStatus(userId, "JOINED");

        // 유효한 채팅방만 필터링
        List<ChatRoom> myRooms = memberships.stream()
                .map(ChatroomUser::getChatRoom)
                .filter(room -> "N".equals(room.getIsDeleted()))
                .toList();

        // keyword 필터링
        if (keyword != null && !keyword.isBlank()) {
            String lowerKeyword = keyword.toLowerCase();
            myRooms = myRooms.stream()
                    .filter(room -> {
                        String title = room.getTitle().toLowerCase();
                        String ownerNickname = chatroomUserRepository
                                .findByChatRoomIdAndRole(room.getId(), "OWNER")
                                .map(user -> user.getUser().getNickname().toLowerCase())
                                .orElse("");

                        List<String> hashtags = chatRoomHashtagRepository.findByChatRoomId(room.getId())
                                .stream()
                                .map(link -> link.getHashtag().getTagText().toLowerCase())
                                .toList();

                        return title.contains(lowerKeyword)
                                || ownerNickname.contains(lowerKeyword)
                                || hashtags.stream().anyMatch(tag -> tag.contains(lowerKeyword));
                    })
                    .toList();
        }

        List<ChatRoom> paged = myRooms.stream()
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .skip(offset)
                .limit(limit)
                .toList();

        List<ChatRoomListResponseDTO> rooms = mapRoomsToDTOs(paged, userId);
        boolean hasMore = myRooms.size() > (offset + rooms.size());

        return ChatRoomListPageResponseDTO.builder()
                .rooms(rooms)
                .hasMore(hasMore)
                .build();
    }


    // 채팅방 나가기
    @Transactional
    public void exitChatRoom(Long chatRoomId, Long userId) {
        ChatroomUser user = chatroomUserRepository
            .findByChatRoomIdAndUserId(chatRoomId, userId)
            .orElseThrow(() -> new IllegalArgumentException("참여 기록이 없습니다."));

        user.leave(); // status → 'LEFT', left_at, status_updated_at 기록
    }


}
