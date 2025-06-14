package com.test.foodtrip.domain.post.repository;

import com.test.foodtrip.domain.post.entity.PostTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PostTagRepository extends JpaRepository<PostTag, Long> {
    Optional<PostTag> findByTagText(String tagText);

    @Query("SELECT pt FROM PostTag pt WHERE pt.tagText IN :tagTexts")
    List<PostTag> findAllByTagTextIn(@Param("tagTexts") List<String> tagTexts);

    @Query(value = """
    SELECT pt.tag_text, COUNT(ptg.post_id) as tag_count 
    FROM POSTTAG pt 
    JOIN POSTTAGGING ptg ON pt.post_tag_id = ptg.post_tag_id 
    GROUP BY pt.tag_text 
    ORDER BY tag_count DESC 
    FETCH FIRST 10 ROWS ONLY
    """, nativeQuery = true)
    List<Object[]> findTopTags();
}