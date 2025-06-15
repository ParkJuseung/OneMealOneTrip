// 파일 경로: src/main/java/com/test/foodtrip/domain/user/dto/UsersInfoDTO.java

package com.test.foodtrip.domain.user.dto;

import com.test.foodtrip.domain.user.entity.UsersInfo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 나의 소개(UserInfo) 정보를 담는 DTO
 */

@Schema(description = "나의 소개(UserInfo) 정보를 담는 DTO")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsersInfoDTO {

    /** PK */
    @Schema(description = "고유 식별자(ID)", example = "1")
    private Long id;

    /** 소개 글 내용 */
    @Schema(description = "자기소개 내용", example = "안녕하세요! 여행과 사진을 좋아합니다.")
    private String content;

    /** 관심사 태그 (콤마로 구분된 문자열) */
    @Schema(description = "소개 태그 목록 (쉼표로 구분)", example = "여행,사진,맛집")
    private String tags;

    /** 생성 시각 */
    @Schema(description = "생성 일시", example = "2025-06-11T15:30:00")
    private LocalDateTime createdAt;

    /** 수정 시각 */
    @Schema(description = "수정 일시", example = "2025-06-12T10:00:00")
    private LocalDateTime updatedAt;

    /** 엔티티 → DTO 변환 메서드 */
    public static UsersInfoDTO fromEntity(UsersInfo entity) {
        if (entity == null) {
            return null;
        }
        return UsersInfoDTO.builder()
                .id(entity.getId())
                .content(entity.getContent())
                .tags(entity.getTags())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
