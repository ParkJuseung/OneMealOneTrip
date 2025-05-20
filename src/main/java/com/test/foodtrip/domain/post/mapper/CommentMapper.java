package com.test.foodtrip.domain.post.mapper;

import com.test.foodtrip.domain.post.dto.CommentWithRepliesDTO;
import com.test.foodtrip.domain.post.entity.Comment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface CommentMapper {

    /**
     * 인기 댓글 조회 (좋아요가 5개 이상인 댓글)
     */
    List<Comment> findPopularComments(@Param("postId") Long postId, @Param("limit") int limit);

    /**
     * 댓글과 대댓글 함께 조회 (페이징 처리)
     */
    List<CommentWithRepliesDTO> findCommentsWithReplies(
            @Param("postId") Long postId,
            @Param("offset") int offset,
            @Param("limit") int limit);

    /**
     * 댓글 통계 조회
     */
    List<Map<String, Object>> getCommentStats(@Param("postId") Long postId);

    /**
     * 사용자의 댓글 반응 조회
     */
    List<Map<String, Object>> getUserReactions(
            @Param("userId") Long userId,
            @Param("commentIds") List<Long> commentIds);
}