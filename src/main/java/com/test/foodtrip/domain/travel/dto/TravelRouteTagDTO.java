package com.test.foodtrip.domain.travel.dto;

import com.test.foodtrip.domain.travel.entity.TravelRouteTag;
import com.test.foodtrip.domain.travel.entity.TravelRouteTagging;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "여행 코스에 연결된 태그 정보 DTO")
public class TravelRouteTagDTO {

    @Schema(description = "태그 ID", example = "1")
    private Long tagId;

    @Schema(description = "태그 이름", example = "자연")
    private String tagName;

    public static TravelRouteTagDTO fromEntity(TravelRouteTagging tagging) {
        TravelRouteTag tag = tagging.getTag();
        return TravelRouteTagDTO.builder()
                .tagId(tag.getTagId())
                .tagName(tag.getTagName())
                .build();
    }

}
