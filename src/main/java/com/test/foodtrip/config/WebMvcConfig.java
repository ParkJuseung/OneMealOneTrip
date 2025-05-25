package com.test.foodtrip.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 기본 이미지 접근 (이미 자동 처리되지만 명시 가능)
        registry.addResourceHandler("/images/**")
                .addResourceLocations("classpath:/static/images/");

        // 업로드 이미지 접근
        // 예: /uploads/chatroom/example.png
        String uploadPath = Paths.get(System.getProperty("user.dir"), "uploads").toUri().toString();

        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(uploadPath);
    }
}
