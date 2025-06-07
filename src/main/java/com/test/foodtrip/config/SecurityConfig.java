package com.test.foodtrip.config;

import com.test.foodtrip.domain.user.service.SocialAuthenticationSuccessHandler;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.test.foodtrip.domain.user.service.CustomOAuth2UserService;
@Configuration
public class SecurityConfig {

    /**
     * 뷰에서 전달된 prompt/auth_type 파라미터를
     * OAuth2 인증 요청 URL에 그대로 포함시키도록 해 줍니다.
     */
    @Bean
    public OAuth2AuthorizationRequestResolver authorizationRequestResolver(
            ClientRegistrationRepository registrations) {

        DefaultOAuth2AuthorizationRequestResolver resolver =
                new DefaultOAuth2AuthorizationRequestResolver(
                        registrations,
                        "/oauth2/authorization"
                );

        resolver.setAuthorizationRequestCustomizer(customizer ->
                customizer.attributes(attrs -> {
                    ServletRequestAttributes sra =
                            (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
                    if (sra != null) {
                        HttpServletRequest request = sra.getRequest();

                        // Google/Kakao 계정 선택용 prompt
                        String prompt = request.getParameter("prompt");
                        if (prompt != null) {
                            attrs.put("prompt", prompt);
                        }

                        // Naver 재인증용 auth_type
                        String authType = request.getParameter("auth_type");
                        if (authType != null) {
                            attrs.put("auth_type", authType);
                        }
                    }
                })
        );

        return resolver;
    }

    /**
     * SecurityFilterChain 빈은 한 번만 정의해야 합니다.
     * OAuth2 로그인, 세션·쿠키 무효화, 정적 리소스 허용 등을 모두 이곳에 설정하세요.
     */
    @Bean
    public SecurityFilterChain filterChain(
            HttpSecurity http,
            OAuth2AuthorizationRequestResolver resolver,
            CustomOAuth2UserService oauth2UserService,
            SocialAuthenticationSuccessHandler successHandler
    ) throws Exception {

        http
                // CSRF 보호 비활성화 (API 클라이언트 or JS에서 직접 요청 시 필요)
                .csrf(csrf -> csrf.disable())
                // 정적 리소스와 로그인/가입/인증 엔드포인트는 모두 허용
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/css/**", "/js/**", "/images/**",
                                "/", "/index", "/login", "/signup", "/api/**",
                                "/oauth2/authorization/**", "/login/oauth2/**",
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html"
                        ).permitAll()
                        .anyRequest().authenticated()
                )
                // OAuth2 로그인 설정
                .oauth2Login(oauth -> oauth
                        .loginPage("/login")
                        .authorizationEndpoint(a -> a
                                .authorizationRequestResolver(resolver)
                        )
                        .userInfoEndpoint(u -> u
                                .userService(oauth2UserService)
                        )
                        .successHandler(successHandler)
                )
                // 로그아웃 설정
                .logout(logout -> logout
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .logoutSuccessUrl("/login")
                );

        return http.build();
    }
}
