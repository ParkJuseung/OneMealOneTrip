package com.test.foodtrip.domain.post.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Getter @Setter
@NoArgsConstructor
public class CommentCreateRequest {

    @NotBlank(message = "댓글 내용은 필수입니다")
    @Size(max = 3000, message = "댓글은 최대 3000자까지 입력 가능합니다")
    private String content;

    private Long parentId;  // 대댓글인 경우 부모 댓글 ID
}