// 📄 ChatRoomHashtagRepository.java
package com.test.foodtrip.domain.chat.repository;

import com.test.foodtrip.domain.chat.entity.ChatRoomHashtag;
import com.test.foodtrip.domain.chat.entity.ChatRoomHashtagId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface ChatRoomHashtagRepository extends JpaRepository<ChatRoomHashtag, ChatRoomHashtagId> {

    // 특정 채팅방에 연결된 해시태그 전체 삭제
    @Modifying
    @Transactional
    @Query("DELETE FROM ChatRoomHashtag crh WHERE crh.chatRoom.id = :chatRoomId")
    void deleteByChatRoomId(@Param("chatRoomId") Long chatRoomId);

    // 선택적: 특정 해시태그로 연결된 방 목록 조회
    List<ChatRoomHashtag> findByHashtagId(Long hashtagId);


    List<ChatRoomHashtag> findByChatRoomId(Long id);
}
