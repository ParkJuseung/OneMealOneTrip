package com.test.foodtrip.domain.chat.service;

import com.test.foodtrip.domain.chat.dto.ChatroomEnterRequestDTO;
import com.test.foodtrip.domain.chat.entity.ChatroomUser;
import com.test.foodtrip.domain.chat.entity.ChatRoom;
import com.test.foodtrip.domain.chat.repository.ChatroomUserRepository;
import com.test.foodtrip.domain.chat.repository.ChatRoomRepository;
import com.test.foodtrip.domain.user.entity.User;
import com.test.foodtrip.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ChatroomUserService {

    private final ChatroomUserRepository chatroomUserRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final UserRepository userRepository;

    // DTO를 기반으로 채팅방 입력 로직 처리
    public void enterChatroom(ChatroomEnterRequestDTO dto) {
        // 1. 채팅방과 사용자 조회
        ChatRoom chatRoom = chatRoomRepository.findById(dto.getRoomId())
                .orElseThrow(() -> new IllegalArgumentException("채팅방이 존재하지 않습니다."));

        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("사용자가 존재하지 않습니다."));

        // 2. ChatroomUser 엔티티 생성
        ChatroomUser chatroomUser = ChatroomUser.builder()
                .chatRoom(chatRoom)
                .user(user)
                .role(dto.getRole() != null ? dto.getRole() : "MEMBER")
                .profileImageUrl(dto.getProfileImageUrl())
                .joinedAt(LocalDateTime.now())	// 입장 시간
                .status("JOINED")				// 기본 상태
                .statusUpdatedAt(LocalDateTime.now()) // 상태 변경
                .build();

        // 3. DB에 저장
        chatroomUserRepository.save(chatroomUser);
    }
}
