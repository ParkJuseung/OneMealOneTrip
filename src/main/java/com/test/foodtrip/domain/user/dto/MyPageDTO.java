// 파일 경로: src/main/java/com/test/foodtrip/domain/user/dto/MyPageDTO.java

package com.test.foodtrip.domain.user.dto;

import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

//import com.test.foodtrip.domain.post.dto.PostDTO;
//import com.test.foodtrip.domain.post.dto.CommentDTO;
import com.test.foodtrip.domain.user.dto.UsersInfoDTO;
//import com.test.foodtrip.domain.user.dto.TravelBucketDTO;

/**
 * 마이페이지에 보여줄 회원 정보 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MyPageDTO {

    /** PK */
    private Long id;

    /** 소셜 로그인 타입 (예: KAKAO, NAVER) */
    private String socialType;

    /** 소셜 로그인 이메일 */
    private String socialEmail;

    /** 실제 이름 */
    private String name;

    /** 화면에 표시할 닉네임 */
    private String nickname;

    /** 성별 */
    private String gender;

    /** 생년월일 */
    private LocalDate birthDate;

    /** 연락처 */
    private String phone;

    /** 주소 */
    private String address;

    /** 한 줄 소개(인사말) */
    private String greeting;

    /** 프로필 이미지 경로 */
    private String profileImage;

    /** 역할 (기본값 "USER") */
    private String role;

    /** 활성 여부 (기본값 "Y") */
    private String active;

    /** 가입일시 */
    private LocalDateTime createdAt;

    /** 최근 수정일시 */
    private LocalDateTime updatedAt;

    /** 사용자가 작성한 게시글 목록 */
    //private List<PostDTO> posts;

    /** 사용자가 작성한 댓글 목록 */
    //private List<CommentDTO> comments;

    /** 추가 사용자 정보(예: 상세 프로필) */
    private UsersInfoDTO userInfo;

    /** 사용자의 여행 버킷리스트 */
    //private List<TravelBucketDTO> travelBuckets;

    /** 사용자가 팔로우하는 사람 수 */
    private int followingCount;

    /** 사용자를 팔로우하는 사람 수 */
    private int followersCount;
}
