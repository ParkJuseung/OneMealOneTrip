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
    private EntityManager em;

    private int countParticipants(Long chatRoomId) {
        return ((Number) em.createQuery("""
        SELECT COUNT(cu) FROM ChatroomUser cu
        WHERE cu.chatRoom.id = :chatRoomId
          AND cu.status = 'JOINED'
    """)
                .setParameter("chatRoomId", chatRoomId)
                .getSingleResult()).intValue();
    }

    @Override
    public List<ChatRoomListResponseDTO> findAllRooms(int offset, int limit, Long userId) {
        TypedQuery<ChatRoom> query = em.createQuery(
            "SELECT c FROM ChatRoom c JOIN FETCH c.user WHERE c.isDeleted = 'N' ORDER BY c.createdAt DESC",
            ChatRoom.class
        );
        query.setFirstResult(offset);
        query.setMaxResults(limit);

        List<ChatRoom> rooms = query.getResultList();

        return rooms.stream()
                .map(room -> {
                    int participantCount = countParticipants(room.getId()); // ✅ 동적 계산
                    return ChatRoomListResponseDTO.builder()
                            .id(room.getId())
                            .title(room.getTitle())
                            .thumbnailImageUrl(room.getThumbnailImageUrl())
                            .createdAt(room.getCreatedAt())
                            .hashtags(List.of())
                            .likeCount(0)
                            .participantCount(participantCount) // ✅ 적용
                            .ownerNickname(room.getUser() != null ? room.getUser().getNickname() : "알수없음")
                            .build();
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<ChatRoomListResponseDTO> findPopularRooms(int offset, int limit) {
        List<Object[]> result = em.createQuery("""
            SELECT c, COUNT(l.id)
            FROM ChatRoom c
            JOIN FETCH c.user
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
                    int participantCount = countParticipants(room.getId()); // ✅ 동적 계산

                    return ChatRoomListResponseDTO.builder()
                            .id(room.getId())
                            .title(room.getTitle())
                            .thumbnailImageUrl(room.getThumbnailImageUrl())
                            .createdAt(room.getCreatedAt())
                            .hashtags(List.of())
                            .likeCount(likeCount.intValue())
                            .participantCount(participantCount) // ✅ 적용
                            .ownerNickname(room.getUser() != null ? room.getUser().getNickname() : "알수없음")
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
        JOIN FETCH c.user
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
                    int participantCount = countParticipants(room.getId()); // ✅ 동적 계산

                    return ChatRoomListResponseDTO.builder()
                            .id(room.getId())
                            .title(room.getTitle())
                            .thumbnailImageUrl(room.getThumbnailImageUrl())
                            .createdAt(room.getCreatedAt())
                            .hashtags(List.of())
                            .likeCount(0)
                            .participantCount(participantCount) // ✅ 적용
                            .ownerNickname(room.getUser() != null ? room.getUser().getNickname() : "알수없음")
                            .build();
                })
                .collect(Collectors.toList());
    }
}
