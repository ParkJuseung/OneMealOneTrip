package com.test.foodtrip.domain.user.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class IndexController {

    /**
     * 애플리케이션 진입점 → index.html 로 포워드
     */
    @GetMapping({"/index"})
    public String index() {



        return "index";  // templates/index.html
    }

}
