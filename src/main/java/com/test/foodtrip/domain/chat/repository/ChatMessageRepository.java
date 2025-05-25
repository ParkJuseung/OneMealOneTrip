package com.test.foodtrip.domain.chat.repository;

import com.test.foodtrip.domain.chat.entity.ChatMessage;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

// 채팅 메시지를 저장하고, 채팅방별 메시지를 조회하는 Repository
// 무한 스크롤, 최근 메시지 불러오기 등에서 사용
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

	// 특정 채팅방에 속한 메시지 조회 (최신순)
	List<ChatMessage> findByChatRoomIdOrderByCreatedAtAsc(Long chatRoomId);

	// 사용자가 마지막으로 읽은 메시지 처리
	@Query("SELECT MAX(m.id) FROM ChatMessage m WHERE m.chatRoom.id = :chatRoomId")
	Long findLastMessageIdByChatRoomId(@Param("chatRoomId") Long chatRoomId);

	@Query("""
			    SELECT m FROM ChatMessage m
			    WHERE m.chatRoom.id = :chatRoomId
			      AND m.id > :lastReadMessageId
			    ORDER BY m.id ASC
			""")
	List<ChatMessage> findMessagesAfterLastRead(@Param("chatRoomId") Long chatRoomId,
			@Param("lastReadMessageId") Long lastReadMessageId, Pageable pageable // 예: PageRequest.of(0, 30)
	);

	@Query("""
			    SELECT m FROM ChatMessage m
			    WHERE m.chatRoom.id = :chatRoomId
			      AND m.id < :beforeMessageId
			    ORDER BY m.id DESC
			""")
	List<ChatMessage> findPreviousMessages(@Param("chatRoomId") Long chatRoomId,
			@Param("beforeMessageId") Long beforeMessageId, Pageable pageable);

	// ✅ 처음 입장한 사용자용 메시지 조회 (joinedAt 이후)
	@Query("""
		    SELECT m FROM ChatMessage m
		    WHERE m.chatRoom.id = :chatRoomId
		      AND m.createdAt >= :joinedAt
		    ORDER BY m.id ASC
		""")
		List<ChatMessage> findMessagesCreatedAfter(@Param("chatRoomId") Long chatRoomId,
		                                           @Param("joinedAt") LocalDateTime joinedAt,
		                                           Pageable pageable);

}