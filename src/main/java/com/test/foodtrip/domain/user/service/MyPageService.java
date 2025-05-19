
// src/main/java/com/test/foodtrip/domain/user/service/MyPageService.java
package com.test.foodtrip.domain.user.service;

import com.test.foodtrip.domain.user.dto.MyPageDTO;

public interface MyPageService {
    MyPageDTO getMyPage(Long userId);
}
