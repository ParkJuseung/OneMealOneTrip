package com.test.foodtrip.domain.chat.controller;

import com.test.foodtrip.domain.chat.dto.ChatRoomListResponseDTO;
import com.test.foodtrip.domain.chat.service.ChatRoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class ChatViewController {

    private final ChatRoomService chatRoomService;

    @GetMapping("/chat")
    public String chatRoomList(Model model) {
        Long currentUserId = 999L; // 테스트용 유저 ID
        List<ChatRoomListResponseDTO> chatRooms = chatRoomService.getAllRooms(currentUserId);
        model.addAttribute("chatRooms", chatRooms);

        // 🔧 mock authentication.principal.id 주입
        model.addAttribute("authentication", new Object() {
            public Object getPrincipal() {
                return new Object() {
                    public Long getId() {
                        return 999L;
                    }
                };
            }
        });

        return "chat/chat-list";
    }

    // ✅ 채팅방 수정 폼 페이지
    // URL 예시: /chatroom/detailedit?id=6&mode=edit
    @GetMapping("/chatroom/detailedit")
    public String showChatroomEditForm(@RequestParam Long id, @RequestParam(required = false) String mode, Model model) {
        model.addAttribute("chatroomId", id);
        model.addAttribute("mode", mode);

        if ("edit".equals(mode)) {
            var dto = chatRoomService.getRoomDetail(id, 999L);
            model.addAttribute("room", dto);
        }

        return "chat/detailedit"; // templates/chat/detailedit.html
    }

}
