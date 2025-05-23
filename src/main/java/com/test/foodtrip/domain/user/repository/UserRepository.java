package com.test.foodtrip.domain.user.repository;

import com.test.foodtrip.domain.user.entity.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findBySocialTypeAndSocialEmail(String socialType, String socialEmail);
    boolean existsByNickname(String nickname);

    // 닉네임으로 사용자 검색
    Optional<User> findByNickname(String nickname);
    
    //
    Optional<User> findBySocialEmail(String email);

    // 활성 상태인 사용자 조회
    @Query("SELECT u FROM User u WHERE u.active = 'Y'")
    List<User> findActiveUsers();

    // 댓글 작성 통계가 높은 사용자들 조회
    @Query("""
    SELECT u, COUNT(c) as commentCount
    FROM User u 
    LEFT JOIN u.comments c
    WHERE u.active = 'Y' 
        AND c.isDeleted = 'N'
    GROUP BY u.id
    ORDER BY COUNT(c) DESC
    """)
    List<Object[]> findActiveCommenters(Pageable pageable);

}
