package com.test.foodtrip.domain.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "신고 생성 요청 DTO")
@Getter @Setter
public class ReportCreateRequestDTO {

    @Schema(description = "신고 대상 유형", example = "COMMENT", allowableValues = {"COMMENT", "POST"})
    @NotBlank(message = "신고 유형은 필수입니다")
    private String reportType;

    @Schema(description = "신고 대상 ID (댓글 ID 또는 게시글 ID)", example = "123")
    @NotNull(message = "신고 대상 ID는 필수입니다")
    private Long targetId;

    @Schema(description = "신고 사유", example = "INAPPROPRIATE",
            allowableValues = {"SPAM", "INAPPROPRIATE", "HARASSMENT", "HATE_SPEECH", "OTHER"})
    @NotBlank(message = "신고 사유는 필수입니다")
    private String reason;

    @Schema(description = "신고 상세 내용", example = "부적절한 언어 사용")
    @Size(max = 1000, message = "상세 내용은 1000자를 초과할 수 없습니다")
    private String detail;

    @Schema(description = "신고당한 사용자 ID", example = "456")
    private Long reportedId;
}