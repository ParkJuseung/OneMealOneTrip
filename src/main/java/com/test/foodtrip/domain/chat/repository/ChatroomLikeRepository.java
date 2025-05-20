package com.test.foodtrip.domain.chat.repository;

import com.test.foodtrip.domain.chat.entity.ChatroomLike;
import org.springframework.data.jpa.repository.JpaRepository;

// 채팅방에 대한 좋아요 상태를 저장하는 Repository
// 유저가 어떤 채팅방에 좋아요를 눌렀는지 추적
public interface ChatroomLikeRepository extends JpaRepository<ChatroomLike, Long> {
}
