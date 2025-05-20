package com.test.foodtrip.domain.post.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CommentUserDTO {
    private Long id;
    private String nickname;
    private String profileImage;
}