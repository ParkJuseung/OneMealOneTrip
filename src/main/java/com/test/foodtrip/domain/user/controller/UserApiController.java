package com.test.foodtrip.domain.user.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import com.test.foodtrip.domain.user.repository.UserRepository;
import java.util.Map;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
@Tag(name = "User API", description = "사용자 관련 비동기 API")
public class UserApiController {

    private final UserRepository userRepository;

    @Operation(
            summary = "닉네임 중복 확인",
            description = "주어진 닉네임의 중복 여부를 반환합니다."
    )
    @GetMapping("/check-nickname")
    public Map<String, Boolean> checkNickname(@RequestParam String nickname) {
        boolean exists = userRepository.existsByNickname(nickname);
        return Map.of("exists", exists);
    }
}
