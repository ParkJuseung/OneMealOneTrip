package com.test.foodtrip.domain.user.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.test.foodtrip.domain.user.dto.GoogleResponse;
import com.test.foodtrip.domain.user.dto.KakaoResponse;
import com.test.foodtrip.domain.user.dto.NaverResponse;
import com.test.foodtrip.domain.user.entity.User;
import com.test.foodtrip.domain.user.repository.UserRepository;
import com.test.foodtrip.global.oauth.CustomOAuth2User;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
@RequiredArgsConstructor
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final UserRepository userRepository;
    private final ObjectMapper mapper;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

        DefaultOAuth2UserService delegate = new DefaultOAuth2UserService();
        OAuth2User oauth2User = delegate.loadUser(userRequest);

        String provider = userRequest.getClientRegistration().getRegistrationId();
        Map<String, Object> attrs = oauth2User.getAttributes();

        String socialEmail;
        String name;

        if ("naver".equals(provider)) {
            NaverResponse.Response resp = mapper.convertValue(attrs.get("response"), NaverResponse.Response.class);
            socialEmail = resp.getEmail();
            name = resp.getName();
        } else if ("google".equals(provider)) {
            GoogleResponse resp = mapper.convertValue(attrs, GoogleResponse.class);
            socialEmail = resp.getEmail();
            name = resp.getName();
        } else { // kakao
            KakaoResponse resp = mapper.convertValue(attrs, KakaoResponse.class);
            socialEmail = resp.getKakaoAccount().getEmail();
            name = resp.getKakaoAccount().getProfile().getNickname();
        }

        Optional<User> userOpt = userRepository.findBySocialTypeAndSocialEmail(provider, socialEmail);

        HttpSession session = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
                .getRequest().getSession();

        User user;
        if (userOpt.isPresent()) {
            user = userOpt.get();
            session.setAttribute("user_id", user.getId());
            log.info("기존 사용자 로그인: id={}, name={}, email={}", user.getId(), user.getName(), user.getSocialEmail());
        } else {
            session.setAttribute("oauth2_attrs", Map.of(
                    "provider", provider,
                    "social_email", socialEmail,
                    "name", name
            ));
            user = User.builder()
                    .socialType(provider)
                    .socialEmail(socialEmail)
                    .name(name)
                    .nickname(null)
                    .build();
            log.info("🆕 신규 사용자, 세션에 임시 정보 저장됨: email={}, name={}", socialEmail, name);
        }

        return new CustomOAuth2User(user, attrs);
    }
}

