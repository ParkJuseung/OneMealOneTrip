package com.test.foodtrip.domain.post.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PostController {

    @GetMapping(value= {"/", "/home"})
    public String index() {
        return "index";
    }

    @GetMapping("/post")
    public String create() {
        return "post/create-post";
    }
}
