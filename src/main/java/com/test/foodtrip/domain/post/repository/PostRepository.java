package com.test.foodtrip.domain.post.repository;

import com.test.foodtrip.domain.post.entity.Post;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {

    // 게시글의 전체 통계 조회 (조회수, 좋아요, 댓글 수)
    @Query("""
    SELECT p.viewCount,
           COUNT(DISTINCT pl.id) as likeCount,
           COUNT(DISTINCT c.id) as commentCount
    FROM Post p 
    LEFT JOIN p.likes pl
    LEFT JOIN p.comments c
    WHERE p.id = :postId
    GROUP BY p.id, p.viewCount
    """)
    Object[] findPostStatistics(@Param("postId") Long postId);

    // 인기 게시글 조회 (좋아요와 댓글 수 기준)
    @Query("""
    SELECT p FROM Post p 
    LEFT JOIN p.likes pl
    LEFT JOIN p.comments c
    WHERE p.createdAt >= :since
    GROUP BY p.id
    ORDER BY (COUNT(DISTINCT pl.id) + COUNT(DISTINCT c.id)) DESC
    """)
    List<Post> findPopularPosts(@Param("since") LocalDateTime since, Pageable pageable);
}
