package com.test.foodtrip.domain.post.controller;

import com.test.foodtrip.domain.post.dto.PageRequestDTO;
import com.test.foodtrip.domain.post.dto.PageResultDTO;
import com.test.foodtrip.domain.post.dto.PostDTO;
import com.test.foodtrip.domain.post.service.PostService;
import org.springframework.data.querydsl.QPageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
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
        model.addAttribute("result", postService.getList(pageRequestDTO));
        return "post/post";
    }

    @GetMapping("/post/{id}")
    public String detail(@PathVariable("id") Long id, @ModelAttribute("requestDTO") PageRequestDTO requestDTO, Model model) {
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

        System.out.println("제목: " + dto.getTitle());
        System.out.println("내용: " + dto.getContent());


        Long pno = postService.create(dto);
        redirectAttributes.addFlashAttribute("msg", pno);
        return "redirect:/post";
    }


}
