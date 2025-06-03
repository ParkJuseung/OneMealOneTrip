package com.test.foodtrip.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    // application.yml 에 정의한 업로드 폴더 경로를 읽어옵니다.
    @Value("${file.upload-dir}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 브라우저에서 "/images/profiles/**" 로 요청이 들어오면
        // 로컬 디스크의 uploadDir 폴더를 뒤집어 매핑해 줍니다.
        registry
                .addResourceHandler("/images/profiles/**")
                .addResourceLocations("file:" + uploadDir + "/");
    }
}
