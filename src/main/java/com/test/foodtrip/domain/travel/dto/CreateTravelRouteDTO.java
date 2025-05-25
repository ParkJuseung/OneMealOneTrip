package com.test.foodtrip.domain.travel.dto;

import lombok.Data;

import java.util.List;

@Data
public class CreateTravelRouteDTO {
    private Long userId;

    private String title;
    private String description;
    private double totalDistance;
    private Integer totalTime;
    private Integer views;
    private List<String> tags;
    private List<RoutePlaceDTO> places;
}
