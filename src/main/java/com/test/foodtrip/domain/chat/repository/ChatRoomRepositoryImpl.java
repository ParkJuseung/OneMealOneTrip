package com.test.foodtrip.domain.chat.repository;

import com.test.foodtrip.domain.chat.dto.ChatRoomListResponseDTO;
import com.test.foodtrip.domain.chat.entity.ChatRoom;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class ChatRoomRepositoryImpl implements ChatRoomRepositoryCustom {

    @PersistenceContext
    private final EntityManager em;

    @Override
    public List<ChatRoomListResponseDTO> findAllRooms(int offset, int limit, Long userId) {
        TypedQuery<ChatRoom> query = em.createQuery(
                "SELECT c FROM ChatRoom c WHERE c.isDeleted = 'N' ORDER BY c.createdAt DESC", ChatRoom.class
        );
        query.setFirstResult(offset);
        query.setMaxResults(limit);

        List<ChatRoom> rooms = query.getResultList();

        return rooms.stream()
                .map(room -> ChatRoomListResponseDTO.builder()
                        .id(room.getId())
                        .title(room.getTitle())
                        .thumbnailImageUrl(room.getThumbnailImageUrl())
                        .createdAt(room.getCreatedAt())
                        .hashtags(List.of())  // 나중에 Hashtag join 또는 서비스 레이어에서 추가 가능
                        .likeCount(0)
                        .participantCount(0)
                        .ownerNickname("알수없음")
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public List<ChatRoomListResponseDTO> findPopularRooms(int offset, int limit) {
        // JPQL로 채팅방과 좋아요 수를 기준으로 정렬 (좋아요 수 DESC, 생성일 DESC)
        List<Object[]> result = em.createQuery("""
            SELECT c, COUNT(l.id)
            FROM ChatRoom c
            LEFT JOIN ChatroomLike l ON l.chatRoom.id = c.id
            WHERE c.isDeleted = 'N'
            GROUP BY c
            ORDER BY COUNT(l.id) DESC, c.createdAt DESC
        """, Object[].class)
                .setFirstResult(offset)
                .setMaxResults(limit)
                .getResultList();

        return result.stream()
                .map(row -> {
                    ChatRoom room = (ChatRoom) row[0];
                    Long likeCount = (Long) row[1];

                    return ChatRoomListResponseDTO.builder()
                            .id(room.getId())
                            .title(room.getTitle())
                            .thumbnailImageUrl(room.getThumbnailImageUrl())
                            .createdAt(room.getCreatedAt())
                            .hashtags(List.of()) // 나중에 서비스 계층에서 추가 처리
                            .likeCount(likeCount.intValue())
                            .participantCount(0)
                            .ownerNickname("알수없음")
                            .build();
                })
                .collect(Collectors.toList());
    }


    @Override
    public List<ChatRoomListResponseDTO> findMyRooms(Long userId, int offset, int limit) {
        List<Object[]> result = em.createQuery("""
        SELECT c, cu.role
        FROM ChatroomUser cu
        JOIN cu.chatRoom c
        WHERE cu.user.id = :userId
          AND cu.status = 'JOINED'
          AND c.isDeleted = 'N'
        ORDER BY c.createdAt DESC
    """, Object[].class)
                .setParameter("userId", userId)
                .setFirstResult(offset)
                .setMaxResults(limit)
                .getResultList();

        return result.stream()
                .map(row -> {
                    ChatRoom room = (ChatRoom) row[0];

                    return ChatRoomListResponseDTO.builder()
                            .id(room.getId())
                            .title(room.getTitle())
                            .thumbnailImageUrl(room.getThumbnailImageUrl())
                            .createdAt(room.getCreatedAt())
                            .hashtags(List.of()) // 나중에 서비스 계층에서 해시태그 주입
                            .likeCount(0) // 나중에 서비스 계층에서 주입
                            .participantCount(0)
                            .ownerNickname("알수없음")
                            .build();
                })
                .collect(Collectors.toList());
    }

}
