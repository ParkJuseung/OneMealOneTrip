package com.test.foodtrip.domain.user.controller;

import com.test.foodtrip.domain.user.dto.MyPageDTO;
import com.test.foodtrip.domain.user.service.MyPageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import jakarta.servlet.http.HttpSession;

@Controller
@RequiredArgsConstructor
public class MyPageController {

    private final MyPageService myPageService;

    /**
     * 마이페이지 조회
     * 1. HttpSession에서 "user_id" 꺼내기
     * 2. 서비스에 userId 전달해서 MyPageDTO 받기
     * 3. Model에 담아서 mypage.html에 렌더링
     */
    @GetMapping("/mypage")
    public String showMyPage(HttpSession session, Model model) {
        // 세션에서 user_id 가져오기
        Long userId = (Long) session.getAttribute("user_id");
        if (userId == null) {
            // 세션에 user_id가 없으면 로그인 페이지로 리다이렉트
            return "redirect:/login";
        }

        // 서비스 호출 → MyPageDTO 생성
        MyPageDTO dto = myPageService.getMyPage(userId);

        // 뷰에 전달
        model.addAttribute("user", dto);
        return "mypage";
    }
}
