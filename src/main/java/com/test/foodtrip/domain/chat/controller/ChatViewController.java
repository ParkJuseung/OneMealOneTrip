package com.test.foodtrip.domain.chat.controller;

import com.test.foodtrip.domain.chat.dto.ChatRoomListResponseDTO;
import com.test.foodtrip.domain.chat.dto.ChatRoomViewDTO;
import com.test.foodtrip.domain.chat.entity.ChatRoom;
import com.test.foodtrip.domain.chat.repository.ChatRoomRepository;
import com.test.foodtrip.domain.chat.service.ChatRoomService;
import com.test.foodtrip.domain.chat.service.ChatRoomUserService;
import com.test.foodtrip.domain.user.entity.User;
import com.test.foodtrip.domain.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;


@Controller
@RequiredArgsConstructor
public class ChatViewController {

    private final ChatRoomService chatRoomService;
    private final UserRepository userRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomUserService chatRoomUserService;
    
    @PostMapping("/chatroom/{id}/join")
    public ResponseEntity<Void> joinChatRoom(@PathVariable("id") Long chatRoomId,
                                             @AuthenticationPrincipal OAuth2User oauthUser) {
        // 1. 로그인한 사용자의 이메일로 유저 조회
        String email = oauthUser.getAttribute("email");
        User user = userRepository.findBySocialEmail(email)
            .orElseThrow(() -> new RuntimeException("유저가 존재하지 않습니다."));

        // 2. chatroom_user 테이블에 참여자 기록 INSERT or UPDATE
        chatRoomUserService.joinChatRoom(chatRoomId, user.getId());

        // 3. 바디 없이 200 OK 응답
        return ResponseEntity.ok().build();
    }

    // ✅ 로그인된 유저의 ID 조회
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String email = oAuth2User.getAttribute("email");

        User user = userRepository.findBySocialEmail(email)
                .orElseThrow(() -> new RuntimeException("로그인된 유저 정보를 찾을 수 없습니다."));
        
        return user.getId();
    }

    // ✅ 채팅방 목록 화면
    @GetMapping("/chat")
    public String chatRoomList(Model model) {
        model.addAttribute("currentUserId", getCurrentUserId());
        return "chat/chat-list";
    }


    // ✅ 채팅방 수정 폼 페이지
    @GetMapping("/chatroom/detailedit")
    public String showChatroomEditForm(@RequestParam Long id, @RequestParam(required = false) String mode, Model model) {
        Long currentUserId = getCurrentUserId();

        model.addAttribute("chatroomId", id);
        model.addAttribute("mode", mode);

        if ("edit".equals(mode)) {
            var dto = chatRoomService.getRoomDetail(id, currentUserId);
            model.addAttribute("room", dto);
        }

        return "chat/detailedit";
    }
    
    // 채팅방 입장 (내부) 페이지
    @GetMapping("/chatroom")
    public String enterChatRoom(@RequestParam("id") Long id,
                                @AuthenticationPrincipal OAuth2User oauthUser,
                                Model model) {

        String email = oauthUser.getAttribute("email"); // 로그인된 이메일
        User user = userRepository.findBySocialEmail(email)
            .orElseThrow(() -> new RuntimeException("유저를 찾을 수 없습니다."));

        ChatRoom chatRoom = chatRoomRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("채팅방을 찾을 수 없습니다."));

        ChatRoomViewDTO chatData = ChatRoomViewDTO.builder()
            .chatRoomId(chatRoom.getId()) // 채팅방 아이디값
            .userId(user.getId()) // 유저
            .nickname(user.getNickname()) // 채팅방 유저 별명
            .chatRoomTitle(chatRoom.getTitle()) // 채팅방 제목
            .build();

        model.addAttribute("chatData", chatData);
        return "chat/chatroom";
    }
    
  
}
