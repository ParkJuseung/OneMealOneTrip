package com.test.foodtrip.domain.chat.service;

import com.test.foodtrip.domain.chat.dto.ChatRoomCreateRequestDTO;
import com.test.foodtrip.domain.chat.dto.ChatRoomEditRequestDTO;
import com.test.foodtrip.domain.chat.dto.ChatRoomListResponseDTO;
import com.test.foodtrip.domain.chat.dto.ChatRoomDetailResponseDTO;
import com.test.foodtrip.domain.chat.entity.ChatRoom;
import com.test.foodtrip.domain.chat.entity.ChatroomNoticeHistory;
import com.test.foodtrip.domain.chat.entity.ChatroomUser;
import com.test.foodtrip.domain.chat.entity.Hashtag;
import com.test.foodtrip.domain.chat.repository.ChatRoomRepository;
import com.test.foodtrip.domain.chat.repository.ChatroomNoticeRepository;
import com.test.foodtrip.domain.chat.repository.ChatroomUserRepository;
import com.test.foodtrip.domain.chat.repository.HashtagRepository;
import com.test.foodtrip.domain.user.entity.User;
import com.test.foodtrip.domain.chat.entity.ChatRoomHashtag;
import com.test.foodtrip.domain.chat.entity.ChatRoomHashtagId;
import com.test.foodtrip.domain.chat.repository.ChatRoomHashtagRepository;
// import com.test.foodtrip.domain.user.repository.UserRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.apache.ibatis.javassist.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;
    private final HashtagRepository hashtagRepository;
    private final ChatroomUserRepository chatroomUserRepository;
    private final ChatroomNoticeRepository chatroomNoticeRepository;
    private final ChatRoomHashtagRepository chatRoomHashtagRepository;

    @PersistenceContext
    private EntityManager entityManager;

    // private final UserRepository userRepository; // !! 통합 시 주석 해제

    private static final Object TEST_USER = new Object() {
        public Long getId() {
            return 999L;
        }

        public String getNickname() {
            return "테스트유저";
        }
    };

    //채팅방 전체 목록 조회
    public List<ChatRoomListResponseDTO> getAllRooms(Long currentUserId) {
        List<ChatRoom> chatRooms = chatRoomRepository.findByIsDeleted("N");

        System.out.println("채팅방 개수 (삭제 제외): " + chatRooms.size());

        return chatRooms.stream()
                .map(room -> {
                    // 각 방의 해시태그 조회
                    List<ChatRoomHashtag> hashtagLinks = chatRoomHashtagRepository.findByChatRoomId(room.getId());
                    List<String> hashtags = hashtagLinks.stream()
                            .map(link -> link.getHashtag().getTagText())
                            .collect(Collectors.toList());

                    return ChatRoomListResponseDTO.builder()
                            .id(room.getId())
                            .title(room.getTitle())
                            .thumbnailImageUrl(room.getThumbnailImageUrl())
                            .createdAt(room.getCreatedAt())
                            .hashtags(hashtags) // 실제 값으로 설정
                            .build();
                })
                .collect(Collectors.toList());
    }


    //채팅방 생성 처리
    @Transactional
    public Long createChatRoom(ChatRoomCreateRequestDTO dto, Long currentUserId) {
        //public Long createChatRoom(ChatRoomCreateRequestDTO dto) {
        ChatRoom chatRoom = ChatRoom.builder()
                .title(dto.getTitle())
                .thumbnailImageUrl(dto.getThumbnailImageUrl())
                .build();
        chatRoom = chatRoomRepository.save(chatRoom);
        chatRoomRepository.flush();

        List<Hashtag> hashtags = dto.getHashtags().stream()
                .map(tag -> hashtagRepository.findByTagText(tag)
                        .orElseGet(() -> hashtagRepository.save(
                                Hashtag.builder().tagText(tag).build())))
                .toList();
        for (Hashtag tag : hashtags) {
            ChatRoomHashtag rel = ChatRoomHashtag.builder()
                    .chatRoom(chatRoom)
                    .hashtag(tag)
                    .build();
            chatRoomHashtagRepository.save(rel);
        }

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

        User testUser = entityManager.getReference(User.class, 999L);

        ChatroomUser chatroomUser = ChatroomUser.builder()
                .chatRoom(chatRoom)
                .user(testUser)
                .role("OWNER")
                .status("JOINED")
                .joinedAt(LocalDateTime.now())
                .statusUpdatedAt(LocalDateTime.now())
                .build();
        chatroomUserRepository.save(chatroomUser);

        return chatRoom.getId();
    }

    //채팅방 상세 조회
    @Transactional(readOnly = true)
    //public ChatRoomDetailResponseDTO getRoomDetail(Long id) {
    public ChatRoomDetailResponseDTO getRoomDetail(Long id, Long currentUserId) {
        ChatRoom chatRoom = chatRoomRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("채팅방이 존재하지 않습니다. ID: " + id));

        ChatroomNoticeHistory latestNotice = chatroomNoticeRepository
                .findTopByChatRoomOrderByCreatedAtDesc(chatRoom)
                .orElse(null);


        // 사용자의 역할 조회
        ChatroomUser myMembership = chatroomUserRepository
                .findByChatRoomIdAndUserId(chatRoom.getId(), currentUserId)
                .orElse(null);

        String role = "OWNER";
        if (myMembership != null) {
            role = myMembership.getRole(); // "OWNER" 또는 "JOINED"
        }

        List<ChatRoomHashtag> hashtagLinks = chatRoomHashtagRepository.findByChatRoomId(chatRoom.getId());
        List<String> hashtags = hashtagLinks.stream()
                .map(link -> link.getHashtag().getTagText())
                .collect(Collectors.toList());

        return ChatRoomDetailResponseDTO.builder()
                .id(chatRoom.getId())
                .title(chatRoom.getTitle())
                .thumbnailImageUrl(chatRoom.getThumbnailImageUrl())
                .createdAt(chatRoom.getCreatedAt())
                .notice(latestNotice != null ? latestNotice.getContent() : null)
                .description(latestNotice != null ? latestNotice.getDescription() : null)
                .hashtags(hashtags)
                .myRole(role) // 역할 포함
                .build();
    }


    // 논리 삭제(DB상 실제 삭제가 아닌 사용자 접근제어 및 가림처리)
    @Transactional
    //public void deleteChatRoom(Long id) {
    public void deleteChatRoom(Long id, Long currentUserId) {
        ChatRoom chatRoom = chatRoomRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 채팅방이 존재하지 않습니다. ID: " + id));

        if ("Y".equals(chatRoom.getIsDeleted())) {
            throw new IllegalStateException("이미 삭제된 채팅방입니다.");
        }

        // 연관 해시태그 삭제
        chatRoomHashtagRepository.deleteByChatRoomId(id);

        // 논리 삭제 처리
        chatRoom.softDelete();
    }

    @Transactional
    public void editRoom(Long roomId, ChatRoomEditRequestDTO dto, Long currentUserId) {
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
            ChatRoomHashtag rel = ChatRoomHashtag.builder()
                    .chatRoom(chatRoom)
                    .hashtag(tag)
                    .build();
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
        }
    }

    // 채팅방 삭제
    public void deleteRoom(Long chatRoomId, Long userId) {
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new IllegalArgumentException("채팅방을 찾을 수 없습니다."));

        ChatroomUser user = chatroomUserRepository.findByChatRoomIdAndUserId(chatRoomId, userId)
                .orElseThrow(() -> new IllegalStateException("해당 채팅방에 속한 사용자가 아닙니다."));

        if (!user.getRole().equals("OWNER")) {
            throw new IllegalStateException("삭제 권한이 없습니다."); // ✅ 커스텀 예외 없이 처리
        }

        chatRoom.markAsDeleted(); // isDeleted = 'Y'
        chatRoomRepository.save(chatRoom);
    }

}
