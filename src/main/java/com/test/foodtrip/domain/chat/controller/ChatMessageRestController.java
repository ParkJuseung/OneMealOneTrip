package com.test.foodtrip.domain.chat.controller;

import com.test.foodtrip.domain.chat.dto.ChatMessageGroupedResponseDTO;
import com.test.foodtrip.domain.chat.dto.ChatMessageResponseDTO;
import com.test.foodtrip.domain.chat.dto.MarkReadRequestDTO;
import com.test.foodtrip.domain.chat.service.ChatMessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chatroom")
@RequiredArgsConstructor
public class ChatMessageRestController {

	private final ChatMessageService chatMessageService;

	// ✅ 과거 메시지 조회
	@GetMapping("/{chatRoomId}/previous-messages")
	public ResponseEntity<List<ChatMessageResponseDTO>> getPreviousMessages(
			@PathVariable("chatRoomId") Long chatRoomId,
			@RequestParam("before") Long beforeMessageId,
			@RequestParam("userId") Long userId) {
		List<ChatMessageResponseDTO> result = chatMessageService.getPreviousMessages(chatRoomId, beforeMessageId, userId);
		return ResponseEntity.ok(result);
	}

	// ✅ 입장 시 기준선 기반 메시지 그룹 조회
	@GetMapping("/{chatRoomId}/grouped-messages")
	public ResponseEntity<ChatMessageGroupedResponseDTO> getGroupedMessages(
			@PathVariable("chatRoomId") Long chatRoomId,
			@RequestParam("userId") Long userId) {
		ChatMessageGroupedResponseDTO result = chatMessageService.getGroupedMessages(chatRoomId, userId);
		return ResponseEntity.ok(result);
	}

	// ✅ 읽음 처리 API
	@PostMapping("/{chatRoomId}/read")
	public ResponseEntity<?> markAsRead(
			@PathVariable("chatRoomId") Long chatRoomId,
			@RequestBody MarkReadRequestDTO dto,
			@RequestParam("userId") Long userId) {
		chatMessageService.markMessagesAsRead(chatRoomId, userId, dto.getLastMessageId());
		return ResponseEntity.ok().build();
	}
}
