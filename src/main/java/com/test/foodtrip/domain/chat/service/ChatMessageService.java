package com.test.foodtrip.domain.chat.service;

import com.test.foodtrip.domain.chat.dto.ChatMessageGroupedResponseDTO;
import com.test.foodtrip.domain.chat.dto.ChatMessageResponseDTO;
import com.test.foodtrip.domain.chat.entity.ChatMessage;
import com.test.foodtrip.domain.chat.entity.ChatroomUser;
import com.test.foodtrip.domain.chat.repository.ChatMessageRepository;
import com.test.foodtrip.domain.chat.repository.ChatroomUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatMessageService {

    private final ChatMessageRepository chatMessageRepository;
    private final ChatroomUserRepository chatroomUserRepository;

    /**
     * ✅ 입장 시 보여줄 메시지 (입장 시각 이후 메시지만 조회)
     */
    public ChatMessageGroupedResponseDTO getGroupedMessages(Long chatRoomId, Long userId) {
        ChatroomUser user = chatroomUserRepository.findByChatRoom_IdAndUser_Id(chatRoomId, userId)
            .orElseThrow(() -> new RuntimeException("채팅방 참여 정보가 없습니다."));

        if (!"JOINED".equals(user.getStatus())) {
            return ChatMessageGroupedResponseDTO.builder()
                    .beforeMessages(List.of())
                    .afterMessages(List.of())
                    .lastReadMessageId(0L)
                    .build();
        }

        LocalDateTime entryTime = user.getStatusUpdatedAt();

        List<ChatMessage> afterMessages = chatMessageRepository
                .findByChatRoom_IdAndCreatedAtAfterOrderByCreatedAtAsc(chatRoomId, entryTime);

        // ✅ senderRole 포함하여 DTO 변환
        List<ChatMessageResponseDTO> afterDtos = afterMessages.stream()
                .map(message -> {
                    ChatroomUser sender = chatroomUserRepository
                            .findByChatRoom_IdAndUser_Id(chatRoomId, message.getUser().getId())
                            .orElseThrow(() -> new RuntimeException("보낸 사람의 역할 정보를 찾을 수 없습니다."));
                    return ChatMessageResponseDTO.fromEntity(message, sender.getRole());
                })
                .collect(Collectors.toList());

        return ChatMessageGroupedResponseDTO.builder()
                .beforeMessages(List.of())
                .afterMessages(afterDtos)
                .lastReadMessageId(user.getLastReadMessageId() == null ? 0L : user.getLastReadMessageId())
                .build();
    }

    /**
     * ✅ 무한 스크롤을 위한 이전 메시지 조회 (입장 이후만 허용)
     */
    public List<ChatMessageResponseDTO> getPreviousMessages(Long chatRoomId, Long beforeMessageId, Long userId) {
        ChatroomUser user = chatroomUserRepository.findByChatRoom_IdAndUser_Id(chatRoomId, userId)
            .orElseThrow(() -> new RuntimeException("채팅방 참여 정보가 없습니다."));

        LocalDateTime entryTime = user.getStatusUpdatedAt();

        List<ChatMessage> messages = chatMessageRepository.findPreviousMessagesAfterTime(
                chatRoomId,
                beforeMessageId,
                entryTime,
                PageRequest.of(0, 10)
        );

        return messages.stream()
                .sorted(Comparator.comparing(ChatMessage::getId))
                .map(message -> {
                    ChatroomUser sender = chatroomUserRepository
                            .findByChatRoom_IdAndUser_Id(chatRoomId, message.getUser().getId())
                            .orElseThrow(() -> new RuntimeException("보낸 사람의 역할 정보를 찾을 수 없습니다."));
                    return ChatMessageResponseDTO.fromEntity(message, sender.getRole());
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public void markMessagesAsRead(Long chatRoomId, Long userId, Long lastMessageId) {
        ChatroomUser cu = chatroomUserRepository.findByChatRoom_IdAndUser_Id(chatRoomId, userId)
            .orElseThrow(() -> new RuntimeException("채팅방 참여 정보가 없습니다."));

        cu.setLastReadMessageId(lastMessageId);
    }
}
