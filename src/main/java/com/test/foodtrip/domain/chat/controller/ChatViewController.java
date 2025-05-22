package com.test.foodtrip.domain.chat.controller;

import com.test.foodtrip.domain.chat.dto.ChatRoomListResponseDTO;
import com.test.foodtrip.domain.chat.service.ChatRoomService;
import com.test.foodtrip.domain.user.entity.User;
import com.test.foodtrip.domain.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class ChatViewController {

    private final ChatRoomService chatRoomService;
    private final UserRepository userRepository;

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
}
