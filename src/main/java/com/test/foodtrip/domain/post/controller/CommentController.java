package com.test.foodtrip.domain.post.controller;

import com.test.foodtrip.domain.post.dto.*;
import com.test.foodtrip.domain.post.entity.Comment;
import com.test.foodtrip.domain.post.service.CommentService;
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

    // 디버깅용 단순 엔드포인트 추가
    @GetMapping("/test/{postId}")
    public ResponseEntity<String> testComments(@PathVariable Long postId) {
        try {
            Long count = commentService.getTotalCommentCount(postId);
            return ResponseEntity.ok("PostID " + postId + "의 댓글 수: " + count);
        } catch (Exception e) {
            return ResponseEntity.ok("에러: " + e.getMessage());
        }
    }

    // 댓글 목록 조회 (페이징)
    @GetMapping("/post/{postId}")
    public ResponseEntity<ApiResponse<PageResultDTO<CommentDTO, Comment>>> getComments(
            @PathVariable Long postId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        try {
            System.out.println("댓글 조회 요청 - PostID: " + postId + ", Page: " + page); // 로그 추가

            Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").ascending());
            PageResultDTO<CommentDTO, Comment> result = commentService.getCommentsByPostId(postId, pageable);


            return ResponseEntity.ok(ApiResponse.success(result));
        } catch (Exception e) {
            e.printStackTrace(); // 에러 스택트레이스 출력
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("댓글 조회 중 오류가 발생했습니다: " + e.getMessage()));
        }
    }

    // 댓글 작성
    @PostMapping
    public ResponseEntity<ApiResponse<CommentDTO>> createComment(@RequestBody CommentCreateRequest request) {
        try {
            CommentDTO comment = commentService.createComment(request);
            return ResponseEntity.ok(ApiResponse.success(comment));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("댓글 작성 중 오류가 발생했습니다."));
        }
    }

    // 댓글 수정
    @PutMapping("/{commentId}")
    public ResponseEntity<ApiResponse<CommentDTO>> updateComment(
            @PathVariable Long commentId,
            @RequestBody CommentUpdateRequest request) {
        try {
            CommentDTO comment = commentService.updateComment(commentId, request);
            return ResponseEntity.ok(ApiResponse.success(comment));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("댓글 수정 중 오류가 발생했습니다."));
        }
    }

    // 댓글 삭제
    @DeleteMapping("/{commentId}")
    public ResponseEntity<ApiResponse<String>> deleteComment(@PathVariable Long commentId) {
        try {
            commentService.deleteComment(commentId);
            return ResponseEntity.ok(ApiResponse.success("댓글이 삭제되었습니다."));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("댓글 삭제 중 오류가 발생했습니다."));
        }
    }

    // 댓글 좋아요/싫어요
    @PostMapping("/{commentId}/reaction")
    public ResponseEntity<ApiResponse<CommentReactionResultDTO>> toggleReaction(
            @PathVariable Long commentId,
            @RequestBody CommentReactionRequest request) {
        try {
            CommentReactionResultDTO result = commentService.toggleReaction(commentId, request);
            return ResponseEntity.ok(ApiResponse.success(result));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("반응 처리 중 오류가 발생했습니다."));
        }
    }

    // 댓글 통계 (좋아요/싫어요 수)
    @GetMapping("/{commentId}/statistics")
    public ResponseEntity<ApiResponse<CommentStatisticsDTO>> getCommentStatistics(@PathVariable Long commentId) {
        try {
            CommentStatisticsDTO statistics = commentService.getCommentStatistics(commentId);
            return ResponseEntity.ok(ApiResponse.success(statistics));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("통계 조회 중 오류가 발생했습니다."));
        }
    }

    // 인기 댓글 조회 (좋아요 5개 이상)
    @GetMapping("/post/{postId}/popular")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getPopularComments(@PathVariable Long postId) {
        try {
            var popularComments = commentService.getPopularComments(postId);
            var totalComments = commentService.getTotalCommentCount(postId);

            Map<String, Object> result = new HashMap<>();
            result.put("popularComments", popularComments);
            result.put("totalComments", totalComments);

            return ResponseEntity.ok(ApiResponse.success(result));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("인기 댓글 조회 중 오류가 발생했습니다."));
        }
    }
}