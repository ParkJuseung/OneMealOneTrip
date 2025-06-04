package com.test.foodtrip.domain.post.service;

import com.test.foodtrip.domain.post.dto.*;
import com.test.foodtrip.domain.post.entity.Comment;
import com.test.foodtrip.domain.post.entity.enums.DeleteStatus;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CommentService {

    // 댓글 CRUD
    CommentDTO createComment(CommentCreateRequest request);
    CommentDTO updateComment(Long commentId, CommentUpdateRequest request);
    void deleteComment(Long commentId);
    CommentDTO getComment(Long commentId);

    // 댓글 조회 - 단순화된 버전
    PageResultDTO<CommentDTO, Comment> getCommentsByPostId(Long postId, Pageable pageable);
    List<CommentDTO> getPopularComments(Long postId);
    Long getTotalCommentCount(Long postId);

    // 댓글 반응 (좋아요/싫어요)
    CommentReactionResultDTO toggleReaction(Long commentId, CommentReactionRequest request);
    CommentStatisticsDTO getCommentStatistics(Long commentId);
    CommentReactionSummaryDTO getUserReactionSummary(Long commentId, Long userId);

    // 대댓글 관련
    List<CommentDTO> getReplies(Long parentCommentId);

    // 변환 메서드들 - displayContent 추가
    default CommentDTO entityToDto(Comment comment) {
        CommentDTO dto = CommentDTO.builder()
                .id(comment.getId())
                .postId(comment.getPost().getId())
                .userId(comment.getUser().getId())
                .userNickname(comment.getUser().getNickname())
                .userProfileImage(comment.getUser().getProfileImage())
                .content(comment.getContent())
                .parentId(comment.getParent() != null ? comment.getParent().getId() : null)
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .isDeleted(comment.getIsDeleted())
                .hasReplies(!comment.getReplies().isEmpty())
                .build();

        // displayContent 명시적으로 설정
        if (comment.getIsDeleted() == DeleteStatus.Y) {
            // DTO에는 displayContent 필드가 없으므로 content를 직접 수정
            dto.setContent("삭제된 댓글입니다");
        }

        return dto;
    }
}