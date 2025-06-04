package com.test.foodtrip.domain.post.controller;

import com.test.foodtrip.domain.post.dto.*;
import com.test.foodtrip.domain.post.entity.Comment;
import com.test.foodtrip.domain.post.service.CommentService;
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

@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    // 로그인 상태를 확인하는 헬퍼 메서드
    private boolean isLoggedIn(HttpSession session) {
        return session != null && session.getAttribute("user_id") != null;
    }

    // 댓글 목록 조회 (페이징) - 로그인 불필요 (읽기 권한)
    @GetMapping("/post/{postId}")
    public ResponseEntity<ApiResponse<PageResultDTO<CommentDTO, Comment>>> getComments(
    		@PathVariable("postId") Long postId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").ascending());
            PageResultDTO<CommentDTO, Comment> result = commentService.getCommentsByPostId(postId, pageable);
            return ResponseEntity.ok(ApiResponse.success(result));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("댓글 조회 중 오류가 발생했습니다: " + e.getMessage()));
        }
    }

    // 댓글 작성 - 로그인 필요
    @PostMapping
    public ResponseEntity<ApiResponse<CommentDTO>> createComment(
            @RequestBody CommentCreateRequest request,
            HttpSession session) {
        try {
            if (!isLoggedIn(session)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponse.error("로그인이 필요합니다."));
            }

            CommentDTO comment = commentService.createComment(request);
            return ResponseEntity.ok(ApiResponse.success(comment));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("댓글 작성 중 오류가 발생했습니다: " + e.getMessage()));
        }
    }

    // 댓글 수정 - 로그인 필요
    @PutMapping("/{commentId}")
    public ResponseEntity<ApiResponse<CommentDTO>> updateComment(
    		@PathVariable("commentId") Long commentId,
            @RequestBody CommentUpdateRequest request,
            HttpSession session) {
        try {
            if (!isLoggedIn(session)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponse.error("로그인이 필요합니다."));
            }

            CommentDTO comment = commentService.updateComment(commentId, request);
            return ResponseEntity.ok(ApiResponse.success(comment));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("댓글 수정 중 오류가 발생했습니다"));
        }
    }

    // 댓글 삭제 - 로그인 필요
    @DeleteMapping("/{commentId}")
    public ResponseEntity<ApiResponse<String>> deleteComment(
    		@PathVariable("commentId") Long commentId,
            HttpSession session) {
        try {
            if (!isLoggedIn(session)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponse.error("로그인이 필요합니다."));
            }

            commentService.deleteComment(commentId);
            return ResponseEntity.ok(ApiResponse.success("댓글이 삭제되었습니다."));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("댓글 삭제 중 오류가 발생했습니다"));
        }
    }

    // 댓글 좋아요/싫어요 - 로그인 필요
    @PostMapping("/{commentId}/reaction")
    public ResponseEntity<ApiResponse<CommentReactionResultDTO>> toggleReaction(
    		@PathVariable("commentId") Long commentId,
            @RequestBody CommentReactionRequest request,
            HttpSession session) {
        try {
            if (!isLoggedIn(session)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponse.error("로그인이 필요합니다."));
            }

            CommentReactionResultDTO result = commentService.toggleReaction(commentId, request);
            return ResponseEntity.ok(ApiResponse.success(result));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("반응 처리 중 오류가 발생했습니다"));
        }
    }

    // 댓글 통계 (좋아요/싫어요 수) - 로그인 불필요
    @GetMapping("/{commentId}/statistics")
    public ResponseEntity<ApiResponse<CommentStatisticsDTO>> getCommentStatistics(@PathVariable("commentId") Long commentId) {
        try {
            CommentStatisticsDTO statistics = commentService.getCommentStatistics(commentId);
            return ResponseEntity.ok(ApiResponse.success(statistics));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("통계 조회 중 오류가 발생했습니다"));
        }
    }

    // 인기 댓글 조회 (좋아요 5개 이상) - 로그인 불필요
    @GetMapping("/post/{postId}/popular")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getPopularComments(@PathVariable("postId") Long postId) {
        try {
            var popularComments = commentService.getPopularComments(postId);
            var totalComments = commentService.getTotalCommentCount(postId);

            Map<String, Object> result = new HashMap<>();
            result.put("popularComments", popularComments);
            result.put("totalComments", totalComments);

            return ResponseEntity.ok(ApiResponse.success(result));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("인기 댓글 조회 중 오류가 발생했습니다"));
        }
    }

    // 대댓글 조회 - 로그인 불필요
    @GetMapping("/{parentCommentId}/replies")
    public ResponseEntity<ApiResponse<java.util.List<CommentDTO>>> getReplies( @PathVariable("parentCommentId") Long parentCommentId) {
        try {
            var replies = commentService.getReplies(parentCommentId);
            return ResponseEntity.ok(ApiResponse.success(replies));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("대댓글 조회 중 오류가 발생했습니다"));
        }
    }

    // 댓글 단건 조회 - 로그인 불필요
    @GetMapping("/{commentId}")
    public ResponseEntity<ApiResponse<CommentDTO>> getComment(@PathVariable("commentId") Long commentId) {
        try {
            CommentDTO comment = commentService.getComment(commentId);
            return ResponseEntity.ok(ApiResponse.success(comment));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("댓글을 찾을 수 없습니다"));
        }
    }

    // 디버깅용 테스트 엔드포인트
    @GetMapping("/test/{postId}")
    public ResponseEntity<String> testComments(@PathVariable Long postId) {
        try {
            Long count = commentService.getTotalCommentCount(postId);
            return ResponseEntity.ok("PostID " + postId + "의 댓글 수: " + count);
        } catch (Exception e) {
            return ResponseEntity.ok("에러: " + e.getMessage());
        }
    }
}