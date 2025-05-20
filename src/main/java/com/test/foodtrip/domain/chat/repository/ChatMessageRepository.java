package com.test.foodtrip.domain.chat.repository;

import com.test.foodtrip.domain.chat.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

// 채팅 메시지를 저장하고, 채팅방별 메시지를 조회하는 Repository
// 무한 스크롤, 최근 메시지 불러오기 등에서 사용
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    // 특정 채팅방에 속한 메시지 조회 (최신순)
    List<ChatMessage> findByChatRoomIdOrderByCreatedAtAsc(Long chatRoomId);
}