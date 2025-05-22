package com.test.foodtrip.domain.post.repository;

import com.test.foodtrip.domain.post.entity.Comment;
import com.test.foodtrip.domain.post.entity.enums.DeleteStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    // 게시글의 댓글 수 조회
    Long countByPostIdAndIsDeleted(Long postId, DeleteStatus isDeleted);

    // 부모 댓글의 대댓글 수 조회
    Long countByParentAndIsDeleted(Comment parent, DeleteStatus isDeleted);

    // 게시글의 최상위 댓글 조회 (페이징) - 단순화
    @Query("""
        SELECT c FROM Comment c 
        WHERE c.post.id = :postId 
            AND c.parent IS NULL 
            AND c.isDeleted = 'N'
        ORDER BY c.createdAt ASC
        """)
    Page<Comment> findTopLevelCommentsByPostId(@Param("postId") Long postId, Pageable pageable);

    // 대댓글 조회 (단순 버전)
    @Query("SELECT c FROM Comment c WHERE c.parent.id = :parentId AND c.isDeleted = 'N' ORDER BY c.createdAt ASC")
    List<Comment> findRepliesByParentId(@Param("parentId") Long parentId);

    // 게시글의 모든 활성 댓글 조회 (통계용)
    @Query("""
        SELECT c FROM Comment c 
        WHERE c.post.id = :postId 
            AND c.isDeleted = 'N'
        ORDER BY c.createdAt DESC
        """)
    List<Comment> findAllActiveCommentsByPostId(@Param("postId") Long postId);

    // 사용자가 작성한 댓글 조회
    @Query("""
        SELECT c FROM Comment c 
        WHERE c.user.id = :userId 
            AND c.isDeleted = 'N'
        ORDER BY c.createdAt DESC
        """)
    Page<Comment> findCommentsByUserId(@Param("userId") Long userId, Pageable pageable);

    // 최근 댓글 조회
    @Query("""
        SELECT c FROM Comment c 
        WHERE c.post.id = :postId 
            AND c.isDeleted = 'N'
        ORDER BY c.createdAt DESC
        """)
    List<Comment> findRecentComments(@Param("postId") Long postId, Pageable pageable);

    // 오늘 작성된 댓글 수 - 네이티브 쿼리로 변경
    @Query(value = """
        SELECT COUNT(*) FROM COMMENTS c 
        WHERE c.post_id = :postId 
            AND c.is_deleted = 'N'
            AND TRUNC(c.created_at) = TRUNC(SYSDATE)
        """, nativeQuery = true)
    Long countTodayComments(@Param("postId") Long postId);
}