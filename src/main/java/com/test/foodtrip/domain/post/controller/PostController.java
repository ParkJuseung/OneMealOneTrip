package com.test.foodtrip.domain.post.controller;

import com.test.foodtrip.domain.post.dto.CommentDTO;
import com.test.foodtrip.domain.post.entity.Post;
import com.test.foodtrip.domain.post.service.CommentService;
import com.test.foodtrip.domain.post.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;
    private final CommentService commentService;

    @GetMapping("/")
    public String index() {
        return "layout/main";
    }

    @GetMapping("/post")
    public String create() {
        return "post/create-post";
    }

    /**
     * 게시글 상세 페이지
     */
    @GetMapping("/posts/{postId}")
    public String getPostDetail(
            @PathVariable Long postId,
            @RequestParam(defaultValue = "0") int commentPage,
            @AuthenticationPrincipal UserDetails userDetails,
            Model model) {

        // 현재 사용자 ID 가져오기
        Long currentUserId = null;
        if (userDetails != null) {
            currentUserId = Long.parseLong(userDetails.getUsername());
        }

        // 게시글 정보 가져오기
        Post post = postService.getPostDetail(postId);
        model.addAttribute("post", post);

        // 댓글 정보 가져오기 (페이징)
        Page<CommentDTO> commentsPage = commentService.getCommentsByPost(postId, commentPage, 20, currentUserId);
        model.addAttribute("comments", commentsPage.getContent());
        model.addAttribute("commentPage", commentsPage);

        // 로그인 여부
        model.addAttribute("isLoggedIn", userDetails != null);

        // 현재 사용자 ID
        model.addAttribute("currentUserId", currentUserId);

        return "post/detail";
    }
}
