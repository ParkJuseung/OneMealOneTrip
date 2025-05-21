// src/main/java/com/test/foodtrip/domain/user/service/MyPageService.java
package com.test.foodtrip.domain.user.service;

import com.test.foodtrip.domain.user.dto.MyPageDTO;
import com.test.foodtrip.domain.user.dto.UsersInfoDTO;

public interface MyPageService {
    MyPageDTO getMyPage(Long userId);

    void updateProfile(Long userId, MyPageDTO formDto);

    /** 나의 소개 생성 */
    UsersInfoDTO createIntro(Long userId, UsersInfoDTO introDto);

    /** 나의 소개 수정 */
    UsersInfoDTO updateIntro(Long userId, UsersInfoDTO introDto);

    /** 나의 소개 삭제(내용 비우기) */
    void deleteIntro(Long userId);
}
