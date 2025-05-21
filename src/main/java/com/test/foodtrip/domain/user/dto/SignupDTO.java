package com.test.foodtrip.domain.user.dto;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;


@Data
public class SignupDTO {

    @NotBlank
    private String socialType;

    @NotBlank
    private String socialEmail;

    @NotBlank
    private String name;



    @NotBlank(message = "닉네임은 필수 항목입니다.")
    @Pattern(regexp = "^[가-힣]{2,10}$", message = "닉네임은 한글 2~10자만 가능합니다.")
    private String nickname;

    @NotBlank
    private String gender = "other";

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate birthDate;

    @Pattern(regexp = "^$|^\\d{3}-\\d{3,4}-\\d{4}$", message = "올바른 전화번호 형식으로 입력해주세요. (예: 010-1234-5678)")
    private String phone;

    private String address;

    @Size(max = 100, message = "인사말은 100자 이하로 입력해주세요.")
    private String greeting;

    private MultipartFile profileImage;
}
