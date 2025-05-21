package com.test.foodtrip.domain.chat.service;

import com.test.foodtrip.domain.chat.dto.ChatroomEnterRequestDTO;
import com.test.foodtrip.domain.chat.entity.ChatroomUser;
import com.test.foodtrip.domain.chat.entity.ChatRoom;
import com.test.foodtrip.domain.chat.repository.ChatroomUserRepository;
import com.test.foodtrip.domain.chat.repository.ChatRoomRepository;
import com.test.foodtrip.domain.user.entity.User;
import com.test.foodtrip.domain.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;


import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@org.springframework.transaction.annotation.Transactional
public class ChatroomUserServiceTest {

    @Autowired
    ChatroomUserService chatroomUserService;

    @Autowired
    ChatRoomRepository chatRoomRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    ChatroomUserRepository chatroomUserRepository;

    @Test
    @DisplayName("채팅방 입장 시 ChatroomUser가 저장된다")
    void testEnterChatroom() {
        // given
        User user = userRepository.save(User.builder()
        		.name("테스트유저")
        		.socialType("KAKAO")
        		.socialEmail("testuser@kakao.com")
        		.nickname("테스트")
        		.gender("test")
        		.build());
        ChatRoom room = chatRoomRepository.save(ChatRoom.builder().title("테스트방").build());

        ChatroomEnterRequestDTO dto = ChatroomEnterRequestDTO.builder()
                .userId(user.getId())
                .roomId(room.getId())
                .role("MEMBER")
                .profileImageUrl("http://example.com/test.jpg")
                .build();

        // when
        chatroomUserService.enterChatroom(dto);

        // then
        ChatroomUser saved = chatroomUserRepository.findAll().get(0);
        assertThat(saved.getUser().getId()).isEqualTo(user.getId());
        assertThat(saved.getChatRoom().getId()).isEqualTo(room.getId());
        assertThat(saved.getRole()).isEqualTo("MEMBER");
        assertThat(saved.getProfileImageUrl()).isEqualTo("http://example.com/test.jpg");
    }
}
