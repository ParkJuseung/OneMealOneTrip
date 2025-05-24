package com.test.foodtrip.domain.travel.dto;

import lombok.Data;

@Data
public class CreateRoutePlaceDTO {
    private String placeName;
    private String address;
    private String description;
    private double latitude;
    private double longitude;
    private int visitOrder;
    private String apiPlaceId;
}
