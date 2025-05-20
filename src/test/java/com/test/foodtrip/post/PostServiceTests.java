package com.test.foodtrip.post;

import com.test.foodtrip.domain.post.dto.PostDTO;
import com.test.foodtrip.domain.post.service.PostService;
import com.test.foodtrip.domain.post.service.PostServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;

@SpringBootTest
public class PostServiceTests {
    @Autowired
    private PostService service;

    @Test
    @DisplayName("게시글 등록 테스트")
    public void testCreate(){
        PostDTO postDTO = PostDTO.builder()
                .title("등록 테스트용")
                .content("게시글 등록 테스트입니다.")
                .latitude(37.5665)
                .longitude(126.9780)
                .placeName("서울특별시")
                .createdAt(LocalDateTime.now())
                .build();

        System.out.println(service.create(postDTO));
    }
}
