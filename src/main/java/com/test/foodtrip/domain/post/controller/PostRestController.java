package com.test.foodtrip.domain.post.controller;

import com.test.foodtrip.domain.post.dto.ApiResponse;
import com.test.foodtrip.domain.post.dto.PostDTO;
import com.test.foodtrip.domain.post.service.PostService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
@Log4j2
public class PostRestController {

    private final PostService postService;

    // 로그인 상태를 확인하는 헬퍼 메서드
    private boolean isLoggedIn(HttpSession session) {
        return session != null && session.getAttribute("user_id") != null;
    }

    // 현재 로그인 사용자 ID 가져오기
    private Long getCurrentUserId(HttpSession session) {
        if (session != null) {
            return (Long) session.getAttribute("user_id");
        }
        return null;
    }

    // 게시글 수정
    @PutMapping("/{postId}")
    public ResponseEntity<ApiResponse<PostDTO>> updatePost(
            @PathVariable Long postId,
            @RequestBody PostDTO postDTO,
            HttpSession session) {
        try {
            // 로그인 체크
            if (!isLoggedIn(session)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponse.error("로그인이 필요합니다."));
            }

            // 현재 사용자 ID 설정
            Long currentUserId = getCurrentUserId(session);

            // 게시글 조회하여 작성자 확인
            PostDTO existingPost = postService.read(postId);
            if (!existingPost.getUserId().equals(currentUserId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(ApiResponse.error("게시글 수정 권한이 없습니다."));
            }

            // ID 설정 후 수정
            postDTO.setId(postId);
            postDTO.setUserId(currentUserId);

            postService.modify(postDTO);

            // 수정된 게시글 조회
            PostDTO updatedPost = postService.read(postId);

            return ResponseEntity.ok(ApiResponse.success(updatedPost));

        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("게시글 수정 중 오류: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("게시글 수정 중 오류가 발생했습니다: " + e.getMessage()));
        }
    }

    // 게시글 삭제
    @DeleteMapping("/{postId}")
    public ResponseEntity<ApiResponse<String>> deletePost(
            @PathVariable Long postId,
            HttpSession session) {
        try {
            // 로그인 체크
            if (!isLoggedIn(session)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponse.error("로그인이 필요합니다."));
            }

            // 현재 사용자 ID
            Long currentUserId = getCurrentUserId(session);

            // 게시글 조회하여 작성자 확인
            PostDTO existingPost = postService.read(postId);
            if (!existingPost.getUserId().equals(currentUserId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(ApiResponse.error("게시글 삭제 권한이 없습니다."));
            }

            postService.remove(postId);

            return ResponseEntity.ok(ApiResponse.success("게시글이 삭제되었습니다."));

        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("게시글 삭제 중 오류: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("게시글 삭제 중 오류가 발생했습니다: " + e.getMessage()));
        }
    }

    // 게시글 단건 조회 (API용)
    @GetMapping("/{postId}")
    public ResponseEntity<ApiResponse<PostDTO>> getPost(@PathVariable Long postId) {
        try {
            PostDTO post = postService.read(postId);
            return ResponseEntity.ok(ApiResponse.success(post));
        } catch (Exception e) {
            log.error("게시글 조회 중 오류: ", e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("게시글을 찾을 수 없습니다."));
        }
    }
}