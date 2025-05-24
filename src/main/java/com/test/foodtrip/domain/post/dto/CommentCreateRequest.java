package com.test.foodtrip.domain.post.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommentCreateRequest {
    @NotNull(message = "게시글 ID는 필수입니다.")
    private Long postId;

    // userId 제거 - 세션에서 현재 로그인된 사용자를 가져오도록 변경

    @NotBlank(message = "댓글 내용은 필수입니다.")
    private String content;

    private Long parentId; // 대댓글인 경우 부모 댓글 ID
}