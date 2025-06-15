package com.test.foodtrip.domain.user.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;


@Controller
@Tag(name = "Auth", description = "인증 API")
public class LoginController {

    @Operation(
            summary = "로그인 페이지 조회",
            description = "로그인 폼을 반환합니다."
    )
    @GetMapping("/login")
    public String loginPage(Model model) {
        model.addAttribute("showSignup", false);
        return "user/login";
    }
}
