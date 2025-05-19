package com.test.foodtrip.domain.post.repository;

import com.test.foodtrip.domain.post.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post, Long> {

}
