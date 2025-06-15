// src/main/java/com/test/foodtrip/domain/user/service/impl/MyPageServiceImpl.java
package com.test.foodtrip.domain.user.service.impl;

import com.test.foodtrip.domain.travel.service.TravelRouteService;
import com.test.foodtrip.domain.user.dto.MyPageDTO;
import com.test.foodtrip.domain.user.dto.PostPreviewDTO;
import com.test.foodtrip.domain.user.dto.TravelPreviewDTO;
import com.test.foodtrip.domain.user.dto.UsersInfoDTO;
import com.test.foodtrip.domain.user.entity.UsersInfo;
import com.test.foodtrip.domain.user.entity.User;
import com.test.foodtrip.domain.user.repository.UserRepository;
import com.test.foodtrip.domain.user.repository.UsersInfoRepository;
import com.test.foodtrip.domain.user.service.MyPageService;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;



@Service
@RequiredArgsConstructor
@Transactional
public class MyPageServiceImpl implements MyPageService {
    private final UserRepository userRepository;
    private final UsersInfoRepository infoRepository;

    private final TravelRouteService travelRouteService;

    @Override
    public MyPageDTO getMyPage(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 회원입니다."));

        // 1) 프로필·통계 빌더
        MyPageDTO.MyPageDTOBuilder builder = MyPageDTO.builder()
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
                .followingCount(user.getFollowing().size())
                .followersCount(user.getFollowers().size())
                .userInfo(UsersInfoDTO.fromEntity(user.getUserInfo()));

        // 2) Post → PostPreviewDTO 매핑
        List<PostPreviewDTO> previewsPost = user.getPosts().stream()
                .map(p -> new PostPreviewDTO(
                        p.getId(),
                        p.getTitle(),
                        p.getImages().isEmpty()
                                ? "/images/default.png"
                                : p.getImages().get(0).getImageUrl(),   // :contentReference[oaicite:0]{index=0}
                        p.getViewCount(),
                        p.getLikes().size(),
                        p.getComments().size(),
                        p.getImages().stream()
                                .map(img -> img.getImageUrl())
                                .collect(Collectors.toList())
                ))
                .collect(Collectors.toList());

        // 3) TravelRoute → TravelPreviewDTO 매핑
        List<TravelPreviewDTO> previewsTravel = user.getTravels().stream()
                .map(r -> new TravelPreviewDTO(
                        r.getRouteId(),
                        r.getTitle(),
                        // TravelRouteService 에서 썸네일 URL을 가져옴
                        travelRouteService.getThumbnailUrlByRouteId(r.getRouteId()),
                        r.getDescription(),
                        r.getTotalTime()
                ))
                .collect(Collectors.toList());

        // 4) 빌더에 주입
        return builder
                .previewPosts(previewsPost)
                .previewTravels(previewsTravel)
                .build();
    }







    @Override
    public void updateProfile(Long userId, MyPageDTO formDto) {

        System.out.println("▶▶ updateProfile 시작: userId=" + userId
                + ", nickname=" + formDto.getNickname()
                + ", greeting=" + formDto.getGreeting());

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 회원입니다."));

        // 삭제 플래그 처리 후, 프로필 이미지 반영
        if (formDto.getProfileImage() != null) {
            // 새로 업로드된 이미지가 있으면 업데이트
            user.setProfileImage(formDto.getProfileImage());
        } else if (formDto.isRemoveProfileImage()) {
            // 삭제 요청이 있으면 null 로 초기화
            user.setProfileImage(null);
        }
// else { 아무 작업도 하지 않아서, 기존 이미지 유지 }
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

        // 1) UsersInfo 엔티티로 변환
        UsersInfo info = UsersInfo.builder()
                .user(user)
                .content(introDto.getContent())
                .tags(introDto.getTags())
                .build();

        // 2) UsersInfo 단독 저장
        info = infoRepository.save(info);

        // 3) User 에 연관관계 설정 후 저장 (cascade 가 없어도 동작)
        user.setUserInfo(info);
        userRepository.save(user);

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
        info = infoRepository.save(info);    // 변경된 엔티티만 저장
        return UsersInfoDTO.fromEntity(info);
    }

    @Override
    public void deleteIntro(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 회원입니다."));
        UsersInfo info = user.getUserInfo();
        if (info != null) {
            // 엔티티 자체를 지워버려도 되지만, 요구사항이 “내용 비우기”라면:
            infoRepository.delete(info);
            user.setUserInfo(null);
            userRepository.save(user);
        }
    }





}
