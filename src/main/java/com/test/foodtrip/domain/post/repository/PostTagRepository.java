package com.test.foodtrip.domain.post.repository;

import com.test.foodtrip.domain.post.entity.PostTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PostTagRepository extends JpaRepository<PostTag, Long> {
    Optional<PostTag> findByTagText(String tagText);
}