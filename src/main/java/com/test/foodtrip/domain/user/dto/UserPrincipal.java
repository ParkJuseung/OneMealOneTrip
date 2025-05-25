package com.test.foodtrip.domain.user.dto;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

public class UserPrincipal implements OAuth2User {

    private final Long userId;
    private final String nickname;
    private final String profileImage;
    private final String role;
    private final String socialEmail;
    private final Map<String, Object> attributes;

    public UserPrincipal(Long userId, String nickname, String profileImage, String role,
                         String socialEmail, Map<String, Object> attributes) {
        this.userId = userId;
        this.nickname = nickname;
        this.profileImage = profileImage;
        this.role = role;
        this.socialEmail = socialEmail;
        this.attributes = attributes;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role));
    }

    @Override
    public String getName() {
        return socialEmail;
    }

    // Getter 메서드들
    public Long getUserId() {
        return userId;
    }

    public String getNickname() {
        return nickname;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public String getRole() {
        return role;
    }

    public String getSocialEmail() {
        return socialEmail;
    }
}