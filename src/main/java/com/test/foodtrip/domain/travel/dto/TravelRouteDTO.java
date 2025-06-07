package com.test.foodtrip.domain.travel.dto;

import com.test.foodtrip.domain.travel.entity.RoutePlace;
import com.test.foodtrip.domain.travel.entity.TravelRoute;
import com.test.foodtrip.domain.travel.service.GooglePlaceService;
import com.test.foodtrip.domain.user.entity.User;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "여행 코스 상세 응답 DTO")
public class TravelRouteDTO {

    @Schema(description = "여행 코스 ID", example = "1")
    private Long routeId;

    @Schema(description = "여행 코스 제목", example = "제주도 자연 여행")
    private String title;

    @Schema(description = "코스 설명", example = "3박 4일 일정으로 제주도 유명 명소 탐방")
    private String description;

    @Schema(description = "총 거리 (단위: 미터)", example = "12500.0")
    private Double totalDistance;

    @Schema(description = "총 예상 시간 (단위: 초)", example = "7200")
    private Integer totalTime;

    @Schema(description = "조회수", example = "10")
    private Integer views;

    @Schema(description = "생성 일시", example = "2025-06-07T13:00:00")
    private LocalDateTime createdAt;

    @Schema(description = "마지막 수정 일시", example = "2025-06-07T14:00:00")
    private LocalDateTime updatedAt;

    @ArraySchema(schema = @Schema(implementation = RoutePlaceDTO.class))
    @Schema(description = "여행 코스에 포함된 장소 목록")
    @Builder.Default
    private List<RoutePlaceDTO> places = new ArrayList<>();

    @ArraySchema(schema = @Schema(implementation = TravelRouteTagDTO.class))
    @Schema(description = "여행 코스에 포함된 태그 목록")
    @Builder.Default
    private List<TravelRouteTagDTO> tags = new ArrayList<>();

    @Schema(description = "작성자 ID", example = "5")
    private Long userId;

    @Schema(description = "작성자 이름 (닉네임)", example = "김여행")
    private String userName;

    @Schema(description = "여행 코스 내 장소 수", example = "3", accessMode = Schema.AccessMode.READ_ONLY)
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