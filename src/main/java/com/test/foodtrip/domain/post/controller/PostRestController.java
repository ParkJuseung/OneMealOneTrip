package com.test.foodtrip.domain.post.controller;

import com.test.foodtrip.domain.post.dto.PostDTO;
import com.test.foodtrip.domain.post.service.PostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpSession;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "게시글 API", description = "게시글 관련 REST API 컨트롤러")
@RestController
@RequestMapping("/api/posts")
@Log4j2
public class PostRestController {

    private final PostService postService;

    public PostRestController(@Qualifier("postServiceImpl") PostService postService) {
        this.postService = postService;
    }

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

    @Operation(summary = "게시글 수정", description = "특정 게시글 정보를 수정합니다. 본인이 작성한 게시글만 수정 가능합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "게시글 수정 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "로그인이 필요한 기능입니다",
                    content = @Content(schema = @Schema(implementation = com.test.foodtrip.domain.post.dto.ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "게시글 수정 권한이 없습니다",
                    content = @Content(schema = @Schema(implementation = com.test.foodtrip.domain.post.dto.ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 오류",
                    content = @Content(schema = @Schema(implementation = com.test.foodtrip.domain.post.dto.ApiResponse.class)))
    })
    @PutMapping("/{postId}")
    public ResponseEntity<com.test.foodtrip.domain.post.dto.ApiResponse<PostDTO>> updatePost(
            @Parameter(description = "수정할 게시글 ID", required = true) @PathVariable Long postId,
            @Parameter(description = "수정할 게시글 정보", required = true) @RequestBody PostDTO postDTO,
            HttpSession session) {
        try {
            // 로그인 체크
            if (!isLoggedIn(session)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(com.test.foodtrip.domain.post.dto.ApiResponse.error("로그인이 필요합니다."));
            }

            // 현재 사용자 ID 설정
            Long currentUserId = getCurrentUserId(session);

            // 게시글 조회하여 작성자 확인
            PostDTO existingPost = postService.read(postId);
            if (!existingPost.getUserId().equals(currentUserId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(com.test.foodtrip.domain.post.dto.ApiResponse.error("게시글 수정 권한이 없습니다."));
            }

            // ID 설정 후 수정
            postDTO.setId(postId);
            postDTO.setUserId(currentUserId);

            postService.modify(postDTO);

            // 수정된 게시글 조회
            PostDTO updatedPost = postService.read(postId);

            return ResponseEntity.ok(com.test.foodtrip.domain.post.dto.ApiResponse.success(updatedPost));

        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(com.test.foodtrip.domain.post.dto.ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("게시글 수정 중 오류: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(com.test.foodtrip.domain.post.dto.ApiResponse.error("게시글 수정 중 오류가 발생했습니다: " + e.getMessage()));
        }
    }

    @Operation(summary = "게시글 삭제", description = "특정 게시글을 삭제합니다. 본인이 작성한 게시글만 삭제 가능합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "게시글 삭제 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "로그인이 필요한 기능입니다",
                    content = @Content(schema = @Schema(implementation = com.test.foodtrip.domain.post.dto.ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "게시글 삭제 권한이 없습니다",
                    content = @Content(schema = @Schema(implementation = com.test.foodtrip.domain.post.dto.ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 오류",
                    content = @Content(schema = @Schema(implementation = com.test.foodtrip.domain.post.dto.ApiResponse.class)))
    })
    @DeleteMapping("/{postId}")
    public ResponseEntity<com.test.foodtrip.domain.post.dto.ApiResponse<String>> deletePost(
            @Parameter(description = "삭제할 게시글 ID", required = true) @PathVariable("postId") Long postId,
            HttpSession session) {
        try {
            // 로그인 체크
            if (!isLoggedIn(session)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(com.test.foodtrip.domain.post.dto.ApiResponse.error("로그인이 필요합니다."));
            }

            // 현재 사용자 ID
            Long currentUserId = getCurrentUserId(session);

            // 게시글 조회하여 작성자 확인
            PostDTO existingPost = postService.read(postId);
            if (!existingPost.getUserId().equals(currentUserId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(com.test.foodtrip.domain.post.dto.ApiResponse.error("게시글 삭제 권한이 없습니다."));
            }

            postService.remove(postId);

            return ResponseEntity.ok(com.test.foodtrip.domain.post.dto.ApiResponse.success("게시글이 삭제되었습니다."));

        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(com.test.foodtrip.domain.post.dto.ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("게시글 삭제 중 오류: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(com.test.foodtrip.domain.post.dto.ApiResponse.error("게시글 삭제 중 오류가 발생했습니다: " + e.getMessage()));
        }
    }

    @Operation(summary = "게시글 조회", description = "특정 게시글의 상세 정보를 조회합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "게시글 조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "게시글을 찾을 수 없습니다",
                    content = @Content(schema = @Schema(implementation = com.test.foodtrip.domain.post.dto.ApiResponse.class)))
    })
    @GetMapping("/{postId}")
    public ResponseEntity<com.test.foodtrip.domain.post.dto.ApiResponse<PostDTO>> getPost(
            @Parameter(description = "조회할 게시글 ID", required = true) @PathVariable Long postId) {
        try {
            PostDTO post = postService.read(postId);
            return ResponseEntity.ok(com.test.foodtrip.domain.post.dto.ApiResponse.success(post));
        } catch (Exception e) {
            log.error("게시글 조회 중 오류: ", e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(com.test.foodtrip.domain.post.dto.ApiResponse.error("게시글을 찾을 수 없습니다."));
        }
    }
}