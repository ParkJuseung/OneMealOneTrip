package com.test.foodtrip.domain.travel.dto;

import com.test.foodtrip.domain.travel.entity.RoutePlace;
import com.test.foodtrip.domain.travel.service.GooglePlaceService;
import lombok.*;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoutePlaceDTO {

    private String apiPlaceId;
    private String placeName;
    private String address;
    private String description;
    private Double latitude;
    private Double longitude;
    private Integer visitOrder;

    private List<String> photos; // 만약 존재한다면

    public static RoutePlaceDTO fromEntity(RoutePlace entity, List<String> photoUrls) {

        //List<String> photoUrls = placeService.getPhotoUrlsByPlaceId(entity.getApiPlaceId());

        return RoutePlaceDTO.builder()
                .apiPlaceId(entity.getApiPlaceId())
                .placeName(entity.getPlaceName())
                .address(entity.getAddress())
                .description(entity.getDescription())
                .latitude(entity.getLatitude())
                .longitude(entity.getLongitude())
                .visitOrder(entity.getVisitOrder())
                .photos(photoUrls) // 지금은 비워두기
                .build();
    }

}
