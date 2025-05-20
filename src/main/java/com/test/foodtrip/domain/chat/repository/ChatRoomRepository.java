package com.test.foodtrip.domain.chat.repository;

import com.test.foodtrip.domain.chat.entity.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;

// 채팅방 정보를 조회하거나 저장하는 Repository
// 기본 CRUD 외에 채팅방 리스트 출력에 주로 사용
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
    // 필요한 커스텀 쿼리가 있으면 여기에 작성
}
