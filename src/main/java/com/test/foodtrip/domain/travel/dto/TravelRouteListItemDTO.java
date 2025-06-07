package com.test.foodtrip.domain.travel.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
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

    // JPQL에서 사용할 생성자
    public TravelRouteListItemDTO(Long routeId, String title, String userName,
                                  String profileImage, Integer views, Integer placeCount) {
        this.routeId = routeId;
        this.title = title;
        this.userName = userName;
        this.profileImage = profileImage;
        this.views = views;
        this.placeCount = placeCount;
    }

    // ✨ 이 메서드가 없으면 오류 발생
    public void setBookmarked(boolean bookmarked) {
        this.bookmarked = bookmarked;
    }

    public boolean isBookmarked() {
        return this.bookmarked;
    }

    public void setTagNames(List<String> tagNames) {
        this.tagNames = tagNames;
    }
}
