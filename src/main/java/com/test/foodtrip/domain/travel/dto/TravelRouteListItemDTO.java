package com.test.foodtrip.domain.travel.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class TravelRouteListItemDTO {

    private Long routeId;
    private String title;
    private Integer placeCount;
    private Integer views;
    private String userName;
    private String profileImage;
    private Long userId;
    private List<String> tagNames; // 단순 문자열 리스트로

    private boolean bookmarked; // ← 이거 추가

    public boolean isBookmarked() {
        return bookmarked;
    }

    public void setBookmarked(boolean bookmarked) {
        this.bookmarked = bookmarked;
    }

}
