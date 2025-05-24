package com.test.foodtrip.domain.chat.repository;

import com.test.foodtrip.domain.chat.entity.ChatRoom;
import com.test.foodtrip.domain.chat.entity.ChatroomNoticeHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

// 채팅방 공지사항 및 설명글 변경 이력을 저장하는 Repository
// 최신 공지사항 조회나 수정 히스토리 추적에 사용
public interface ChatroomNoticeRepository extends JpaRepository<ChatroomNoticeHistory, Long> {

    // 특정 채팅방의 가장 최신 공지사항 조회
    Optional<ChatroomNoticeHistory> findTopByChatRoomOrderByCreatedAtDesc(ChatRoom chatRoom);
}
