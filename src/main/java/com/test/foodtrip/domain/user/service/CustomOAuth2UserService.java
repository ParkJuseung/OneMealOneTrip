package com.test.foodtrip.domain.user.service;

import com.test.foodtrip.domain.user.dto.GoogleResponse;
import com.test.foodtrip.domain.user.dto.KakaoResponse;
import com.test.foodtrip.domain.user.dto.NaverResponse;

import com.test.foodtrip.domain.user.dto.UserPrincipal;
import com.test.foodtrip.domain.user.entity.User;
import com.test.foodtrip.domain.user.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpSession;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Map;
import java.util.Optional;

@Service
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final UserRepository userRepository;
    private final DefaultOAuth2UserService delegate = new DefaultOAuth2UserService();
    private final ObjectMapper mapper = new ObjectMapper();

    public CustomOAuth2UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest request)
            throws OAuth2AuthenticationException {

        OAuth2User oauth2User = delegate.loadUser(request);

        String provider = request.getClientRegistration().getRegistrationId();

        Map<String,Object> attrs = oauth2User.getAttributes();

        String socialEmail;
        String name;

        if ("naver".equals(provider)) {
            NaverResponse.Response resp =
                    mapper.convertValue(attrs.get("response"), NaverResponse.Response.class);
            socialEmail = resp.getEmail();
            name        = resp.getName();

        } else if ("google".equals(provider)) {
            GoogleResponse resp = mapper.convertValue(attrs, GoogleResponse.class);
            socialEmail = resp.getEmail();
            name        = resp.getName();

        } else {
            KakaoResponse resp = mapper.convertValue(attrs, KakaoResponse.class);
            socialEmail = resp.getKakaoAccount().getEmail();
            name        = resp.getKakaoAccount().getProfile().getNickname();
        }

        Optional<User> userOpt =
                userRepository.findBySocialTypeAndSocialEmail(provider, socialEmail);

        HttpSession session = ((ServletRequestAttributes)
                RequestContextHolder.getRequestAttributes())
                .getRequest().getSession();

        if (userOpt.isEmpty()) {
            // 신규 사용자의 경우 회원가입 페이지로 이동시키기 위해 정보 저장
            session.setAttribute("oauth2_attrs", Map.of(
                    "provider",     provider,
                    "social_email", socialEmail,
                    "name",         name
            ));

            // 임시 UserPrincipal 반환 (회원가입 필요)
            return new UserPrincipal(null, name, null, "GUEST", socialEmail, attrs);
        } else {
            // 기존 유저인 경우
            User user = userOpt.get();
            session.setAttribute("user_id", user.getId());

            // UserPrincipal로 변환하여 반환
            return new UserPrincipal(
                    user.getId(),
                    user.getNickname(),
                    user.getProfileImage(),
                    user.getRole(),
                    user.getSocialEmail(),
                    attrs
            );
        }
    }
}

