package com.test.foodtrip.domain.chat.repository;

import com.test.foodtrip.domain.chat.entity.Hashtag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

// 채팅방에서 사용되는 해시태그를 관리하는 Repository
// 채팅방 생성 시 해시태그 저장 및 검색에 사용
public interface HashtagRepository extends JpaRepository<Hashtag, Long> {

    // 태그 텍스트로 기존 해시태그 조회 (중복 저장 방지용)
    Optional<Hashtag> findByTagText(String tagText);

    @Query("SELECT h FROM Hashtag h WHERE LOWER(h.tagText) LIKE LOWER(CONCAT('%', :keyword, '%')) ORDER BY h.tagText ASC")
    List<Hashtag> findTop5ByTagTextContainingIgnoreCaseOrderByTagTextAsc(@Param("keyword") String keyword);

}