package com.test.foodtrip.domain.post.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentReactionSummaryDTO {
    private Long commentId;
    private Long likeCount;
    private Long dislikeCount;
    private Boolean userLiked;
    private Boolean userDisliked;
}