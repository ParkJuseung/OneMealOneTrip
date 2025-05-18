package com.test.foodtrip.domain.user.service;

import com.test.foodtrip.domain.user.dto.GoogleResponse;
import com.test.foodtrip.domain.user.dto.KakaoResponse;
import com.test.foodtrip.domain.user.dto.NaverResponse;

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

@Service
public class CustomOAuth2UserService
        implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

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

        HttpSession session = ((ServletRequestAttributes)
                RequestContextHolder.getRequestAttributes())
                .getRequest().getSession();

        session.setAttribute("oauth2_attrs", Map.of(
                "provider",     provider,
                "social_email", socialEmail,
                "name",         name
        ));

        return oauth2User;
    }
}

