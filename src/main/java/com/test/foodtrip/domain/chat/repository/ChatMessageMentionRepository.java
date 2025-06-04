package com.test.foodtrip.domain.chat.repository;

import com.test.foodtrip.domain.chat.entity.ChatMessageMention;
import org.springframework.data.jpa.repository.JpaRepository;

// 채팅 메시지 내에서 멘션된 사용자 정보를 관리하는 Repository
// 특정 메시지에서 어떤 사용자가 언급되었는지 확인
public interface ChatMessageMentionRepository extends JpaRepository<ChatMessageMention, ChatMessageMention.ChatMessageMentionId> {
}
