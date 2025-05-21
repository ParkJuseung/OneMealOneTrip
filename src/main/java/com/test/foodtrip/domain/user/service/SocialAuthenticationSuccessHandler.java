package com.test.foodtrip.domain.user.service;

import com.test.foodtrip.domain.user.repository.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
        setDefaultTargetUrl("/index");

    }

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws ServletException, IOException {

        System.out.println(">>> (1)[Handler] onAuthenticationSuccess 호출됨");
        System.out.println(">>> [Handler] committed before = " + response.isCommitted());

        HttpSession session = request.getSession(false);
        System.out.println(">>> session.user_id = " + session.getAttribute("user_id"));

        String sid = session != null ? session.getId() : "세션없음";
        System.out.println(">>> [Handler] sessionId = " + sid);
        System.out.println(">>> oauth2_attrs = " + session.getAttribute("oauth2_attrs"));



        @SuppressWarnings("unchecked")
        Map<String, String> oauth2Attrs =
                (Map<String, String>) session.getAttribute("oauth2_attrs");

        System.out.println(">>> oauth2_attrs in session: " + oauth2Attrs);


        if (oauth2Attrs != null) {

            // **인증 해제** → 신규 유저는 가입 완료 전까지 인증이 유지되면 안 됩니다
            SecurityContextHolder.clearContext();

            System.out.println(">>> 신규 유저로 판단, /signup 으로 리다이렉트");
            getRedirectStrategy().sendRedirect(request, response, "/signup");

            // (2) 리다이렉트 직후
            System.out.println("(2)>>> [Handler] committed after  = " + response.isCommitted());

            return;
        } else {
            System.out.println(">>> 기존 유저로 판단, 기본 성공 핸들러 실행");

            getRedirectStrategy().sendRedirect(request, response, "/index");


        }
    }
}
