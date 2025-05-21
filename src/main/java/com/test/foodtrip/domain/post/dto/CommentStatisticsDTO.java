package com.test.foodtrip.domain.post.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentStatisticsDTO {
    private Long commentId;
    private Long likeCount;
    private Long dislikeCount;
    private Long replyCount;
    private Boolean isPopular;
    private Boolean isBlinded;
}