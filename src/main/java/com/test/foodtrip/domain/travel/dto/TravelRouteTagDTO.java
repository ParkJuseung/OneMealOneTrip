package com.test.foodtrip.domain.travel.dto;

import com.test.foodtrip.domain.travel.entity.TravelRouteTag;
import com.test.foodtrip.domain.travel.entity.TravelRouteTagging;
import jakarta.persistence.Id;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TravelRouteTagDTO {

    @Id
    private Long tagId;
    private String tagName;

    public static TravelRouteTagDTO fromEntity(TravelRouteTagging tagging) {
        TravelRouteTag tag = tagging.getTag();
        return TravelRouteTagDTO.builder()
                .tagId(tag.getTagId())
                .tagName(tag.getTagName())
                .build();
    }

}
