package com.test.foodtrip.domain.post.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter @Setter
@NoArgsConstructor
public class CommentWithRepliesDTO {
    private Long id;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean isDeleted;
    private CommentUserDTO user;
    private List<CommentDTO> replies = new ArrayList<>();
    private long likeCount;
    private long dislikeCount;
    private boolean popular;
    private boolean blinded;
}

