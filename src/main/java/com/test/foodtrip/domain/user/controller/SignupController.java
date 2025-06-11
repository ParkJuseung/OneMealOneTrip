package com.test.foodtrip.domain.user.controller;

import com.test.foodtrip.domain.user.dto.SignupDTO;
import com.test.foodtrip.domain.user.entity.User;
import com.test.foodtrip.domain.user.repository.UserRepository;
import com.test.foodtrip.domain.user.service.FileStorageService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;



@Controller
@RequiredArgsConstructor
@RequestMapping("/signup")
@Tag(name = "Signup", description = "회원가입 API")
public class SignupController {

    private final UserRepository userRepository;

    private final FileStorageService storageService;



    @Operation(
            summary = "회원가입 페이지 조회",
            description = "신규 회원가입 폼을 보여줍니다."
    )
    @GetMapping
    public String showSignupForm(HttpSession session, Model model) {
        String sid = session != null ? session.getId() : "세션없음";
        System.out.println(">>> [Controller] sessionId = " + sid);

        @SuppressWarnings("unchecked")
        Map<String, String> attrs =
                (Map<String, String>) session.getAttribute("oauth2_attrs");

        System.out.println(">>> [Controller] oauth2_attrs = " + attrs);

        if (attrs == null) {
            return "redirect:/index";
        }

        SignupDTO signupDTO = new SignupDTO();
        signupDTO.setSocialType( attrs.get("provider") );
        signupDTO.setSocialEmail(attrs.get("social_email"));
        signupDTO.setName(       attrs.get("name") );

        model.addAttribute("signupDTO", signupDTO);
        model.addAttribute("showSignup",  true);
        return "user/login";  // login.html 안의 signup 섹션 활성화
    }


    @Operation(
            summary = "회원가입 처리",
            description = "회원 정보를 저장하고 포스트 목록 페이지로 리다이렉트합니다."
    )
    @PostMapping
    public String processSignup(
            @Valid @ModelAttribute("signupDTO") SignupDTO signupDTO,
            BindingResult bindingResult,
            HttpSession session,
            Model model
    ) {
        @SuppressWarnings("unchecked")
        Map<String, String> attrs = (Map<String, String>) session.getAttribute("oauth2_attrs");
        if (attrs == null) {
            return "redirect:/login";
        }
        if (bindingResult.hasErrors()) {
            System.out.println("=== Validation Errors ===");
            bindingResult.getAllErrors().forEach(err -> System.out.println(err));

            model.addAttribute("showSignup", true);
            model.addAttribute("provider", attrs.get("provider"));
            model.addAttribute("socialEmail", attrs.get("social_email"));
            model.addAttribute("name", attrs.get("name"));
            return "user/login";
        }

        // 닉네임 중복 검사
        if (userRepository.existsByNickname(signupDTO.getNickname())) {
            bindingResult.rejectValue(
                    "nickname",
                    "duplicate",
                    "이미 사용 중인 닉네임입니다."
            );
            model.addAttribute("showSignup", true);
            model.addAttribute("provider", attrs.get("provider"));
            model.addAttribute("socialEmail", attrs.get("social_email"));
            model.addAttribute("name", attrs.get("name"));
            return "user/login";
        }

        if (signupDTO.getPhone() != null && signupDTO.getPhone().isBlank()) {
            signupDTO.setPhone(null);
        }

        // 1) 프로필 이미지가 있으면 저장하고, 반환된 파일명을 DTO에 세팅
        MultipartFile file = signupDTO.getProfileImageFile();
        if (file != null && !file.isEmpty()) {
            String filename = storageService.storeFile(file);
            signupDTO.setProfileImage(filename);
        }

        User user = User.builder()
                .socialType(signupDTO.getSocialType())
                .socialEmail(signupDTO.getSocialEmail())
                .name(signupDTO.getName())
                .nickname(signupDTO.getNickname())
                .gender(signupDTO.getGender())
                .birthDate(signupDTO.getBirthDate())
                .phone(signupDTO.getPhone())
                .address(signupDTO.getAddress())
                .greeting(signupDTO.getGreeting())
                .profileImage(signupDTO.getProfileImage())
                .build();

        User saved = userRepository.save(user);
        System.out.println(">>> saved user id=" + saved.getId());

        session.removeAttribute("oauth2_attrs");
        session.setAttribute("user_id", user.getId());


        return "redirect:/post";
    }
}
