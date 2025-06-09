package com.test.foodtrip.domain.travel.dto;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Schema(description = "여행 코스 생성 요청 DTO")
@Data
public class CreateTravelRouteDTO {

    @Schema(hidden = true)
    private Long userId;

    @Schema(description = "여행 코스 제목", example = "제주도 여행")
    private String title;

    @Schema(description = "여행 설명", example = "3박 4일 제주도 자연과 맛집 투어입니다.")
    private String description;

    @Schema(description = "총 거리 (단위: 미터)", example = "14500.0")
    private double totalDistance;

    @Schema(description = "총 예상 시간 (단위: 초)", example = "7200")
    private Integer totalTime;

    @Schema(description = "초기 조회수 (생략 가능)", example = "1", defaultValue = "1")
    private Integer views;

    @Schema(
            description = "사용자가 선택한 태그 목록",
            example = "[\"자연\", \"맛집\"]"
    )
    private List<String> tags;

    @ArraySchema(
            schema = @Schema(implementation = RoutePlaceDTO.class)
    )
    @Schema(description = "여행 경로에 포함된 장소 목록")
    private List<RoutePlaceDTO> places;
}
