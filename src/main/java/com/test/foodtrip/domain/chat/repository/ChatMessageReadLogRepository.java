package com.test.foodtrip.domain.chat.repository;

import com.test.foodtrip.domain.chat.entity.ChatMessageReadLog;
import org.springframework.data.jpa.repository.JpaRepository;

// 채팅 메시지의 읽음 상태를 관리하는 Repository
// 특정 메시지를 누가 읽었는지 저장하고 조회
public interface ChatMessageReadLogRepository extends JpaRepository<ChatMessageReadLog, ChatMessageReadLog.ChatMessageReadLogId> {
}
