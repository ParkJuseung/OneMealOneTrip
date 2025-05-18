package com.test.foodtrip.domain.user.controller;

import com.test.foodtrip.domain.user.dto.SignupDTO;
import com.test.foodtrip.domain.user.entity.User;
import com.test.foodtrip.domain.user.repository.UserRepository;
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

import java.util.Map;

@Controller
@RequiredArgsConstructor
@RequestMapping("/signup")
public class SignupController {

    private final UserRepository userRepository;

    @GetMapping
    public String showSignupForm(HttpSession session, Model model) {
        String sid = session != null ? session.getId() : "세션없음";
        System.out.println(">>> [Controller] sessionId = " + sid);

        @SuppressWarnings("unchecked")
        Map<String, String> attrs =
                (Map<String, String>) session.getAttribute("oauth2_attrs");

        System.out.println(">>> [Controller] oauth2_attrs = " + attrs);

        if (attrs == null) {
            return "redirect:/main";
        }

        SignupDTO signupDTO = new SignupDTO();
        signupDTO.setSocialType( attrs.get("provider") );
        signupDTO.setSocialEmail(attrs.get("social_email"));
        signupDTO.setName(       attrs.get("name") );

        model.addAttribute("signupDTO", signupDTO);
        model.addAttribute("showSignup",  true);
        return "user/login";  // login.html 안의 signup 섹션 활성화
    }

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


        if (signupDTO.getPhone() != null && signupDTO.getPhone().isBlank()) {
            signupDTO.setPhone(null);
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
                .build();

        User saved = userRepository.save(user);
        System.out.println(">>> saved user id=" + saved.getId());

        session.removeAttribute("oauth2_attrs");
        session.setAttribute("user_id", user.getId());


        return "redirect:/main";
    }
}
