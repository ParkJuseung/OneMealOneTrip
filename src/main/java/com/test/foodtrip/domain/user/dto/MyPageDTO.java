// 파일 경로: src/main/java/com/test/foodtrip/domain/user/dto/MyPageDTO.java

package com.test.foodtrip.domain.user.dto;

import com.test.foodtrip.domain.post.dto.PostDTO;
import com.test.foodtrip.domain.travel.dto.TravelRouteListItemDTO;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

//import com.test.foodtrip.domain.post.dto.PostDTO;
//import com.test.foodtrip.domain.post.dto.CommentDTO;
import org.springframework.format.annotation.DateTimeFormat;
//import com.test.foodtrip.domain.user.dto.TravelBucketDTO;
import org.springframework.web.multipart.MultipartFile;

import io.swagger.v3.oas.annotations.media.Schema;


/**
 * 마이페이지에 보여줄 회원 정보 DTO
 */
@Schema(description = "마이페이지에 노출될 종합 정보 DTO")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter @Setter
public class MyPageDTO {

    /** PK */
    @Schema(description = "사용자 고유 ID", example = "1")
    private Long id;

    /** 소셜 로그인 타입 (예: KAKAO, NAVER) */
    @Schema(description = "소셜 타입", example = "GOOGLE")
    private String socialType;

    /** 소셜 로그인 이메일 */
    @Schema(description = "이메일", example = "test@example.com")
    private String socialEmail;

    /** 실제 이름 */
    @Schema(description = "이름", example = "홍길동")
    private String name;

    /** 화면에 표시할 닉네임 */
    @Schema(description = "닉네임", example = "gildong")
    private String nickname;

    /** 성별 */
    @Schema(description = "성별", example = "male")
    private String gender;

    /** 생년월일 */
    @Schema(description = "생년월일", example = "1995-05-20")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate birthDate;

    /** 연락처 */
    @Schema(description = "전화번호", example = "010-9876-5432")
    private String phone;

    /** 주소 */
    @Schema(description = "주소", example = "부산광역시 해운대구")
    private String address;

    /** 한 줄 소개(인사말) */
    @Schema(description = "인사말", example = "여행을 사랑하는 개발자입니다.")
    private String greeting;

    /** (폼에서) 전송된 이미지 파일을 받을 프로퍼티 */
    @Schema(description = "프로필 이미지 URL", example = "profile2025.png")
    private MultipartFile profileImageFile;

    /** 저장된 파일명(또는 URL)을 받을 필드 */
    @Schema(description = "프로필 파일명", example = "profile2025.png")
    private String profileImage;

    /** 프로필 삭제 요청 플래그 */

    private boolean removeProfileImage;

    /** 역할 (기본값 "USER") */
    @Schema(description = "역할", example = "user")
    private String role;

    /** 활성 여부 (기본값 "Y") */
    private String active;

    /** 가입일시 */
    @Schema(description = "등록 일시", example = "2025-06-10T14:00:00")
    private LocalDateTime createdAt;

    /** 최근 수정일시 */
    @Schema(description = "수정 일시", example = "2025-06-11T09:30:00")
    private LocalDateTime updatedAt;

    /** 사용자가 작성한 게시글 목록 */

    /** 사용자가 작성한 게시글 목록 */
    private List<PostPreviewDTO> previewPosts;

    /** 사용자가 작성한 추천 여행 경로 목록 */
    private List<TravelPreviewDTO> previewTravels;



    /** 사용자가 작성한 댓글 목록 */
    //private List<CommentDTO> comments;

    /** 추가 사용자 정보(예: 상세 프로필) */
    @Schema(description = "사용자 기본 정보", example = "상세 프로필")
    private UsersInfoDTO userInfo;

    /** 사용자의 여행 버킷리스트 */

    //private List<TravelBucketDTO> travelBuckets;

    /** 사용자가 팔로우하는 사람 수 */
    @Schema(description = "팔로잉 수", example = "1")
    private int followingCount;

    /** 사용자를 팔로우하는 사람 수 */
    @Schema(description = "팔로워 수", example = "1")
    private int followersCount;

    /** Post/TravelRoute 통합 미리보기 리스트 */
    //private List<PreviewDTO> items;
    private List<PostPreviewDTO> items;
}
