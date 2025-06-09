package com.test.foodtrip.domain.admin.repository;

import com.test.foodtrip.domain.admin.entity.Report;
import com.test.foodtrip.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReportRepository extends JpaRepository<Report, Long> {

    /**
     * 특정 사용자가 특정 대상을 이미 신고했는지 확인
     */
    boolean existsByReporterAndReportTypeAndTargetIdAndStatus(
            User reporter,
            String reportType,
            Long targetId,
            String status
    );

    /**
     * 특정 대상의 총 신고 수 조회
     */
    int countByReportTypeAndTargetIdAndStatus(
            String reportType,
            Long targetId,
            String status
    );

    /**
     * 특정 사용자의 특정 대상에 대한 신고 조회
     */
    Optional<Report> findByReporterIdAndReportTypeAndTargetIdAndStatus(
            Long reporterId,
            String reportType,
            Long targetId,
            String status
    );

    /**
     * 특정 댓글에 대한 모든 신고 조회 (관리자용)
     */
    @Query("SELECT r FROM Report r WHERE r.reportType = 'COMMENT' AND r.targetId = :commentId ORDER BY r.createdAt DESC")
    java.util.List<Report> findAllByCommentId(@Param("commentId") Long commentId);

    /**
     * 특정 사용자가 접수한 모든 신고 조회
     */
    @Query("SELECT r FROM Report r WHERE r.reporter.id = :reporterId ORDER BY r.createdAt DESC")
    java.util.List<Report> findAllByReporterId(@Param("reporterId") Long reporterId);

    /**
     * 특정 사용자에 대한 모든 신고 조회
     */
    @Query("SELECT r FROM Report r WHERE r.reported.id = :reportedId ORDER BY r.createdAt DESC")
    java.util.List<Report> findAllByReportedId(@Param("reportedId") Long reportedId);

    /**
     * 상태별 신고 조회
     */
    @Query("SELECT r FROM Report r WHERE r.status = :status ORDER BY r.createdAt DESC")
    java.util.List<Report> findAllByStatus(@Param("status") String status);

    /**
     * 처리 대기 중인 신고 수 조회
     */
    long countByStatus(String status);
}