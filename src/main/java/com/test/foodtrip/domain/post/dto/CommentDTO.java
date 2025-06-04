// CommentDTO.java
package com.test.foodtrip.domain.post.dto;

import com.test.foodtrip.domain.post.entity.enums.DeleteStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentDTO {
    private Long id;
    private Long postId;
    private Long userId;
    private String userNickname;
    private String userProfileImage;
    private String content;
    private Long parentId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private DeleteStatus isDeleted;

    // 반응 관련
    private Long likeCount;
    private Long dislikeCount;
    private Boolean isLikedByCurrentUser;
    private Boolean isDislikedByCurrentUser;

    // 대댓글 정보
    private List<CommentDTO> replies;
    private Boolean hasReplies;

    // 상태 정보
    private Boolean isPopular;  // 좋아요 5개 이상
    private Boolean isBlinded;  // 싫어요 10개 이상

    // 화면 표시용 필드 추가
    private String displayContent;

    // 화면 표시용 메서드 - JSON 직렬화를 위해 get으로 시작
    public String getDisplayContent() {
        if (displayContent != null) {
            return displayContent;
        }

        if (isDeleted == DeleteStatus.Y) {
            return "삭제된 댓글입니다";
        } else if (isBlinded != null && isBlinded) {
            return "블라인드 처리된 댓글입니다";
        } else {
            return content;
        }
    }
}