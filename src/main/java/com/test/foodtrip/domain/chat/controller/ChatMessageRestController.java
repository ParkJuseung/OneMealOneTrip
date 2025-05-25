package com.test.foodtrip.domain.chat.controller;

import com.test.foodtrip.domain.chat.dto.ChatMessageGroupedResponseDTO;
import com.test.foodtrip.domain.chat.dto.ChatMessageResponseDTO;
import com.test.foodtrip.domain.chat.service.ChatMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chatroom")
public class ChatMessageRestController {

    private final ChatMessageService chatMessageService;

    /**
     * 위로 스크롤 시: 과거 메시지 30개 불러오기
     */
    @GetMapping("/{chatRoomId}/previous-messages")
    public ResponseEntity<List<ChatMessageResponseDTO>> getPreviousMessages(
            @PathVariable("chatRoomId") Long chatRoomId,
            @RequestParam("before") Long beforeMessageId
    ) {
        List<ChatMessageResponseDTO> messages = chatMessageService.getPreviousMessages(chatRoomId, beforeMessageId);
        return ResponseEntity.ok(messages); // JSON으로 응답
    }

    /**
     * 입장 시: 기준선 기준 과거+이후 메시지 나눠서 반환
     */
    @GetMapping("/{chatRoomId}/grouped-messages")
    public ResponseEntity<ChatMessageGroupedResponseDTO> getGroupedMessages(
            @PathVariable("chatRoomId") Long chatRoomId,
            @RequestParam("userId") Long userId
    ) {
        ChatMessageGroupedResponseDTO response = chatMessageService.getGroupedMessages(chatRoomId, userId);
        return ResponseEntity.ok(response);
    }
}
