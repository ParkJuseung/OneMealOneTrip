package com.test.foodtrip.domain.travel.dto;

import com.test.foodtrip.domain.travel.entity.RoutePlace;
import com.test.foodtrip.domain.travel.service.GooglePlaceService;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "여행 경로 내 장소 응답 DTO")
public class RoutePlaceDTO {

    @Schema(description = "Google Places API의 Place ID", example = "ChIJDbdkHFQayUwR7-8fITgxTmU")
    private String apiPlaceId;

    @Schema(description = "장소 이름", example = "한라산 국립공원")
    private String placeName;

    @Schema(description = "장소 주소", example = "제주특별자치도 서귀포시 색달동")
    private String address;

    @Schema(description = "장소 설명", example = "자연과 등산을 즐길 수 있는 명소")
    private String description;

    @Schema(description = "위도", example = "33.3617")
    private Double latitude;

    @Schema(description = "경도", example = "126.5292")
    private Double longitude;

    @Schema(description = "여행 경로 내에서 해당 장소의 방문 순서 (1부터 시작)", example = "1")
    private Integer visitOrder;

    @ArraySchema(schema = @Schema(type = "string", example = "https://example.com/photo1.jpg"))
    @Schema(description = "장소에 연결된 Google 사진 URL 목록 (있을 경우)")
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
