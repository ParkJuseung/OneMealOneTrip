package com.test.foodtrip.domain.post.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Getter
@Setter
@ToString
public class PageResultDTO<DTO, EN> {
    // DTO 리스트
    private List<DTO> dtoList;

    // 전체 페이지 수
    private int totalPage;

    // 현재 페이지 번호 (1부터 시작)
    private int page;

    // 페이지당 항목 수
    private int size;

    // 페이지네이션에서 보여질 시작/끝 페이지 번호
    private int start, end;

    // 이전/다음 버튼 표시 여부
    private boolean prev, next;

    // 페이지 번호 리스트 (1, 2, 3, ...)
    private List<Integer> pageList;

    public PageResultDTO(Page<EN> result, Function<EN, DTO> fn) {
        // Entity를 DTO로 변환
        dtoList = result.stream().map(fn).collect(Collectors.toList());
        totalPage = result.getTotalPages();
        makePageList(result.getPageable());
    }

    private void makePageList(Pageable pageable){
        // 현재 페이지 번호 (0부터 시작하는 것을 1부터 시작하도록 변환)
        this.page = pageable.getPageNumber() + 1;
        this.size = pageable.getPageSize();

        // 10개씩 페이지 그룹을 만들어서 표시
        // 예: 1-10, 11-20, 21-30...
        int tempEnd = (int)(Math.ceil(page/10.0)) * 10;

        start = tempEnd - 9;  // 시작 페이지

        // 이전 그룹이 있는지 체크
        prev = start > 1;

        // 끝 페이지는 전체 페이지 수와 계산된 끝 페이지 중 작은 값
        end = totalPage > tempEnd ? tempEnd : totalPage;

        // 다음 그룹이 있는지 체크
        next = totalPage > tempEnd;

        // 페이지 번호 리스트 생성 (start부터 end까지)
        pageList = IntStream.rangeClosed(start, end).boxed().collect(Collectors.toList());
    }
}