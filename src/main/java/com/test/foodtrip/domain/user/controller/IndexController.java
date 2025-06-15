package com.test.foodtrip.domain.user.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;


@Controller
@Tag(name = "Index", description = "메인 페이지 API")
public class IndexController {

    /**
     * 애플리케이션 진입점 → index.html 로 포워드
     */
    @Operation(
            summary = "메인 페이지 조회",
            description = "홈(인덱스) 페이지를 반환합니다."
    )
    @GetMapping({"/index"})
    public String index() {



        return "index";  // templates/index.html
    }

}
