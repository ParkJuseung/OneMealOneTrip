package com.test.foodtrip.domain.post.repository;

import com.test.foodtrip.domain.post.entity.PostTagging;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostTaggingRepository extends JpaRepository<PostTagging, PostTagging.PostTaggingId> {

    @Query("SELECT pt.tagText FROM PostTagging pg JOIN pg.postTag pt WHERE pg.post.id = :postId")
    List<String> findTagsByPostId(@Param("postId") Long postId);

    @Query("SELECT pg FROM PostTagging pg WHERE pg.post.id = :postId")
    List<PostTagging> findByPostId(@Param("postId") Long postId);


    @Query("""
    SELECT pt.post.id, pt.postTag.tagText 
    FROM PostTagging pt 
    WHERE pt.post.id IN :postIds
    """)
    List<Object[]> findTagsByPostIds(@Param("postIds") List<Long> postIds);
}