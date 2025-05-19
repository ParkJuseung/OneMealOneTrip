package com.test.foodtrip.domain.post.repository;

import com.test.foodtrip.domain.post.entity.Comment;
import com.test.foodtrip.domain.post.entity.Post;
import org.apache.ibatis.annotations.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    /**
     게시글에 달린 최상위 댓글을 페이징 하여 조회 ( 대댓글 제외 )
     Page : 페이징 처리된 결과를 담는 객체
     - ex)

     Pageable : Pageable 매개 변수를 통해서 페이지 번호, 페이지 크기, 정렬 조건 등을 지정할 수 있다.
     - ex)
     */
    Page<Comment> findByPostAndParentIsNullOrderByCreatedAtDesc(Post post, Pageable pageable);


    /**
     특정 댓글에 달린 대댓글 조회
     */
    List<Comment> findByParentOrderByCreatedAtAsc(Comment parent);

    /**
     인기 댓글(좋아요 수 기준) 상위 3개 조회
     */
    @Query("SELECT c FROM Comment c JOIN c.reactions r " +
            "WHERE c.post = :post AND c.parent IS NULL " +
            "GROUP BY c " +
            "HAVING COUNT(CASE WHEN r.reactionType = 'LIKE' THEN 1 END) >= 5 " +
            "ORDER BY COUNT(CASE WHEN r.reactionType = 'LIKE' THEN 1 END) DESC")
    List<Comment> findTopPopularComments(@Param("post") Post post, Pageable pageable);


}
