package com.test.foodtrip.domain.travel.dto;

import com.test.foodtrip.domain.travel.entity.TravelRoute;
import lombok.*;

import java.util.List;
import java.util.stream.Collectors;

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

    public static TravelRouteListItemDTO fromEntity(TravelRoute route, boolean isBookmarked) {
        String userName = route.getUser() != null ? route.getUser().getNickname() : "탈퇴한 사용자";
        String profileImage = route.getUser() != null ? route.getUser().getProfileImage() : null;
        int placeCount = route.getPlaces() != null ? route.getPlaces().size() : 0;

        TravelRouteListItemDTO dto = new TravelRouteListItemDTO(
                route.getRouteId(),
                route.getTitle(),
                userName,
                profileImage,
                route.getViews(),
                placeCount
        );

        dto.setBookmarked(isBookmarked);

        // 태그 목록도 추가 가능 (추가로 setTagNames() 호출)
        List<String> tagNames = route.getTags().stream()
                .map(t -> t.getTag().getTagName())
                .collect(Collectors.toList());
        dto.setTagNames(tagNames);

        return dto;
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
