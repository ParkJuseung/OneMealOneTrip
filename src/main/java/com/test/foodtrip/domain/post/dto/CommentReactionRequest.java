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
    // userId 제거 - 세션에서 현재 로그인된 사용자를 가져오도록 변경

    @NotNull(message = "반응 타입은 필수입니다.")
    private ReactionType reactionType;
}