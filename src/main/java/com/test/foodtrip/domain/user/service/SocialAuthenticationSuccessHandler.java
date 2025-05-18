package com.test.foodtrip.domain.user.service;

import com.test.foodtrip.domain.user.repository.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

@Component
public class SocialAuthenticationSuccessHandler
        extends SavedRequestAwareAuthenticationSuccessHandler {

    private final UserRepository userRepository;

    public SocialAuthenticationSuccessHandler(UserRepository userRepository) {
        super();
        this.userRepository = userRepository;
        setDefaultTargetUrl("/main");
    }

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws ServletException, IOException {

        System.out.println(">>> SocialAuthenticationSuccessHandler 호출됨");

        HttpSession session = request.getSession(false);
        String sid = session != null ? session.getId() : "세션없음";
        System.out.println(">>> [Handler] sessionId = " + sid);
        System.out.println(">>> oauth2_attrs = " + session.getAttribute("oauth2_attrs"));



        @SuppressWarnings("unchecked")
        Map<String, String> oauth2Attrs =
                (Map<String, String>) session.getAttribute("oauth2_attrs");

        System.out.println(">>> oauth2_attrs in session: " + oauth2Attrs);


        if (oauth2Attrs != null) {


            System.out.println(">>> 신규 유저로 판단, /signup 으로 리다이렉트");
            getRedirectStrategy().sendRedirect(request, response, "/signup");

        } else {
            System.out.println(">>> 기존 유저로 판단, 기본 성공 핸들러 실행");

            super.onAuthenticationSuccess(request, response, authentication);



        }
    }
}
