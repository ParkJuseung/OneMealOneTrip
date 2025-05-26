package com.test.foodtrip.domain.user.service;

import com.test.foodtrip.domain.user.dto.VisitorStatsDTO;

public interface VisitorService {
    /**
     * 주어진 사용자(pageOwnerId)에 대한
     * 당일·주간·월간·누적 방문자 통계를 반환한다.
     */
    VisitorStatsDTO getStats(Long pageOwnerId);
    void recordVisit(Long pageOwnerId, Long visitorUserId);
}
