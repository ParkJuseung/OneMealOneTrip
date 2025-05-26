package com.test.foodtrip.domain.post.repository;

import com.test.foodtrip.domain.post.entity.PostImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PostImageRepository extends JpaRepository<PostImage, Long> {

    /**
     * 특정 게시글들의 모든 이미지를 순서대로 조회
     * N+1 문제 해결을 위해 사용
     */
    @Query("SELECT pi FROM PostImage pi WHERE pi.post.id IN :postIds ORDER BY pi.post.id, pi.imageOrder")
    List<PostImage> findByPostIdIn(@Param("postIds") List<Long> postIds);

    /**
     * 특정 게시글의 이미지들을 순서대로 조회
     */
    @Query("SELECT pi FROM PostImage pi WHERE pi.post.id = :postId ORDER BY pi.imageOrder")
    List<PostImage> findByPostIdOrderByImageOrder(@Param("postId") Long postId);

    /**
     * 특정 게시글의 첫 번째 이미지만 조회 (썸네일용)
     */
    @Query("SELECT pi FROM PostImage pi WHERE pi.post.id = :postId AND pi.imageOrder = 0")
    PostImage findFirstImageByPostId(@Param("postId") Long postId);

    /**
     * 여러 게시글들의 첫 번째 이미지들만 조회 (리스트 페이지 썸네일용)
     */
    @Query("SELECT pi FROM PostImage pi WHERE pi.post.id IN :postIds AND pi.imageOrder = 0")
    List<PostImage> findFirstImagesByPostIds(@Param("postIds") List<Long> postIds);
}