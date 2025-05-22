package com.test.foodtrip.domain.post.repository;

import com.test.foodtrip.domain.post.entity.Comment;
import com.test.foodtrip.domain.post.entity.CommentReaction;
import com.test.foodtrip.domain.post.entity.enums.ReactionType;
import com.test.foodtrip.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CommentReactionRepository extends JpaRepository<CommentReaction, Long> {

    // 댓글에 대한 사용자의 반응 조회
    Optional<CommentReaction> findByCommentAndUser(Comment comment, User user);

    // 댓글의 특정 반응 타입 개수 조회
    Long countByCommentAndReactionType(Comment comment, ReactionType reactionType);

    // 댓글에 대한 사용자의 특정 반응 존재 여부
    boolean existsByCommentAndUserAndReactionType(Comment comment, User user, ReactionType reactionType);

    // 댓글의 모든 반응 조회
    List<CommentReaction> findByComment(Comment comment);

    // 사용자의 모든 반응 조회
    List<CommentReaction> findByUser(User user);

    // 댓글별 반응 통계 조회
    @Query("""
        SELECT cr.comment.id,
               SUM(CASE WHEN cr.reactionType = 'LIKE' THEN 1 ELSE 0 END) as likeCount,
               SUM(CASE WHEN cr.reactionType = 'DISLIKE' THEN 1 ELSE 0 END) as dislikeCount
        FROM CommentReaction cr 
        WHERE cr.comment.id IN :commentIds
        GROUP BY cr.comment.id
        """)
    List<Object[]> findReactionStatsByCommentIds(@Param("commentIds") List<Long> commentIds);

    // 게시글의 전체 댓글 반응 통계
    @Query("""
        SELECT SUM(CASE WHEN cr.reactionType = 'LIKE' THEN 1 ELSE 0 END) as totalLikes,
               SUM(CASE WHEN cr.reactionType = 'DISLIKE' THEN 1 ELSE 0 END) as totalDislikes
        FROM CommentReaction cr 
        WHERE cr.comment.post.id = :postId
        """)
    Object[] findPostCommentReactionStats(@Param("postId") Long postId);

    // 사용자가 특정 게시글 댓글들에 남긴 반응들 조회
    @Query("""
        SELECT cr FROM CommentReaction cr 
        WHERE cr.user.id = :userId 
            AND cr.comment.post.id = :postId
        """)
    List<CommentReaction> findUserReactionsInPost(@Param("userId") Long userId,
                                                  @Param("postId") Long postId);

    // 가장 많은 좋아요를 받은 댓글들 조회
    @Query("""
        SELECT cr.comment, COUNT(cr) as likeCount
        FROM CommentReaction cr 
        WHERE cr.reactionType = 'LIKE' 
            AND cr.comment.post.id = :postId
            AND cr.comment.isDeleted = 'N'
        GROUP BY cr.comment
        ORDER BY COUNT(cr) DESC
        """)
    List<Object[]> findMostLikedComments(@Param("postId") Long postId);

    // 블라인드 처리된 댓글들 조회 (싫어요 10개 이상)
    @Query("""
        SELECT cr.comment, COUNT(cr) as dislikeCount
        FROM CommentReaction cr 
        WHERE cr.reactionType = 'DISLIKE' 
            AND cr.comment.post.id = :postId
            AND cr.comment.isDeleted = 'N'
        GROUP BY cr.comment
        HAVING COUNT(cr) >= 10
        """)
    List<Object[]> findBlindedComments(@Param("postId") Long postId);
}