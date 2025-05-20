package com.test.foodtrip.domain.post.controller;

import com.test.foodtrip.domain.post.dto.CommentCreateRequest;
import com.test.foodtrip.domain.post.dto.CommentDTO;
import com.test.foodtrip.domain.post.dto.CommentReactionRequest;
import com.test.foodtrip.domain.post.dto.CommentUpdateRequest;
import com.test.foodtrip.domain.post.entity.enums.ReactionType;
import com.test.foodtrip.domain.post.service.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/posts/{postId}/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    /**
     * 댓글 목록 조회
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getComments(
            @PathVariable Long postId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal UserDetails userDetails) {

        Long currentUserId = null;
        if (userDetails != null) {
            // 실제 구현에서는 UserDetails에서 User ID를 가져오는 방식에 맞게 수정 필요
            currentUserId = Long.parseLong(userDetails.getUsername());
        }

        Page<CommentDTO> commentsPage = commentService.getCommentsByPost(postId, page, size, currentUserId);

        Map<String, Object> response = new HashMap<>();
        response.put("comments", commentsPage.getContent());
        response.put("currentPage", commentsPage.getNumber());
        response.put("totalItems", commentsPage.getTotalElements());
        response.put("totalPages", commentsPage.getTotalPages());

        return ResponseEntity.ok(response);
    }

    /**
     * 댓글 작성
     */
    @PostMapping
    public ResponseEntity<CommentDTO> createComment(
            @PathVariable Long postId,
            @Valid @RequestBody CommentCreateRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // 실제 구현에서는 UserDetails에서 User ID를 가져오는 방식에 맞게 수정 필요
        Long userId = Long.parseLong(userDetails.getUsername());

        CommentDTO createdComment = commentService.createComment(postId, request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdComment);
    }

    /**
     * 댓글 수정
     */
    @PutMapping("/{commentId}")
    public ResponseEntity<CommentDTO> updateComment(
            @PathVariable Long postId,
            @PathVariable Long commentId,
            @Valid @RequestBody CommentUpdateRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Long userId = Long.parseLong(userDetails.getUsername());

        try {
            CommentDTO updatedComment = commentService.updateComment(commentId, request, userId);
            return ResponseEntity.ok(updatedComment);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    /**
     * 댓글 삭제
     */
    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteComment(
            @PathVariable Long postId,
            @PathVariable Long commentId,
            @AuthenticationPrincipal UserDetails userDetails) {

        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Long userId = Long.parseLong(userDetails.getUsername());

        try {
            commentService.deleteComment(commentId, userId);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    /**
     * 댓글 좋아요/싫어요
     */
    @PostMapping("/{commentId}/reaction")
    public ResponseEntity<CommentDTO> reactToComment(
            @PathVariable Long postId,
            @PathVariable Long commentId,
            @RequestBody CommentReactionRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Long userId = Long.parseLong(userDetails.getUsername());

        CommentDTO updatedComment = commentService.reactToComment(
                commentId, request.getReactionType(), userId);

        return ResponseEntity.ok(updatedComment);
    }

    /**
     * 댓글 좋아요
     */
    @PostMapping("/{commentId}/like")
    public ResponseEntity<CommentDTO> likeComment(
            @PathVariable Long postId,
            @PathVariable Long commentId,
            @AuthenticationPrincipal UserDetails userDetails) {

        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Long userId = Long.parseLong(userDetails.getUsername());

        CommentDTO updatedComment = commentService.reactToComment(
                commentId, ReactionType.LIKE, userId);

        return ResponseEntity.ok(updatedComment);
    }

    /**
     * 댓글 싫어요
     */
    @PostMapping("/{commentId}/dislike")
    public ResponseEntity<CommentDTO> dislikeComment(
            @PathVariable Long postId,
            @PathVariable Long commentId,
            @AuthenticationPrincipal UserDetails userDetails) {

        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Long userId = Long.parseLong(userDetails.getUsername());

        CommentDTO updatedComment = commentService.reactToComment(
                commentId, ReactionType.DISLIKE, userId);

        return ResponseEntity.ok(updatedComment);
    }
}