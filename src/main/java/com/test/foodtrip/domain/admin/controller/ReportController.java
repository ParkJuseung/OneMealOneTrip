package com.test.foodtrip.domain.admin.controller;

import com.test.foodtrip.domain.admin.dto.ReportCreateRequestDTO;
import com.test.foodtrip.domain.admin.dto.ReportDTO;
import com.test.foodtrip.domain.admin.service.ReportService;
import com.test.foodtrip.domain.post.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "신고 API", description = "댓글 및 게시글 신고 관련 REST API 컨트롤러")
@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    // 로그인 상태를 확인하는 헬퍼 메서드
    private boolean isLoggedIn(HttpSession session) {
        return session != null && session.getAttribute("user_id") != null;
    }

    @Operation(summary = "댓글 신고", description = "부적절한 댓글을 신고합니다. 로그인이 필요합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "신고 접수 성공"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "로그인이 필요합니다",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409",
                    description = "이미 신고한 댓글입니다",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            )
    })
    @PostMapping
    public ResponseEntity<ApiResponse<ReportDTO>> createReport(
            @Parameter(description = "신고 정보", required = true)
            @RequestBody ReportCreateRequestDTO request,
            HttpSession session) {

        try {
            if (!isLoggedIn(session)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponse.error("로그인이 필요합니다."));
            }

            Long reporterId = (Long) session.getAttribute("user_id");
            ReportDTO report = reportService.createReport(reporterId, request);

            return ResponseEntity.ok(ApiResponse.success(report));

        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (RuntimeException e) {
            if (e.getMessage().contains("이미 신고")) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(ApiResponse.error(e.getMessage()));
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("신고 접수 중 오류가 발생했습니다: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("서버 오류가 발생했습니다."));
        }
    }

    @Operation(summary = "신고 취소", description = "본인이 접수한 신고를 취소합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "신고 취소 성공"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "로그인이 필요합니다",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "신고 취소 권한이 없습니다",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "신고를 찾을 수 없습니다",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            )
    })
    @DeleteMapping("/{reportId}")
    public ResponseEntity<ApiResponse<String>> cancelReport(
            @Parameter(description = "신고 ID", required = true)
            @PathVariable("reportId") Long reportId,
            HttpSession session) {

        try {
            if (!isLoggedIn(session)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponse.error("로그인이 필요합니다."));
            }

            Long reporterId = (Long) session.getAttribute("user_id");
            reportService.cancelReport(reportId, reporterId);

            return ResponseEntity.ok(ApiResponse.success("신고가 취소되었습니다."));

        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("서버 오류가 발생했습니다."));
        }
    }

    @Operation(summary = "신고 현황 조회", description = "특정 댓글의 신고 현황을 조회합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "신고 현황 조회 성공"
            )
    })
    @GetMapping("/comment/{commentId}/status")
    public ResponseEntity<ApiResponse<Object>> getReportStatus(
            @Parameter(description = "댓글 ID", required = true)
            @PathVariable("commentId") Long commentId,
            HttpSession session) {

        try {
            Long userId = isLoggedIn(session) ? (Long) session.getAttribute("user_id") : null;
            Object status = reportService.getReportStatus(commentId, userId);

            return ResponseEntity.ok(ApiResponse.success(status));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("신고 현황 조회 중 오류가 발생했습니다."));
        }
    }
}