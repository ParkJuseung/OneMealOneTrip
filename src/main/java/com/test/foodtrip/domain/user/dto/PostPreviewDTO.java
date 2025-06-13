package com.test.foodtrip.domain.user.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.List;

@Data
@AllArgsConstructor
public class PostPreviewDTO {
    private Long id;
    private String title;
    private String thumbnailUrl;
    private Integer viewCount;
    private Integer likeCount;
    private Integer commentCount;
    private List<String> imageUrls;  // 원한다면 미리보기용 다중 URL
}
