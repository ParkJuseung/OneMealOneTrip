package com.test.foodtrip.domain.post.controller;

import com.test.foodtrip.domain.post.dto.PageRequestDTO;
import com.test.foodtrip.domain.post.dto.PageResultDTO;
import com.test.foodtrip.domain.post.dto.PostDTO;
import com.test.foodtrip.domain.post.entity.Post;
import com.test.foodtrip.domain.post.service.PostService;
import jakarta.servlet.http.HttpSession;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@Log4j2
public class PostController {

    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @GetMapping("/")
    public String index() {
        return "redirect:/post";
    }

    @GetMapping("/post")
    public String list(PageRequestDTO pageRequestDTO, Model model) {
        log.info("PostController list() - pageRequestDTO: " + pageRequestDTO);

        PageResultDTO<PostDTO, Post> result = postService.getList(pageRequestDTO);
        model.addAttribute("result", result);

        return "post/post";
    }

    @GetMapping("/post/{id}")
    public String detail(@PathVariable("id") Long id,
                         @ModelAttribute("requestDTO") PageRequestDTO requestDTO,
                         Model model) {
        PostDTO dto = postService.read(id);
        model.addAttribute("dto", dto);
        return "post/detail-post";
    }

    @GetMapping("/post/create")
    public String create(HttpSession session, Model model) {
        // 로그인 체크
        if (!isLoggedIn(session)) {
            return "redirect:/login?error=login_required";
        }
        return "post/create-post";
    }

    @PostMapping("/post/create")
    public String create(@ModelAttribute PostDTO dto,
                         @RequestParam(value = "tags", required = false) List<String> tags,
                         HttpSession session,
                         RedirectAttributes redirectAttributes){
        log.info("PostController create() - dto: " + dto);
        log.info("PostController create() - tags: " + tags);

        // 로그인 체크
        if (!isLoggedIn(session)) {
            return "redirect:/login?error=login_required";
        }

        try {
            // 태그 정보를 DTO에 설정
            dto.setTags(tags);

            Long pno = postService.create(dto);
            redirectAttributes.addFlashAttribute("msg", pno);
            return "redirect:/post";
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/login";
        }
    }

    @GetMapping("/post/modify/{id}")
    public String modify(@PathVariable("id") Long id,
                         @ModelAttribute("requestDTO") PageRequestDTO requestDTO,
                         HttpSession session,
                         Model model) {
        // 로그인 체크
        if (!isLoggedIn(session)) {
            return "redirect:/login?error=login_required";
        }

        PostDTO dto = postService.read(id);
        model.addAttribute("dto", dto);
        return "post/modify-post";
    }

    @PostMapping("/post/modify")
    public String modify(PostDTO dto,
                         @RequestParam(value = "tags", required = false) List<String> tags,
                         @ModelAttribute("requestDTO") PageRequestDTO requestDTO,
                         HttpSession session,
                         RedirectAttributes redirectAttributes) {
        log.info("PostController modify() - dto: " + dto);
        log.info("PostController modify() - tags: " + tags);

        // 로그인 체크
        if (!isLoggedIn(session)) {
            return "redirect:/login?error=login_required";
        }

        try {
            // 태그 정보를 DTO에 설정
            dto.setTags(tags);

            postService.modify(dto);

            redirectAttributes.addAttribute("page", requestDTO.getPage());
            redirectAttributes.addAttribute("type", requestDTO.getType());
            redirectAttributes.addAttribute("keyword", requestDTO.getKeyword());

            return "redirect:/post/" + dto.getId();
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/post/" + dto.getId();
        }
    }

    @PostMapping("/post/remove")
    public String remove(Long id,
                         @ModelAttribute("requestDTO") PageRequestDTO requestDTO,
                         HttpSession session,
                         RedirectAttributes redirectAttributes) {
        log.info("PostController remove() - id: " + id);

        // 로그인 체크
        if (!isLoggedIn(session)) {
            return "redirect:/login?error=login_required";
        }

        try {
            postService.remove(id);

            redirectAttributes.addAttribute("page", 1);
            redirectAttributes.addAttribute("type", requestDTO.getType());
            redirectAttributes.addAttribute("keyword", requestDTO.getKeyword());

            return "redirect:/post";
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/post";
        }
    }

    // 로그인 상태를 확인하는 헬퍼 메서드
    private boolean isLoggedIn(HttpSession session) {
        return session != null && session.getAttribute("user_id") != null;
    }


}