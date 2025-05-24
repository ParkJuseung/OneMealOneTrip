package com.test.foodtrip.domain.post.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommentUpdateRequest {
    // userId 제거 - 세션에서 현재 로그인된 사용자를 가져오도록 변경

    @NotBlank(message = "댓글 내용은 필수입니다.")
    private String content;
}