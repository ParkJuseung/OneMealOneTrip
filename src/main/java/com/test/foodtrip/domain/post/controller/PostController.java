package com.test.foodtrip.domain.post.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PostController {

    @GetMapping("/")
    public String index() {
        return "layout/main";
    }

    @GetMapping("/post")
    public String create() {
        return "post/create-post";
    }
}
