package com.test.foodtrip.domain.post.controller;

import com.test.foodtrip.domain.post.dto.PageRequestDTO;
import com.test.foodtrip.domain.post.dto.PageResultDTO;
import com.test.foodtrip.domain.post.dto.PostDTO;
import com.test.foodtrip.domain.post.entity.Post;
import com.test.foodtrip.domain.post.service.PostService;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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
    public String create() {
        return "post/create-post";
    }

    @PostMapping("/post/create")
    public String create(@ModelAttribute PostDTO dto, RedirectAttributes redirectAttributes){
        log.info("PostController create() - dto: " + dto);

        Long pno = postService.create(dto);
        redirectAttributes.addFlashAttribute("msg", pno);
        return "redirect:/post";
    }

    @GetMapping("/post/modify/{id}")
    public String modify(@PathVariable("id") Long id,
                         @ModelAttribute("requestDTO") PageRequestDTO requestDTO,
                         Model model) {
        PostDTO dto = postService.read(id);
        model.addAttribute("dto", dto);
        return "post/modify-post";
    }

    @PostMapping("/post/modify")
    public String modify(PostDTO dto,
                         @ModelAttribute("requestDTO") PageRequestDTO requestDTO,
                         RedirectAttributes redirectAttributes) {
        log.info("PostController modify() - dto: " + dto);

        postService.modify(dto);

        redirectAttributes.addAttribute("page", requestDTO.getPage());
        redirectAttributes.addAttribute("type", requestDTO.getType());
        redirectAttributes.addAttribute("keyword", requestDTO.getKeyword());

        return "redirect:/post/" + dto.getId();
    }

    @PostMapping("/post/remove")
    public String remove(Long id,
                         @ModelAttribute("requestDTO") PageRequestDTO requestDTO,
                         RedirectAttributes redirectAttributes) {
        log.info("PostController remove() - id: " + id);

        postService.remove(id);

        redirectAttributes.addAttribute("page", 1);
        redirectAttributes.addAttribute("type", requestDTO.getType());
        redirectAttributes.addAttribute("keyword", requestDTO.getKeyword());

        return "redirect:/post";
    }
}