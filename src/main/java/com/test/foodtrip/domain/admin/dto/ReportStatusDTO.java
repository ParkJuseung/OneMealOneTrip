package com.test.foodtrip.domain.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Schema(description = "신고 상태 정보 DTO")
@Getter @Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportStatusDTO {

    @Schema(description = "총 신고 수", example = "5")
    private int totalReportCount;

    @Schema(description = "현재 사용자의 신고 여부", example = "true")
    private boolean isReportedByCurrentUser;

    @Schema(description = "현재 사용자의 신고 ID (신고한 경우)", example = "123")
    private Long currentUserReportId;

    @Schema(description = "댓글 블라인드 처리 여부", example = "false")
    private boolean isBlinded;

    @Schema(description = "신고 가능 여부", example = "true")
    private boolean canReport;

    @Schema(description = "신고 불가 사유", example = "본인 댓글은 신고할 수 없습니다")
    private String cannotReportReason;
}