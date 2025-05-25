package com.test.foodtrip.domain.post.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentReactionResultDTO {
    private Boolean isToggled; // 반응이 추가/제거되었는지
    private String action; // "ADDED", "REMOVED", "CHANGED"
    private Long likeCount;
    private Long dislikeCount;
    private Boolean isLikedByCurrentUser;
    private Boolean isDislikedByCurrentUser;
}