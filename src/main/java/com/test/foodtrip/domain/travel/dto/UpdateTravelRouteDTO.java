package com.test.foodtrip.domain.travel.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateTravelRouteDTO {
    private Long userId;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    private String title;
    private String description;
    private double totalDistance;
    private Integer totalTime;
    private Integer views;
    private List<String> tags;
    private List<RoutePlaceDTO> places;
}
