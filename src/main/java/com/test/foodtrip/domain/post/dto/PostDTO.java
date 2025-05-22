package com.test.foodtrip.domain.post.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostDTO {

    private Long id;
    private Long userId;
    private String title;
    private String content;
    private Integer viewCount;
    private Double latitude;
    private Double longitude;
    private String placeName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private List<String> imageUrls;
    private List<String> tags;
    private Integer likeCount;
    private Integer commentCount;

    public String getElapsedTime() {
        LocalDateTime now = LocalDateTime.now();
        long days = ChronoUnit.DAYS.between(this.createdAt.toLocalDate(), now.toLocalDate());

        if (days == 0) {
            return "오늘";
        } else if (days == 1) {
            return "1일 전";
        } else {
            return days + "일 전";
        }
    }

}
