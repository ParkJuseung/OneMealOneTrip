package com.test.foodtrip.domain.chat.repository;

import com.test.foodtrip.domain.chat.dto.ChatRoomListResponseDTO;
import com.test.foodtrip.domain.chat.entity.ChatRoom;
import org.codehaus.groovy.runtime.StreamGroovyMethods;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

// 채팅방 정보를 조회하거나 저장하는 Repository
// 기본 CRUD 외에 채팅방 리스트 출력에 주로 사용
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long>, ChatRoomRepositoryCustom {

    @Query("SELECT c FROM ChatRoom c WHERE c.isDeleted = 'N'")
    List<ChatRoom> findAllNotDeleted();

    List<ChatRoom> findByIsDeleted(String isDeleted);

	List<ChatRoom> findByIsDeletedOrderByCreatedAtDesc(String string);

    long countByIsDeleted(String isDeleted);

    // (선택) 기존 페이징용 쿼리
    List<ChatRoomListResponseDTO> findAllRooms(int offset, int limit, Long userId);
    List<ChatRoomListResponseDTO> findPopularRooms(int offset, int limit);
    List<ChatRoomListResponseDTO> findMyRooms(Long userId, int offset, int limit);

    @Query("SELECT r FROM ChatRoom r WHERE LOWER(r.title) LIKE LOWER(CONCAT('%', :keyword, '%')) ORDER BY r.title ASC")
    List<ChatRoom> findTop5ByTitleContainingIgnoreCaseOrderByTitleAsc(@Param("keyword") String keyword);

}
