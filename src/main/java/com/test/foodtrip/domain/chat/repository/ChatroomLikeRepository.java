package com.test.foodtrip.domain.chat.repository;

import aj.org.objectweb.asm.commons.Remapper;
import com.test.foodtrip.domain.chat.entity.ChatRoom;
import com.test.foodtrip.domain.chat.entity.ChatroomLike;
import com.test.foodtrip.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

// 채팅방에 대한 좋아요 상태를 저장하는 Repository
// 유저가 어떤 채팅방에 좋아요를 눌렀는지 추적
public interface ChatroomLikeRepository extends JpaRepository<ChatroomLike, Long> {
	// ChatroomLikeRepository.java
	int countByChatRoom_Id(Long chatRoomId); // 언더스코어를 써야 연관 필드의 ID로 조건을 걸 수 있음

	Optional<ChatroomLike> findByChatRoomAndUser(ChatRoom chatRoom, User user);
	Long countByChatRoom_IdAndIsActive(Long chatRoomId, String isActive);
}
