// src/main/java/com/test/foodtrip/domain/user/service/impl/MyPageServiceImpl.java
package com.test.foodtrip.domain.user.service.impl;

import com.test.foodtrip.domain.user.dto.MyPageDTO;
import com.test.foodtrip.domain.user.repository.UserRepository;
import com.test.foodtrip.domain.user.service.MyPageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service  // ← 이 어노테이션이 있어야 스프링이 빈으로 인식합니다.
@RequiredArgsConstructor
public class MyPageServiceImpl implements MyPageService {
    private final UserRepository userRepository;

    @Override
    public MyPageDTO getMyPage(Long userId) {
        // TODO: UserRepository에서 사용자 조회 후 DTO로 변환
        return userRepository.findById(userId)
                .map(user -> new MyPageDTO(/*필드 매핑*/))
                .orElseThrow(() -> new RuntimeException("존재하지 않는 회원입니다."));
    }
}
