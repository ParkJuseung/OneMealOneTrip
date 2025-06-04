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

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatMessageService {

	private final ChatMessageRepository chatMessageRepository;
	private final ChatroomUserRepository chatroomUserRepository;

	public List<ChatMessageResponseDTO> getPreviousMessages(Long chatRoomId, Long beforeMessageId) {
	    List<ChatMessage> messages = chatMessageRepository.findPreviousMessages(
	            chatRoomId,
	            beforeMessageId,
	            PageRequest.of(0, 10)
	    );

	    return messages.stream()
	            .sorted(Comparator.comparing(ChatMessage::getId))
	            .map(ChatMessageResponseDTO::fromEntity)
	            .collect(Collectors.toList());
	}

	/**
	 * 사용자가 채팅방에 입장할 때,
	 * 입장 기록이 없다면 joinedAt 이후 메시지만 조회
	 */
	public ChatMessageGroupedResponseDTO getGroupedMessages(Long chatRoomId, Long userId) {
	    Optional<ChatroomUser> userOpt = chatroomUserRepository.findByChatRoom_IdAndUser_Id(chatRoomId, userId);

	    if (userOpt.isEmpty()) {
	        // 방금 입장했지만 아직 DB에 반영되지 않은 경우 대비
	        return ChatMessageGroupedResponseDTO.builder()
	                .beforeMessages(List.of())
	                .afterMessages(List.of())
	                .lastReadMessageId(0L)
	                .build();
	    }

	    ChatroomUser user = userOpt.get();
	    LocalDateTime joinedAt = user.getJoinedAt();

	    // 사용자 입장 이후 메시지만 조회
	    List<ChatMessage> after = chatMessageRepository.findMessagesCreatedAfter(
	            chatRoomId, joinedAt, PageRequest.of(0, 30));
	    System.out.println("💡 [DEBUG] joinedAt = " + joinedAt);
	    after.forEach(m -> System.out.println("📩 afterMessageId: " + m.getId() + " / createdAt: " + m.getCreatedAt()));
	    
	    List<ChatMessageResponseDTO> afterMessages = after.stream()
	            .map(ChatMessageResponseDTO::fromEntity)
	            .collect(Collectors.toList());

	    // 기준선은 입장한 순간이므로 이전 메시지는 없음
	    return ChatMessageGroupedResponseDTO.builder()
	            .beforeMessages(List.of())
	            .afterMessages(afterMessages)
	            .lastReadMessageId(0L)
	            .build();
	}
}