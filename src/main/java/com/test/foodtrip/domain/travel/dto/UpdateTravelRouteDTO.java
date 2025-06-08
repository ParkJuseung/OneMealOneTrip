package com.test.foodtrip.domain.travel.dto;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "여행 코스 수정 요청 DTO")
public class UpdateTravelRouteDTO {

    @Schema(hidden = true)
    private Long userId;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    @Schema(description = "수정할 여행 코스 제목", example = "수정된 제주도 여행")
    private String title;

    @Schema(description = "수정할 설명", example = "맛집 중심으로 리뉴얼된 3박 4일 여행")
    private String description;

    @Schema(description = "총 거리 (단위: 미터)", example = "16500.0")
    private double totalDistance;

    @Schema(description = "총 예상 시간 (단위: 초)", example = "8400")
    private Integer totalTime;

    @Schema(description = "초기 조회수 (일반적으로는 1)", example = "1")
    private Integer views;

    @Schema(
            description = "수정할 태그 목록",
            example = "[\"자연\", \"맛집\"]"
    )
    private List<String> tags;

    @ArraySchema(schema = @Schema(implementation = RoutePlaceDTO.class))
    @Schema(description = "수정할 장소 목록")
    private List<RoutePlaceDTO> places;
}
