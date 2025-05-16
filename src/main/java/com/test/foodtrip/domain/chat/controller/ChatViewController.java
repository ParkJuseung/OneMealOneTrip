package com.test.foodtrip.domain.chat.controller;

import com.test.foodtrip.domain.chat.dto.ChatRoomListResponseDto;
import com.test.foodtrip.domain.chat.service.ChatRoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class ChatViewController {

    private final ChatRoomService chatRoomService;

    @GetMapping("/chatroom")
    public String chatRoomList(Model model) {
        List<ChatRoomListResponseDto> chatRooms = chatRoomService.getAllRooms();
        model.addAttribute("chatRooms", chatRooms);
        return "chat/chat-list";
    }
}
