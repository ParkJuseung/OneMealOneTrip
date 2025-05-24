package com.test.foodtrip.domain.chat.controller;

import com.test.foodtrip.domain.chat.dto.ChatMessageResponseDTO;
import com.test.foodtrip.domain.chat.dto.ChatMessageSendRequestDTO;
import com.test.foodtrip.domain.chat.entity.ChatMessage;
import com.test.foodtrip.domain.chat.entity.ChatRoom;
import com.test.foodtrip.domain.chat.repository.ChatMessageRepository;
import com.test.foodtrip.domain.chat.repository.ChatRoomRepository;
import com.test.foodtrip.domain.user.entity.User;
import com.test.foodtrip.domain.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class ChatMessageController {

    private final ChatRoomRepository chatRoomRepository;
    private final UserRepository userRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/chat.sendMessage") // 클라이언트는 /app/chat.sendMessage로 보냄
    @Transactional
    public void handleMessage(ChatMessageSendRequestDTO dto) {

        ChatRoom chatRoom = chatRoomRepository.findById(dto.getChatRoomId())
                .orElseThrow(() -> new IllegalArgumentException("채팅방이 존재하지 않습니다."));

        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("유저가 존재하지 않습니다."));

        // 채팅 메시지 생성 및 저장
        ChatMessage message = ChatMessage.create(chatRoom, user, dto.getContent());
        chatMessageRepository.save(message);

        // 메시지 응답용 DTO 구성 (나중에 닉네임, 시간 등 포함 가능)
        ChatMessageResponseDTO response = ChatMessageResponseDTO.builder()
        	    .userId(user.getId())
        	    .chatRoomId(dto.getChatRoomId())
        	    .senderNickname(user.getNickname())
        	    .content(dto.getContent())
        	    .messageType("TEXT")
        	    .build();

        // 해당 채팅방 구독자에게 메시지 브로드캐스트
        messagingTemplate.convertAndSend("/topic/chatroom." + dto.getChatRoomId(), response);
    }
}
