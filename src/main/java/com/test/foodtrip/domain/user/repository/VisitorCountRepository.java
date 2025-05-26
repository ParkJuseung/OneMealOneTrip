package com.test.foodtrip.domain.user.repository;

import com.test.foodtrip.domain.user.entity.VisitorCount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

/**
 * 방문자 집계를 위한 Repository
 */
@Repository
public interface VisitorCountRepository extends JpaRepository<VisitorCount, Long> {

    /**
     * 특정 기간(start~end) 동안의 방문 횟수 합계를 조회
     * (당일 페이지뷰 등)
     */
    long countByUserIdAndVisitDateBetween(Long userId,
                                          LocalDate start,
                                          LocalDate end);

    /**
     * 특정 시점 이후부터(>= since) 모든 방문 횟수 합계를 조회
     * (주간(7일), 월간(30일) 집계에 사용)
     */
    long countByUserIdAndVisitDateGreaterThanEqual(Long userId,
                                                   LocalDate since);

    /**
     * 전체 누적 방문 횟수 조회
     */
    long countByUserId(Long userId);


    /**
     * userId(pageOwner)와 visitorUserId(방문자)의
     * today~today (즉, 같은 날짜) 동안 방문 기록이 있는지 여부
     */
    boolean existsByUser_IdAndVisitorUser_IdAndVisitDateBetween(
            Long userId,
            Long visitorUserId,
            LocalDate startOfDay,
            LocalDate endOfDay);
}
