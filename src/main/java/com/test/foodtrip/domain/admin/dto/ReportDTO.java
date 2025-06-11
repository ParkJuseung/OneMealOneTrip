package com.test.foodtrip.domain.admin.dto;

import com.test.foodtrip.domain.admin.entity.Report;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

@Schema(description = "신고 정보 DTO")
@Getter @Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportDTO {

    @Schema(description = "신고 ID", example = "1")
    private Long id;

    @Schema(description = "신고자 ID", example = "123")
    private Long reporterId;

    @Schema(description = "신고자 닉네임", example = "user123")
    private String reporterNickname;

    @Schema(description = "신고당한 사용자 ID", example = "456")
    private Long reportedId;

    @Schema(description = "신고당한 사용자 닉네임", example = "user456")
    private String reportedNickname;

    @Schema(description = "신고 유형", example = "COMMENT")
    private String reportType;

    @Schema(description = "신고 대상 ID", example = "789")
    private Long targetId;

    @Schema(description = "신고 사유", example = "INAPPROPRIATE")
    private String reason;

    @Schema(description = "신고 상세 내용", example = "부적절한 언어 사용")
    private String detail;

    @Schema(description = "신고 상태", example = "PENDING")
    private String status;

    @Schema(description = "신고 일시", example = "2025-06-09T10:30:00")
    private LocalDateTime createdAt;

    @Schema(description = "처리 일시", example = "2025-06-09T15:30:00")
    private LocalDateTime processedAt;

    @Schema(description = "처리자 ID", example = "999")
    private Long processorId;

    @Schema(description = "처리자 닉네임", example = "admin")
    private String processorNickname;

    // Entity to DTO 변환 메서드
    public static ReportDTO from(Report report) {
        return ReportDTO.builder()
                .id(report.getId())
                .reporterId(report.getReporter() != null ? report.getReporter().getId() : null)
                .reporterNickname(report.getReporter() != null ? report.getReporter().getNickname() : null)
                .reportedId(report.getReported() != null ? report.getReported().getId() : null)
                .reportedNickname(report.getReported() != null ? report.getReported().getNickname() : null)
                .reportType(report.getReportType())
                .targetId(report.getTargetId())
                .reason(report.getReason())
                .detail(report.getDetail())
                .status(report.getStatus())
                .createdAt(report.getCreatedAt())
                .processedAt(report.getProcessedAt())
                .processorId(report.getProcessor() != null ? report.getProcessor().getId() : null)
                .processorNickname(report.getProcessor() != null ? report.getProcessor().getNickname() : null)
                .build();
    }
}