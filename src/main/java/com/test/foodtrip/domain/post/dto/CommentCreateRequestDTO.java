package com.test.foodtrip.domain.post.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CommentCreateRequestDTO {

    private Long postId;
    private String content;
    private Long parentId;

}
