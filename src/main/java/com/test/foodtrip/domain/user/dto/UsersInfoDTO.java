// 파일 경로: src/main/java/com/test/foodtrip/domain/user/dto/UsersInfoDTO.java

package com.test.foodtrip.domain.user.dto;

import com.test.foodtrip.domain.user.entity.UsersInfo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 나의 소개(UserInfo) 정보를 담는 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsersInfoDTO {

    /** PK */
    private Long id;

    /** 소개 글 내용 */
    private String content;

    /** 관심사 태그 (콤마로 구분된 문자열) */
    private String tags;

    /** 생성 시각 */
    private LocalDateTime createdAt;

    /** 수정 시각 */
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
