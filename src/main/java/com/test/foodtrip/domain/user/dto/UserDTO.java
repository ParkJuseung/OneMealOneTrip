package com.test.foodtrip.domain.user.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import io.swagger.v3.oas.annotations.media.Schema;
@Schema(description = "로그인/회원가입 API 응답용 사용자 정보 DTO")
@Getter
@Setter
@ToString
public class UserDTO {

    @Schema(description = "사용자 고유 ID", example = "user123")
	private String user_id;

    @Schema(description = "소셜 플랫폼 구분 (GOOGLE, NAVER, KAKAO)", example = "GOOGLE")
    private String social_type;

    @Schema(description = "소셜 로그인 이메일", example = "test@example.com")
    private String social_email;


    private String email;

    @Schema(description = "실명(이름)", example = "홍길동")
    private String name;

    @Schema(description = "닉네임", example = "gildong")
    private String nickname;

    @Schema(description = "성별", example = "male")
    private String gender;

    @Schema(description = "생년월일 (YYYY-MM-DD)", example = "1990-01-01")
    private String birth_date;

    @Schema(description = "전화번호", example = "010-1234-5678")
    private String phone;

    @Schema(description = "주소", example = "서울특별시 강남구")
    private String address;

    @Schema(description = "인사말", example = "안녕하세요!")
    private String greeting;

    @Schema(description = "프로필 이미지 URL 또는 파일명", example = "profile123.jpg")
    private String profile_image;
}
