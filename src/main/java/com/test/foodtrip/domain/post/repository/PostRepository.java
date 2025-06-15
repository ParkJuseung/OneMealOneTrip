package com.test.foodtrip.domain.post.repository;

import com.test.foodtrip.domain.post.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

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

    // 1. 기본 정보 + 이미지만 조회
    @Query("""
                SELECT DISTINCT p FROM Post p 
                LEFT JOIN FETCH p.images
                LEFT JOIN FETCH p.user
                WHERE p.id = :id
            """)
    Optional<Post> findByIdWithImages(@Param("id") Long id);

    // 2. 조회수 증가는 그대로 유지
    @Modifying
    @Transactional
    @Query("UPDATE Post p SET p.viewCount = p.viewCount + 1 WHERE p.id = :id")
    int incrementViewCount(@Param("id") Long id);


    // 목록 조회용 - 연관관계 없이 기본 정보만
    @Query("SELECT p FROM Post p ORDER BY p.createdAt DESC")
    Page<Post> findAllOptimized(Pageable pageable);

    //기본 검색
    @Query("""
            SELECT p FROM Post p 
            WHERE 
                (:keyword IS NULL OR :keyword = '' OR 
                 p.title LIKE %:keyword% OR 
                 p.content LIKE %:keyword% OR 
                 p.placeName LIKE %:keyword%)
            ORDER BY p.createdAt DESC
            """)
    Page<Post> searchPosts(@Param("keyword") String keyword, Pageable pageable);

    // 태그 검색 기능
// 기존 문제 있는 메서드 제거 또는 수정
    @Query("""
SELECT DISTINCT p FROM Post p 
JOIN p.tags t
JOIN t.postTag pt
WHERE LOWER(pt.tagText) LIKE LOWER(CONCAT('%', :tagKeyword, '%'))
ORDER BY p.createdAt DESC
""")
    Page<Post> searchPostsByTag(@Param("tagKeyword") String tagKeyword, Pageable pageable);

    // 새로운 안전한 메서드들 추가
    @Query("""
SELECT DISTINCT pt.post FROM PostTagging pt
WHERE LOWER(pt.postTag.tagText) LIKE LOWER(CONCAT('%', :tagKeyword, '%'))
ORDER BY pt.post.createdAt DESC
""")
    Page<Post> searchPostsByTagViaTagging(@Param("tagKeyword") String tagKeyword, Pageable pageable);

    @Query("""
SELECT p FROM Post p 
WHERE p.id IN (
    SELECT DISTINCT pt.post.id FROM PostTagging pt
    WHERE LOWER(pt.postTag.tagText) LIKE LOWER(CONCAT('%', :tagKeyword, '%'))
)
ORDER BY p.createdAt DESC
""")
    Page<Post> findPostsByTagKeyword(@Param("tagKeyword") String tagKeyword, Pageable pageable);
}