package com.test.foodtrip.domain.post.dto;

import lombok.*;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@Builder
@AllArgsConstructor
@Getter
@Setter
@ToString
public class PageRequestDTO {
    private int page;
    private int size;
    private String type;  // 검색 타입 (제목, 내용 등)
    private String keyword;  // 검색 키워드

    // 기본값 설정을 위한 생성자
    public PageRequestDTO(){
        this.page = 1;
        this.size = 10;
    }

    public Pageable getPageable(Sort sort){
        // page는 1부터 시작하므로 -1을 해서 0부터 시작하도록 변환
        return PageRequest.of(page-1, size, sort);
    }
}