package com.test.foodtrip.domain.chat.repository;

import com.test.foodtrip.domain.chat.entity.ChatMessage;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    // ✅ 입장 시 보여줄 메시지: statusUpdatedAt 이후 전체 메시지
    List<ChatMessage> findByChatRoom_IdAndCreatedAtAfterOrderByCreatedAtAsc(
        Long chatRoomId, LocalDateTime afterTime
    );

    // ✅ 무한 스크롤: 이전 메시지 중 statusUpdatedAt 이후만
    @Query("""
        SELECT m FROM ChatMessage m
        WHERE m.chatRoom.id = :chatRoomId
          AND m.id < :beforeId
          AND m.createdAt > :afterTime
        ORDER BY m.id DESC
    """)
    List<ChatMessage> findPreviousMessagesAfterTime(
        @Param("chatRoomId") Long chatRoomId,
        @Param("beforeId") Long beforeId,
        @Param("afterTime") LocalDateTime afterTime,
        Pageable pageable
    );

    // ✅ UX용: 가장 마지막 메시지 ID (읽음 처리 등에서 사용 가능)
    @Query("SELECT MAX(m.id) FROM ChatMessage m WHERE m.chatRoom.id = :chatRoomId")
    Long findLastMessageIdByChatRoomId(@Param("chatRoomId") Long chatRoomId);

    // ✅ UX용: 사용자가 읽지 않은 메시지들 (읽음선 기준)
    @Query("""
        SELECT m FROM ChatMessage m
        WHERE m.chatRoom.id = :chatRoomId
          AND m.id > :lastReadMessageId
        ORDER BY m.id ASC
    """)
    List<ChatMessage> findMessagesAfterLastRead(
        @Param("chatRoomId") Long chatRoomId,
        @Param("lastReadMessageId") Long lastReadMessageId,
        Pageable pageable
    );
}
