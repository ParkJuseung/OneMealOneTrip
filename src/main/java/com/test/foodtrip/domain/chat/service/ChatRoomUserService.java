package com.test.foodtrip.domain.chat.service;

import com.test.foodtrip.domain.chat.entity.ChatRoom;
import com.test.foodtrip.domain.chat.entity.ChatroomUser;
import com.test.foodtrip.domain.chat.repository.ChatMessageRepository;
import com.test.foodtrip.domain.chat.repository.ChatRoomRepository;
import com.test.foodtrip.domain.chat.repository.ChatroomUserRepository;
import com.test.foodtrip.domain.user.entity.User;
import com.test.foodtrip.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatRoomUserService {

    private final ChatroomUserRepository chatroomUserRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final UserRepository userRepository;
    private final ChatMessageRepository chatMessageRepository;

    /**
     * 사용자가 채팅방에 입장할 때 호출되는 메서드
     * - 처음 입장하면 사용자를 INSERT 한다.
     * - 이미 채팅방에 참여한 기록이 있으면 rejoin() 호출
     * - 마지막 메시지 ID를 lastReadMessageId에 저장하여 읽음 처리
     */
    @Transactional
    public void joinChatRoom(Long chatRoomId, Long userId) {
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new RuntimeException("채팅방이 존재하지 않습니다."));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("유저가 존재하지 않습니다."));

        ChatroomUser entry = chatroomUserRepository
                .findByChatRoom_IdAndUser_Id(chatRoomId, userId)
                .orElse(null);

        Long lastMessageId = chatMessageRepository.findLastMessageIdByChatRoomId(chatRoomId);

        if (entry == null) {
            // 처음 참여하는 경우
            String role = chatRoom.getUser().getId().equals(user.getId()) ? "OWNER" : "MEMBER";

            ChatroomUser newUser = ChatroomUser.builder()
                    .chatRoom(chatRoom)
                    .user(user)
                    .role(role)
                    .status("JOINED")
                    .joinedAt(LocalDateTime.now())
                    .statusUpdatedAt(LocalDateTime.now())
                    .lastReadMessageId(lastMessageId)
                    .build();
            chatroomUserRepository.save(newUser);
        } else {
            // 재입장 시 도메인 메서드 호출
            entry.rejoin(lastMessageId);
        }
    }

    /**
     * 사용자가 채팅방을 나가는 경우 상태를 LEFT로 변경
     */
    @Transactional
    public void leaveChatRoom(Long chatRoomId, Long userId) {
        ChatroomUser chatUser = chatroomUserRepository
                .findByChatRoom_IdAndUser_Id(chatRoomId, userId)
                .orElseThrow(() -> new RuntimeException("참여 기록이 없습니다."));

        chatUser.leave();
    }

    /**
     * 채팅방 삭제 시 모든 참여자의 상태를 LEFT로 변경
     */
    @Transactional
    public void markAllUsersLeft(Long chatRoomId) {
        List<ChatroomUser> users = chatroomUserRepository.findByChatRoom_Id(chatRoomId);

        for (ChatroomUser user : users) {
            user.leave();
        }
    }
}
