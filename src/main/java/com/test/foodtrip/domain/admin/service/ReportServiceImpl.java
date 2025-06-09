package com.test.foodtrip.domain.admin.service;

import com.test.foodtrip.domain.admin.dto.ReportCreateRequestDTO;
import com.test.foodtrip.domain.admin.dto.ReportDTO;
import com.test.foodtrip.domain.admin.dto.ReportStatusDTO;
import com.test.foodtrip.domain.admin.entity.Report;
import com.test.foodtrip.domain.admin.repository.ReportRepository;
import com.test.foodtrip.domain.post.entity.Comment;
import com.test.foodtrip.domain.post.repository.CommentRepository;
import com.test.foodtrip.domain.user.entity.User;
import com.test.foodtrip.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class ReportServiceImpl implements ReportService {

    private final ReportRepository reportRepository;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;

    @Override
    @Transactional
    public ReportDTO createReport(Long reporterId, ReportCreateRequestDTO request) {
        // 1. 신고자 조회
        User reporter = userRepository.findById(reporterId)
                .orElseThrow(() -> new IllegalStateException("로그인이 필요합니다."));

        // 2. 신고 대상 검증 및 신고당한 사용자 조회
        User reported = null;
        if ("COMMENT".equals(request.getReportType())) {
            Comment comment = commentRepository.findById(request.getTargetId())
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 댓글입니다."));

            // 본인 댓글 신고 방지
            if (comment.getUser().getId().equals(reporterId)) {
                throw new IllegalArgumentException("본인의 댓글은 신고할 수 없습니다.");
            }

            reported = comment.getUser();

            // 삭제된 댓글 신고 방지
            if (comment.isDeleted()) {
                throw new IllegalArgumentException("삭제된 댓글은 신고할 수 없습니다.");
            }
        } else {
            throw new IllegalArgumentException("지원하지 않는 신고 유형입니다: " + request.getReportType());
        }

        // 3. 중복 신고 방지
        boolean alreadyReported = reportRepository.existsByReporterAndReportTypeAndTargetIdAndStatus(
                reporter, request.getReportType(), request.getTargetId(), "PENDING");

        if (alreadyReported) {
            throw new RuntimeException("이미 신고한 " + getTargetTypeName(request.getReportType()) + "입니다.");
        }

        // 4. 신고 엔티티 생성 및 저장
        Report report = new Report();
        report.setReporter(reporter);
        report.setReported(reported);
        report.setReportType(request.getReportType());
        report.setTargetId(request.getTargetId());
        report.setReason(request.getReason());
        report.setDetail(request.getDetail());
        report.setStatus("PENDING");

        Report savedReport = reportRepository.save(report);

        log.info("신고 접수 완료 - 신고자: {}, 대상: {} (ID: {}), 사유: {}",
                reporter.getNickname(), request.getReportType(), request.getTargetId(), request.getReason());

        return ReportDTO.from(savedReport);
    }

    @Override
    @Transactional
    public void cancelReport(Long reportId, Long reporterId) {
        // 1. 신고 조회
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 신고입니다."));

        // 2. 권한 확인 (본인이 접수한 신고만 취소 가능)
        if (!report.getReporter().getId().equals(reporterId)) {
            throw new RuntimeException("신고 취소 권한이 없습니다.");
        }

        // 3. 처리 상태 확인 (대기 상태인 경우만 취소 가능)
        if (!"PENDING".equals(report.getStatus())) {
            throw new RuntimeException("이미 처리된 신고는 취소할 수 없습니다.");
        }

        // 4. 신고 삭제
        reportRepository.delete(report);

        log.info("신고 취소 완료 - 신고자: {}, 신고 ID: {}",
                report.getReporter().getNickname(), reportId);
    }

    @Override
    public ReportStatusDTO getReportStatus(Long commentId, Long userId) {
        // 1. 댓글 조회
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 댓글입니다."));

        // 2. 해당 댓글의 총 신고 수 조회
        int totalReportCount = reportRepository.countByReportTypeAndTargetIdAndStatus(
                "COMMENT", commentId, "PENDING");

        // 3. 현재 사용자의 신고 여부 및 신고 ID 조회
        boolean isReportedByCurrentUser = false;
        Long currentUserReportId = null;

        if (userId != null) {
            Optional<Report> userReport = reportRepository.findByReporterIdAndReportTypeAndTargetIdAndStatus(
                    userId, "COMMENT", commentId, "PENDING");

            if (userReport.isPresent()) {
                isReportedByCurrentUser = true;
                currentUserReportId = userReport.get().getId();
            }
        }

        // 4. 블라인드 처리 여부 확인 (싫어요 10개 이상)
        boolean isBlinded = comment.isBlinded();

        // 5. 신고 가능 여부 판단
        boolean canReport = true;
        String cannotReportReason = null;

        if (userId == null) {
            canReport = false;
            cannotReportReason = "로그인이 필요합니다.";
        } else if (comment.getUser().getId().equals(userId)) {
            canReport = false;
            cannotReportReason = "본인의 댓글은 신고할 수 없습니다.";
        } else if (comment.isDeleted()) {
            canReport = false;
            cannotReportReason = "삭제된 댓글은 신고할 수 없습니다.";
        } else if (isReportedByCurrentUser) {
            canReport = false;
            cannotReportReason = "이미 신고한 댓글입니다.";
        }

        return ReportStatusDTO.builder()
                .totalReportCount(totalReportCount)
                .isReportedByCurrentUser(isReportedByCurrentUser)
                .currentUserReportId(currentUserReportId)
                .isBlinded(isBlinded)
                .canReport(canReport)
                .cannotReportReason(cannotReportReason)
                .build();
    }

    /**
     * 신고 대상 유형명 반환
     */
    private String getTargetTypeName(String reportType) {
        switch (reportType) {
            case "COMMENT":
                return "댓글";
            case "POST":
                return "게시글";
            default:
                return "콘텐츠";
        }
    }
}