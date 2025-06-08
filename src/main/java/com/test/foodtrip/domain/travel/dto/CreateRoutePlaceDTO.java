package com.test.foodtrip.domain.travel.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "여행 코스 생성 시 포함되는 장소 정보 DTO")
public class CreateRoutePlaceDTO {

    @Schema(description = "장소 이름", example = "한라산 국립공원")
    private String placeName;

    @Schema(description = "주소", example = "제주특별자치도 서귀포시 색달동")
    private String address;

    @Schema(description = "장소에 대한 설명", example = "등산과 자연 경관을 즐길 수 있는 명소")
    private String description;

    @Schema(description = "위도", example = "33.3617")
    private double latitude;

    @Schema(description = "경도", example = "126.5292")
    private double longitude;

    @Schema(description = "여행 경로 내에서 해당 장소의 방문 순서 (1, 2, 3...)", example = "1")
    private int visitOrder;

    @Schema(description = "Google Place ID", example = "ChIJDbdkHFQayUwR7-8fITgxTmU")
    private String apiPlaceId;
}
