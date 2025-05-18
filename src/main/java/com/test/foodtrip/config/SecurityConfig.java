package com.test.foodtrip.config;

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
import com.test.foodtrip.domain.user.service.SocialAuthenticationSuccessHandler;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

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

                        String prompt = request.getParameter("prompt");
                        if (prompt != null) {
                            attrs.put("prompt", prompt);
                        }

                        String authType = request.getParameter("auth_type");
                        if (authType != null) {
                            attrs.put("auth_type", authType);
                        }
                    }
                })
        );

        return resolver;
    }

    @Bean
    public SecurityFilterChain filterChain(
            HttpSecurity http,
            OAuth2AuthorizationRequestResolver resolver,
            CustomOAuth2UserService oauth2UserService,
            SocialAuthenticationSuccessHandler successHandler
    ) throws Exception {

        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/css/**", "/js/**", "/images/**",
                                "/login", "/signup",
                                "/oauth2/authorization/**", "/login/oauth2/**",
                                "/api/**"
                        ).permitAll()
                        .anyRequest().authenticated()
                )
                .oauth2Login(oauth -> oauth
                        .loginPage("/login")
                        .authorizationEndpoint(a -> a
                                .authorizationRequestResolver(resolver)
                        )
                        .userInfoEndpoint(u -> u.userService(oauth2UserService))
                        .successHandler(successHandler)
                )
                .logout(logout -> logout
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .logoutSuccessUrl("/login")
                );

        return http.build();
    }
}
