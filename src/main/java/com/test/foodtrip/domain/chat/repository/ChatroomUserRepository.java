package com.test.foodtrip.domain.chat.repository;


import com.test.foodtrip.domain.chat.entity.ChatroomUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

//채팅방 참여자(사용자) 정보를 관리하는 Repository
//채팅방에 누가 입장/퇴장했는지, 유저-채팅방 관계 등을 처리

public interface ChatroomUserRepository extends JpaRepository<ChatroomUser, Long> {

    // 주어진 채팅방 ID와 사용자 ID로 참여 정보 조회
    Optional<ChatroomUser> findByChatRoomIdAndUserId(Long chatRoomId, Long userId);

    // 채팅방 ID 기준으로 참여자 수 카운트
    int countByChatRoomId(Long chatRoomId);

    // 채팅방 ID와 역할로 참여자 조회 (예: 방장 찾기)
    Optional<ChatroomUser> findByChatRoomIdAndRole(Long chatRoomId, String role);

    // 채팅방 ID와 사용자 ID로 참여 정보 조회 (다른 네이밍 방식)
    Optional<ChatroomUser> findByChatRoom_IdAndUser_Id(Long chatRoomId, Long userId);

    // 채팅방 삭제 시 참여자 전부 찾기
    List<ChatroomUser> findByChatRoom_Id(Long chatRoomId);

    // 닉네임 자동완성 등에서 사용
    List<ChatroomUser> findTop5ByRoleAndUserNicknameContainingIgnoreCaseOrderByUserNicknameAsc(String role, String nicknamePart);

    // 유저가 참여한 채팅방 목록 중 특정 상태 필터링
    List<ChatroomUser> findByUserIdAndStatus(Long userId, String joined);

    @Query("""
    	    SELECT cu.lastReadMessageId FROM ChatroomUser cu
    	    WHERE cu.chatRoom.id = :chatRoomId AND cu.user.id = :userId
    	""")
    	Optional<Long> findLastReadMessageId(
    	    @Param("chatRoomId") Long chatRoomId,
    	    @Param("userId") Long userId
    	);

    boolean existsByChatRoom_IdAndUser_Id(Long chatRoomId, Long userId);
	
}
