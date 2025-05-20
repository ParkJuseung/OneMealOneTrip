package com.test.foodtrip.domain.chat.repository;

import com.test.foodtrip.domain.chat.entity.ChatroomUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

//채팅방 참여자(사용자) 정보를 관리하는 Repository
//채팅방에 누가 입장/퇴장했는지, 유저-채팅방 관계 등을 처리

public interface ChatroomUserRepository extends JpaRepository<ChatroomUser, Long> {


    //주어진 채팅방 ID와 사용자 ID로 참여 정보 조회
    //존재하지 않을 수 있으므로 Optional로 반환
    Optional<ChatroomUser> findByChatRoomIdAndUserId(Long chatRoomId, Long userId);

    // 필요 시 유저별 참여 채팅방 조회 등 커스텀 메서드 추가 가능
    // ChatroomUserRepository.java
    int countByChatRoomId(Long chatRoomId);
    
    // ✅ 여기 추가
    Optional<ChatroomUser> findByChatRoomIdAndRole(Long chatRoomId, String role);
}
