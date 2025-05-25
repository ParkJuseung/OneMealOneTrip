package com.test.foodtrip.domain.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 사용자의 방문자 통계(당일·주간·월간·누적)를 담는 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VisitorStatsDTO {
    /** 오늘(당일) 방문 횟수 또는 방문자 수 */
    private long todayCount;
    /** 최근 7일(주간) 방문 횟수 또는 방문자 수 */
    private long weekCount;
    /** 최근 30일(월간) 방문 횟수 또는 방문자 수 */
    private long monthCount;
    /** 전체 누적 방문 횟수 또는 방문자 수 */
    private long totalCount;
}
