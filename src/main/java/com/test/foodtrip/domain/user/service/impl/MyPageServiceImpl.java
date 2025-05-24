// src/main/java/com/test/foodtrip/domain/user/service/impl/MyPageServiceImpl.java
package com.test.foodtrip.domain.user.service.impl;

import com.test.foodtrip.domain.user.dto.MyPageDTO;
import com.test.foodtrip.domain.user.dto.UsersInfoDTO;
import com.test.foodtrip.domain.user.entity.User;
import com.test.foodtrip.domain.user.entity.UsersInfo;
import com.test.foodtrip.domain.user.repository.UserRepository;
import com.test.foodtrip.domain.user.service.MyPageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class MyPageServiceImpl implements MyPageService {
    private final UserRepository userRepository;

    @Override
    public MyPageDTO getMyPage(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 회원입니다."));

        // DTO로 매핑 (필드 매핑 부분을 반드시 채워주세요)
        return MyPageDTO.builder()
                .id(user.getId())
                .socialType(user.getSocialType())
                .socialEmail(user.getSocialEmail())
                .name(user.getName())
                .nickname(user.getNickname())
                .gender(user.getGender())
                .birthDate(user.getBirthDate())
                .phone(user.getPhone())
                .greeting(user.getGreeting())
                .profileImage(user.getProfileImage())
                // 추가로 필요한 필드들이 있다면 아래에 계속 매핑
                // .role(user.getRole())
                // .active(user.getActive())
                // .createdAt(user.getCreatedAt())
                // .updatedAt(user.getUpdatedAt())
                .followingCount(user.getFollowing().size())
                .followersCount(user.getFollowers().size())
                .userInfo(UsersInfoDTO.fromEntity(user.getUserInfo()))
                // 컬렉션 관계도 필요하면 변환해서 넣어줍니다.
                // .posts(user.getPosts().stream().map(PostDTO::from).toList())
                // .comments(user.getComments().stream().map(CommentDTO::from).toList())
                .build();
    }

    @Override
    public void updateProfile(Long userId, MyPageDTO formDto) {

        System.out.println("▶▶ updateProfile 시작: userId=" + userId
                + ", nickname=" + formDto.getNickname()
                + ", greeting=" + formDto.getGreeting());

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 회원입니다."));

        // nickname 은 무조건 덮어쓰기

        user.setNickname(formDto.getNickname());

        if (formDto.getGender() != null) {
            user.setGender(formDto.getGender());
        }

        if (formDto.getBirthDate() != null) {
            user.setBirthDate(formDto.getBirthDate());
        }

        if (formDto.getPhone() != null) {
            user.setPhone(formDto.getPhone());
        }

        if (formDto.getGreeting() != null) {
            user.setGreeting(formDto.getGreeting());
        }

        if (formDto.getProfileImage() != null) {
            user.setProfileImage(formDto.getProfileImage());
        }



        userRepository.save(user);

        // 바로 다시 조회해서 변경이 반영됐는지 한 번 더 로그
        User updated = userRepository.findById(userId).orElseThrow();
        System.out.println("▶▶ 업데이트 후 nickname=" + updated.getNickname()
                + ", greeting=" + updated.getGreeting() + updated.getPhone());
    }



    @Override
    public UsersInfoDTO createIntro(Long userId, UsersInfoDTO introDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 회원입니다."));


        // builder() 사용해서 UsersInfo 생성
        UsersInfo info = UsersInfo.builder()
                .user(user)
                .content(introDto.getContent())
                .tags(introDto.getTags())
                .build();

        user.setUserInfo(info);
        userRepository.saveAndFlush(user);

        return UsersInfoDTO.fromEntity(info);
    }



    @Override
    public UsersInfoDTO updateIntro(Long userId, UsersInfoDTO introDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 회원입니다."));
        UsersInfo info = user.getUserInfo();
        if (info == null) {
            throw new RuntimeException("먼저 소개글을 작성해주세요.");
        }
        info.setContent(introDto.getContent());
        info.setTags(introDto.getTags());
        userRepository.saveAndFlush(user);
        return UsersInfoDTO.fromEntity(info);
    }

    @Override
    public void deleteIntro(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 회원입니다."));
        UsersInfo info = user.getUserInfo();
        if (info != null) {
            info.setContent(null);
            info.setTags(null);
            userRepository.saveAndFlush(user);
        }
    }





}
