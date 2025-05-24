package com.test.foodtrip.domain.post.dto;

import com.test.foodtrip.domain.post.entity.enums.ReactionType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor
public class CommentReactionRequest {

    private ReactionType reactionType;  // LIKE 또는 DISLIKE
}