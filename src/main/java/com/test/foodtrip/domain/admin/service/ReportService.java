
// ReportService.java
package com.test.foodtrip.domain.admin.service;

import com.test.foodtrip.domain.admin.dto.ReportCreateRequestDTO;
import com.test.foodtrip.domain.admin.dto.ReportDTO;
import com.test.foodtrip.domain.admin.dto.ReportStatusDTO;

public interface ReportService {

    /**
     * 신고 생성
     * @param reporterId 신고자 ID
     * @param request 신고 요청 정보
     * @return 생성된 신고 정보
     */
    ReportDTO createReport(Long reporterId, ReportCreateRequestDTO request);

    /**
     * 신고 취소
     * @param reportId 신고 ID
     * @param reporterId 신고자 ID (권한 확인용)
     */
    void cancelReport(Long reportId, Long reporterId);

    /**
     * 댓글 신고 현황 조회
     * @param commentId 댓글 ID
     * @param userId 현재 사용자 ID (null 가능)
     * @return 신고 현황 정보
     */
    ReportStatusDTO getReportStatus(Long commentId, Long userId);
}