package com.test.foodtrip.domain.travel.dto;

import com.test.foodtrip.domain.travel.entity.RoutePlace;
import com.test.foodtrip.domain.travel.entity.TravelRoute;
import com.test.foodtrip.domain.travel.service.GooglePlaceService;
import com.test.foodtrip.domain.user.entity.User;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter // 필요한 필드만 setter 허용도 가능
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TravelRouteDTO {

    private Long routeId;
    private String title;
    private String description;
    private Double totalDistance;
    private Integer totalTime;
    private Integer views;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    @Builder.Default
    private List<RoutePlaceDTO> places = new ArrayList<>();
    
    @Builder.Default
    private List<TravelRouteTagDTO> tags = new ArrayList<>();
    
    private Long userId;
    private String userName;
    
    // 목록 화면에서 사용할 장소 개수 반환
    public int getPlaceCount() {
        return places.size();
    }

    /**
     * TravelRoute 엔티티 생성용 (Builder 패턴 기반)
     */
    public TravelRoute toNewEntity(User user) {
        return TravelRoute.builder()
                .title(this.title)
                .description(this.description)
                .totalDistance(this.totalDistance)
                .totalTime(this.totalTime)
                .user(user)
                .build();
    }

    public static TravelRouteDTO fromEntityWithPhotos(TravelRoute entity, List<String> photoUrls) {
        if (entity == null) return null;

        return TravelRouteDTO.builder()
                .routeId(entity.getRouteId())
                .userId(entity.getUser() != null ? entity.getUser().getId() : null)
                .userName(entity.getUser() != null ? entity.getUser().getNickname() : null)
                .title(entity.getTitle())
                .description(entity.getDescription())
                .totalDistance(entity.getTotalDistance())
                .totalTime(entity.getTotalTime())
                .views(entity.getViews())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .places(entity.getPlaces() != null ?
                        entity.getPlaces().stream()
                                .sorted(Comparator.comparing(RoutePlace::getVisitOrder))
                                .map(place -> RoutePlaceDTO.fromEntity(place, photoUrls))
                                .collect(Collectors.toList()) :
                        new ArrayList<>())
                .tags(entity.getTags() != null ?
                        entity.getTags().stream()
                                .map(TravelRouteTagDTO::fromEntity)
                                .collect(Collectors.toList()) :
                        new ArrayList<>())
                .build();
    }

    public static TravelRouteDTO fromEntity(TravelRoute entity, List<RoutePlaceDTO> places) {
        return TravelRouteDTO.builder()
                .routeId(entity.getRouteId())
                .title(entity.getTitle())
                .description(entity.getDescription())
                .userId(entity.getUser().getId())
                .totalDistance(entity.getTotalDistance())
                .totalTime(entity.getTotalTime())
                .places(places)
                .tags(entity.getTags().stream()
                        .map(TravelRouteTagDTO::fromEntity)
                        .toList())
                .build();
    }

} 