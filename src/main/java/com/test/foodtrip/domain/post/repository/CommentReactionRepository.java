package com.test.foodtrip.domain.post.repository;

import com.test.foodtrip.domain.post.entity.Comment;
import com.test.foodtrip.domain.post.entity.CommentReaction;
import com.test.foodtrip.domain.post.entity.enums.ReactionType;
import com.test.foodtrip.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import javax.swing.text.html.Option;
import java.util.Optional;

public interface CommentReactionRepository extends JpaRepository<CommentReaction, Long> {

    /**
    특정 사용자가 특정 댓글에 남긴 반응 조회

     Optional
     - 값이 있을수도 있고 없을 수도 있음을 표현하기 위해 사용
     - 값이 없을 경우 null 반환하는 문제가 생기기 때문에 사용
     - 처리 방식
        1. 값이 있을때만 처리 (ifPresent)
            optionalReaction.ifPresent(reaction -> {
                System.out.println("Reaction type : " + reaction.getType());
            });
        2. 값이 없을 경우 기본값 사용 (orElse)
            CommentReaction reaction = optionalReaction.orElse(new CommentReaction());
        3. 값이 없으면 예외 던지기 (orElseThrow)
            CommentReaction reaction = optionalReaction.orElseThrow(() ->
                new IllegalStateException("Reaction not found")
            );
        4. 값이 없으면 예외 발생 (map(Function))
        5. 중첩된 Optional 평탄화 (flatMap(Function))
     */
    Optional<CommentReaction> findByCommentAndUser(Comment comment, User user);

    /**
      특정 댓글의 특정 타입 반응 수 계산
     */
    long countByCommentAndReactionType(Comment comment, ReactionType reactionType);

    /**
     특정 댓글에 대한 모든 반응 삭제
    */
    void deleteAllByComment(Comment comment);


}
