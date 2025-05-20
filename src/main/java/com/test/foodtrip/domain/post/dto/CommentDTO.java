package com.test.foodtrip.domain.post.dto;

import com.test.foodtrip.domain.post.entity.Comment;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter @Setter
@NoArgsConstructor
public class CommentDTO {

    private Long id;
    private String content;
    private Long userId;
    private String userName;
    private String userProfileImage;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean deleted;
    private boolean blinded;
    private boolean popular;
    private long likeCount;
    private long dislikeCount;
    private Long parentId;
    private List<CommentDTO> replies = new ArrayList<>();
    private boolean isCurrentUserLiked;  // 현재 사용자가 좋아요를 눌렀는지
    private boolean isCurrentUserDisliked;  // 현재 사용자가 싫어요를 눌렀는지
    private String displayContent;  // 표시될 내용

    @Builder
    public CommentDTO(Long id, String content, Long userId, String userName,
                      String userProfileImage, LocalDateTime createdAt,
                      LocalDateTime updatedAt, boolean deleted, boolean blinded,
                      boolean popular, long likeCount, long dislikeCount,
                      Long parentId, List<CommentDTO> replies,
                      boolean isCurrentUserLiked, boolean isCurrentUserDisliked) {
        this.id = id;
        this.content = content;
        this.userId = userId;
        this.userName = userName;
        this.userProfileImage = userProfileImage;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.deleted = deleted;
        this.blinded = blinded;
        this.popular = popular;
        this.likeCount = likeCount;
        this.dislikeCount = dislikeCount;
        this.parentId = parentId;
        this.replies = replies != null ? replies : new ArrayList<>();
        this.isCurrentUserLiked = isCurrentUserLiked;
        this.isCurrentUserDisliked = isCurrentUserDisliked;

        // 표시될 내용 설정
        if (deleted) {
            this.displayContent = "삭제된 댓글입니다";
        } else if (blinded) {
            this.displayContent = "블라인드 처리된 댓글입니다";
        } else {
            this.displayContent = content;
        }
    }

    // Entity -> DTO 변환 메소드
    public static CommentDTO fromEntity(Comment comment, boolean isCurrentUserLiked,
                                        boolean isCurrentUserDisliked) {
        return CommentDTO.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .userId(comment.getUser().getId())
                .userName(comment.getUser().getNickname())
                .userProfileImage(comment.getUser().getProfileImage())
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .deleted(comment.isDeleted())
                .blinded(comment.isBlinded())
                .popular(comment.isPopular())
                .likeCount(comment.getLikeCount())
                .dislikeCount(comment.getDislikeCount())
                .parentId(comment.getParent() != null ? comment.getParent().getId() : null)
                .isCurrentUserLiked(isCurrentUserLiked)
                .isCurrentUserDisliked(isCurrentUserDisliked)
                .build();
    }
}