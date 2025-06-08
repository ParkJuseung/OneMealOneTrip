package com.test.foodtrip.domain.chat.controller;

import com.test.foodtrip.domain.chat.dto.ChatMessageGroupedResponseDTO;
import com.test.foodtrip.domain.chat.dto.ChatMessageResponseDTO;
import com.test.foodtrip.domain.chat.service.ChatMessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
    @Operation(
            summary = "과거 메시지 조회 (위로 스크롤)",
            description = "스크롤을 올릴 때, 기준 메시지 ID보다 이전에 작성된 메시지 30개를 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "이전 메시지 목록 반환"),
            @ApiResponse(responseCode = "404", description = "채팅방 또는 메시지 기준 ID 없음")
    })
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
    @Operation(
            summary = "기준선 기반 과거+이후 메시지 그룹 조회",
            description = "채팅방 입장 시, 유저의 마지막 읽은 메시지를 기준으로 과거 메시지(before)와 이후 메시지(after)를 나누어 반환합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "메시지 그룹 반환"),
            @ApiResponse(responseCode = "404", description = "채팅방 또는 사용자 정보 없음")
    })
    @GetMapping("/{chatRoomId}/grouped-messages")
    public ResponseEntity<ChatMessageGroupedResponseDTO> getGroupedMessages(
            @PathVariable("chatRoomId") Long chatRoomId,
            @RequestParam("userId") Long userId
    ) {
        ChatMessageGroupedResponseDTO response = chatMessageService.getGroupedMessages(chatRoomId, userId);
        return ResponseEntity.ok(response);
    }
}
