package com.test.foodtrip.domain.post.controller;

import com.test.foodtrip.domain.post.dto.*;
import com.test.foodtrip.domain.post.entity.Comment;
import com.test.foodtrip.domain.post.service.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Tag(name = "댓글 API", description = "게시글 댓글 관련 REST API 컨트롤러")
@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    // 로그인 상태를 확인하는 헬퍼 메서드
    private boolean isLoggedIn(HttpSession session) {
        return session != null && session.getAttribute("user_id") != null;
    }

    @Operation(summary = "댓글 목록 조회", description = "특정 게시글에 달린 댓글 목록을 페이징하여 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "댓글 목록 조회 성공"),
            @ApiResponse(responseCode = "500", description = "서버 오류",
                    content = @Content(schema = @Schema(implementation = com.test.foodtrip.domain.post.dto.ApiResponse.class)))
    })
    @GetMapping("/post/{postId}")
    public ResponseEntity<com.test.foodtrip.domain.post.dto.ApiResponse<PageResultDTO<CommentDTO, Comment>>> getComments(
            @Parameter(description = "게시글 ID", required = true) @PathVariable("postId") Long postId,
            @Parameter(description = "페이지 번호 (0부터 시작)", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기", example = "20") @RequestParam(defaultValue = "20") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").ascending());
            PageResultDTO<CommentDTO, Comment> result = commentService.getCommentsByPostId(postId, pageable);
            return ResponseEntity.ok(com.test.foodtrip.domain.post.dto.ApiResponse.success(result));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(com.test.foodtrip.domain.post.dto.ApiResponse.error("댓글 조회 중 오류가 발생했습니다: " + e.getMessage()));
        }
    }

    @Operation(summary = "댓글 작성", description = "새로운 댓글을 작성합니다. 로그인이 필요합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "댓글 작성 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청",
                    content = @Content(schema = @Schema(implementation = com.test.foodtrip.domain.post.dto.ApiResponse.class))),
            @ApiResponse(responseCode = "401", description = "로그인이 필요합니다",
                    content = @Content(schema = @Schema(implementation = com.test.foodtrip.domain.post.dto.ApiResponse.class)))
    })
    @PostMapping
    public ResponseEntity<com.test.foodtrip.domain.post.dto.ApiResponse<CommentDTO>> createComment(
            @Parameter(description = "댓글 작성 정보", required = true) @RequestBody CommentCreateRequest request,
            HttpSession session) {
        try {
            if (!isLoggedIn(session)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(com.test.foodtrip.domain.post.dto.ApiResponse.error("로그인이 필요합니다."));
            }

            CommentDTO comment = commentService.createComment(request);
            return ResponseEntity.ok(com.test.foodtrip.domain.post.dto.ApiResponse.success(comment));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(com.test.foodtrip.domain.post.dto.ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(com.test.foodtrip.domain.post.dto.ApiResponse.error("댓글 작성 중 오류가 발생했습니다: " + e.getMessage()));
        }
    }

    @Operation(summary = "댓글 수정", description = "기존 댓글을 수정합니다. 본인이 작성한 댓글만 수정 가능합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "댓글 수정 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청",
                    content = @Content(schema = @Schema(implementation = com.test.foodtrip.domain.post.dto.ApiResponse.class))),
            @ApiResponse(responseCode = "401", description = "로그인이 필요합니다",
                    content = @Content(schema = @Schema(implementation = com.test.foodtrip.domain.post.dto.ApiResponse.class))),
            @ApiResponse(responseCode = "403", description = "댓글 수정 권한이 없습니다",
                    content = @Content(schema = @Schema(implementation = com.test.foodtrip.domain.post.dto.ApiResponse.class)))
    })
    @PutMapping("/{commentId}")
    public ResponseEntity<com.test.foodtrip.domain.post.dto.ApiResponse<CommentDTO>> updateComment(
            @Parameter(description = "수정할 댓글 ID", required = true) @PathVariable("commentId") Long commentId,
            @Parameter(description = "수정할 댓글 정보", required = true) @RequestBody CommentUpdateRequest request,
            HttpSession session) {
        try {
            if (!isLoggedIn(session)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(com.test.foodtrip.domain.post.dto.ApiResponse.error("로그인이 필요합니다."));
            }

            CommentDTO comment = commentService.updateComment(commentId, request);
            return ResponseEntity.ok(com.test.foodtrip.domain.post.dto.ApiResponse.success(comment));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(com.test.foodtrip.domain.post.dto.ApiResponse.error(e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(com.test.foodtrip.domain.post.dto.ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(com.test.foodtrip.domain.post.dto.ApiResponse.error("댓글 수정 중 오류가 발생했습니다"));
        }
    }

    @Operation(summary = "댓글 삭제", description = "특정 댓글을 삭제합니다. 본인이 작성한 댓글만 삭제 가능합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "댓글 삭제 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청",
                    content = @Content(schema = @Schema(implementation = com.test.foodtrip.domain.post.dto.ApiResponse.class))),
            @ApiResponse(responseCode = "401", description = "로그인이 필요합니다",
                    content = @Content(schema = @Schema(implementation = com.test.foodtrip.domain.post.dto.ApiResponse.class))),
            @ApiResponse(responseCode = "403", description = "댓글 삭제 권한이 없습니다",
                    content = @Content(schema = @Schema(implementation = com.test.foodtrip.domain.post.dto.ApiResponse.class)))
    })
    @DeleteMapping("/{commentId}")
    public ResponseEntity<com.test.foodtrip.domain.post.dto.ApiResponse<String>> deleteComment(
            @Parameter(description = "삭제할 댓글 ID", required = true) @PathVariable("commentId") Long commentId,
            HttpSession session) {
        try {
            if (!isLoggedIn(session)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(com.test.foodtrip.domain.post.dto.ApiResponse.error("로그인이 필요합니다."));
            }

            commentService.deleteComment(commentId);
            return ResponseEntity.ok(com.test.foodtrip.domain.post.dto.ApiResponse.success("댓글이 삭제되었습니다."));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(com.test.foodtrip.domain.post.dto.ApiResponse.error(e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(com.test.foodtrip.domain.post.dto.ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(com.test.foodtrip.domain.post.dto.ApiResponse.error("댓글 삭제 중 오류가 발생했습니다"));
        }
    }

    @Operation(summary = "댓글 좋아요/싫어요 토글", description = "댓글에 좋아요 또는 싫어요 반응을 추가하거나 취소합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "반응 처리 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청",
                    content = @Content(schema = @Schema(implementation = com.test.foodtrip.domain.post.dto.ApiResponse.class))),
            @ApiResponse(responseCode = "401", description = "로그인이 필요합니다",
                    content = @Content(schema = @Schema(implementation = com.test.foodtrip.domain.post.dto.ApiResponse.class)))
    })
    @PostMapping("/{commentId}/reaction")
    public ResponseEntity<com.test.foodtrip.domain.post.dto.ApiResponse<CommentReactionResultDTO>> toggleReaction(
            @Parameter(description = "댓글 ID", required = true) @PathVariable("commentId") Long commentId,
            @Parameter(description = "반응 정보", required = true) @RequestBody CommentReactionRequest request,
            HttpSession session) {
        try {
            if (!isLoggedIn(session)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(com.test.foodtrip.domain.post.dto.ApiResponse.error("로그인이 필요합니다."));
            }

            CommentReactionResultDTO result = commentService.toggleReaction(commentId, request);
            return ResponseEntity.ok(com.test.foodtrip.domain.post.dto.ApiResponse.success(result));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(com.test.foodtrip.domain.post.dto.ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(com.test.foodtrip.domain.post.dto.ApiResponse.error("반응 처리 중 오류가 발생했습니다"));
        }
    }

    @Operation(summary = "댓글 통계 조회", description = "특정 댓글의 좋아요/싫어요 수를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "통계 조회 성공"),
            @ApiResponse(responseCode = "500", description = "서버 오류",
                    content = @Content(schema = @Schema(implementation = com.test.foodtrip.domain.post.dto.ApiResponse.class)))
    })
    @GetMapping("/{commentId}/statistics")
    public ResponseEntity<com.test.foodtrip.domain.post.dto.ApiResponse<CommentStatisticsDTO>> getCommentStatistics(
            @Parameter(description = "댓글 ID", required = true) @PathVariable("commentId") Long commentId) {
        try {
            CommentStatisticsDTO statistics = commentService.getCommentStatistics(commentId);
            return ResponseEntity.ok(com.test.foodtrip.domain.post.dto.ApiResponse.success(statistics));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(com.test.foodtrip.domain.post.dto.ApiResponse.error("통계 조회 중 오류가 발생했습니다"));
        }
    }

    @Operation(summary = "인기 댓글 조회", description = "특정 게시글의 인기 댓글(좋아요 5개 이상)을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "인기 댓글 조회 성공"),
            @ApiResponse(responseCode = "500", description = "서버 오류",
                    content = @Content(schema = @Schema(implementation = com.test.foodtrip.domain.post.dto.ApiResponse.class)))
    })
    @GetMapping("/post/{postId}/popular")
    public ResponseEntity<com.test.foodtrip.domain.post.dto.ApiResponse<Map<String, Object>>> getPopularComments(
            @Parameter(description = "게시글 ID", required = true) @PathVariable("postId") Long postId) {
        try {
            var popularComments = commentService.getPopularComments(postId);
            var totalComments = commentService.getTotalCommentCount(postId);

            Map<String, Object> result = new HashMap<>();
            result.put("popularComments", popularComments);
            result.put("totalComments", totalComments);

            return ResponseEntity.ok(com.test.foodtrip.domain.post.dto.ApiResponse.success(result));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(com.test.foodtrip.domain.post.dto.ApiResponse.error("인기 댓글 조회 중 오류가 발생했습니다"));
        }
    }

    @Operation(summary = "대댓글 조회", description = "특정 댓글에 달린 대댓글 목록을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "대댓글 조회 성공"),
            @ApiResponse(responseCode = "500", description = "서버 오류",
                    content = @Content(schema = @Schema(implementation = com.test.foodtrip.domain.post.dto.ApiResponse.class)))
    })
    @GetMapping("/{parentCommentId}/replies")
    public ResponseEntity<com.test.foodtrip.domain.post.dto.ApiResponse<java.util.List<CommentDTO>>> getReplies(
            @Parameter(description = "부모 댓글 ID", required = true) @PathVariable("parentCommentId") Long parentCommentId) {
        try {
            var replies = commentService.getReplies(parentCommentId);
            return ResponseEntity.ok(com.test.foodtrip.domain.post.dto.ApiResponse.success(replies));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(com.test.foodtrip.domain.post.dto.ApiResponse.error("대댓글 조회 중 오류가 발생했습니다"));
        }
    }

    @Operation(summary = "댓글 단건 조회", description = "특정 댓글의 상세 정보를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "댓글 조회 성공"),
            @ApiResponse(responseCode = "404", description = "댓글을 찾을 수 없습니다",
                    content = @Content(schema = @Schema(implementation = com.test.foodtrip.domain.post.dto.ApiResponse.class)))
    })
    @GetMapping("/{commentId}")
    public ResponseEntity<com.test.foodtrip.domain.post.dto.ApiResponse<CommentDTO>> getComment(
            @Parameter(description = "댓글 ID", required = true) @PathVariable("commentId") Long commentId) {
        try {
            CommentDTO comment = commentService.getComment(commentId);
            return ResponseEntity.ok(com.test.foodtrip.domain.post.dto.ApiResponse.success(comment));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(com.test.foodtrip.domain.post.dto.ApiResponse.error("댓글을 찾을 수 없습니다"));
        }
    }

    @Operation(summary = "테스트 엔드포인트", description = "디버깅용 테스트 엔드포인트입니다.", hidden = true)
    @GetMapping("/test/{postId}")
    public ResponseEntity<String> testComments(
            @Parameter(description = "게시글 ID", required = true) @PathVariable Long postId) {
        try {
            Long count = commentService.getTotalCommentCount(postId);
            return ResponseEntity.ok("PostID " + postId + "의 댓글 수: " + count);
        } catch (Exception e) {
            return ResponseEntity.ok("에러: " + e.getMessage());
        }
    }
}