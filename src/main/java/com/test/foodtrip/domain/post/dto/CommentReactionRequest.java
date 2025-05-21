package com.test.foodtrip.domain.post.dto;

import com.test.foodtrip.domain.post.entity.enums.ReactionType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommentReactionRequest {
    @NotNull(message = "사용자 ID는 필수입니다.")
    private Long userId;

    @NotNull(message = "반응 타입은 필수입니다.")
    private ReactionType reactionType;
}