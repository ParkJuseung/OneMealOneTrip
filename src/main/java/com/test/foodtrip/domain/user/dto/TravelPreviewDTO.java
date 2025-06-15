package com.test.foodtrip.domain.user.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TravelPreviewDTO {
    private Long id;
    private String title;
    private String mapImageUrl;
    private String description;
    private Integer days;
}
