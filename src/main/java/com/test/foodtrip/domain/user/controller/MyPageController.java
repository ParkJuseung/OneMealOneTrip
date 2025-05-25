// 파일 경로: src/main/java/com/test/foodtrip/domain/user/controller/MyPageController.java

package com.test.foodtrip.domain.user.controller;

import com.test.foodtrip.domain.user.dto.MyPageDTO;
import com.test.foodtrip.domain.user.dto.UsersInfoDTO;
import com.test.foodtrip.domain.user.service.MyPageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

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
        model.addAttribute("sessionUserId", userId);
        model.addAttribute("pageOwnerId", dto.getId());
        return "mypage";
    }

    /**
     * 다른 사람 마이페이지 조회
     */
    @GetMapping("/users/{pageOwnerId}")
    public String viewUserPage(
            @PathVariable Long pageOwnerId,
            HttpSession session,
            Model model
    ) {
        Long sessionUserId = (Long) session.getAttribute("user_id");  // null 가능
        MyPageDTO dto = myPageService.getMyPage(pageOwnerId);

        model.addAttribute("user", dto);
        model.addAttribute("sessionUserId", sessionUserId);
        model.addAttribute("pageOwnerId", pageOwnerId);
        return "mypage";
    }

    /**
     * 프로필 수정 폼 보여주기
     */
    @GetMapping("/mypage/edit")
    public String showEditForm(HttpSession session, Model model) {
        Long userId = (Long) session.getAttribute("user_id");
        if (userId == null) {
            return "redirect:/login";
        }

        MyPageDTO dto = myPageService.getMyPage(userId);
        model.addAttribute("user", dto);
        return "mypage";  // 모달이 같은 페이지에 있으면 기존 템플릿 재사용
    }



    /**
     * 프로필 수정 처리 (AJAX)
     */
    @PostMapping("/mypage/edit")
    @ResponseBody
    public ResponseEntity<Void> updateProfileAjax(
            HttpSession session,
            @ModelAttribute("user") MyPageDTO formDto
    ) {
        Long userId = (Long) session.getAttribute("user_id");
        if (userId == null) {
            // 인증이 안 된 경우 401 반환
            return ResponseEntity.status(401).build();
        }

        myPageService.updateProfile(userId, formDto);
        // 변경 완료, HTTP 200 OK 만 내려줌
        return ResponseEntity.ok().build();
    }



    /** 나의 소개 생성 */
    @PostMapping("/mypage/intro")
    @ResponseBody
    public ResponseEntity<UsersInfoDTO> createIntro(
            HttpSession session,
            @RequestBody UsersInfoDTO introDto
    ) {
        Long sessionUserId = (Long) session.getAttribute("user_id");
        if (sessionUserId == null) {
            return ResponseEntity.status(401).build();
        }
        UsersInfoDTO saved = myPageService.createIntro(sessionUserId, introDto);
        return ResponseEntity.ok(saved);
    }

    /** 나의 소개 수정 */
    @PutMapping("/mypage/intro")
    @ResponseBody
    public ResponseEntity<UsersInfoDTO> updateIntro(
            HttpSession session,
            @RequestBody UsersInfoDTO introDto
    ) {
        Long sessionUserId = (Long) session.getAttribute("user_id");
        if (sessionUserId == null) {
            return ResponseEntity.status(401).build();
        }
        UsersInfoDTO updated = myPageService.updateIntro(sessionUserId, introDto);
        return ResponseEntity.ok(updated);
    }

    /** 나의 소개 삭제 */
    @DeleteMapping("/mypage/intro")
    @ResponseBody
    public ResponseEntity<Void> deleteIntro(HttpSession session) {
        Long sessionUserId = (Long) session.getAttribute("user_id");
        if (sessionUserId == null) {
            return ResponseEntity.status(401).build();
        }
        myPageService.deleteIntro(sessionUserId);
        return ResponseEntity.ok().build();
    }





}

