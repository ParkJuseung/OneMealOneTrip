package com.test.foodtrip.domain.user.dto;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import org.springframework.web.multipart.MultipartFile;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "신규 회원가입 요청 DTO")
@Data
public class SignupDTO {

    @NotBlank
    @Schema(description = "소셜 타입 (GOOGLE, NAVER, KAKAO)", example = "GOOGLE")
    private String socialType;

    @NotBlank
    @Schema(description = "소셜 이메일", example = "test@naver.com")
    private String socialEmail;

    @NotBlank
    @Schema(description = "이름", example = "김철수")
    private String name;


    @Schema(description = "닉네임", example = "chulsu")
    @NotBlank(message = "닉네임은 필수 항목입니다.")
    @Pattern(regexp = "^[가-힣]{2,10}$", message = "닉네임은 한글 2~10자만 가능합니다.")
    private String nickname;

    @Schema(description = "성별 (male/female/other)", example = "other")
    @NotBlank
    private String gender = "other";

    @Schema(description = "생년월일 (YYYY-MM-DD)", example = "1992-08-15")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate birthDate;

    @Schema(description = "전화번호", example = "010-1111-2222")
    @Pattern(regexp = "^$|^\\d{3}-\\d{3,4}-\\d{4}$", message = "올바른 전화번호 형식으로 입력해주세요. (예: 010-1234-5678)")
    private String phone;

    @Schema(description = "주소", example = "대전광역시 중구")
    private String address;

    @Schema(description = "인사말", example = "안녕하세요!")
    @Size(max = 100, message = "인사말은 100자 이하로 입력해주세요.")
    private String greeting;

    @Schema(description = "프로필 이미지 URL", example = "signup123.png")
    private MultipartFile profileImageFile;

    // 저장된 파일명(혹은 URL)을 담을 필드
    private String profileImage;
}
