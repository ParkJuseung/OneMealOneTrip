package com.test.foodtrip.domain.user.service.impl;

import com.test.foodtrip.domain.user.entity.Follow;
import com.test.foodtrip.domain.user.entity.User;
import com.test.foodtrip.domain.user.repository.FollowRepository;
import com.test.foodtrip.domain.user.repository.UserRepository;
import com.test.foodtrip.domain.user.service.FollowService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FollowServiceImpl implements FollowService {

    private final FollowRepository followRepository;
    private final UserRepository   userRepository;

    /** 방문자(followerId)가 pageOwner(followeeId)를 팔로우하고 있는지 */
    @Override
    @Transactional(readOnly = true)
    public boolean isFollowing(Long followerId, Long followeeId) {
        return followRepository.existsByFollower_IdAndFollowing_Id(followerId, followeeId);
    }

    /** 팔로우 요청 처리 */
    @Override
    @Transactional
    public void follow(Long followerId, Long followeeId) {
        if (!isFollowing(followerId, followeeId)) {
            // 실제 User 엔티티 조회
            User follower = userRepository.findById(followerId)
                    .orElseThrow(() -> new IllegalArgumentException("Follower not found: " + followerId));
            User followee = userRepository.findById(followeeId)
                    .orElseThrow(() -> new IllegalArgumentException("Followee not found: " + followeeId));

            // 엔티티 빌더로 객체 생성
            Follow f = Follow.builder()
                    .follower(follower)
                    .following(followee)
                    .build();

            followRepository.save(f);
        }
    }

    /** 언팔로우 요청 처리 */
    @Override
    @Transactional
    public void unfollow(Long followerId, Long followeeId) {
        followRepository.deleteByFollower_IdAndFollowing_Id(followerId, followeeId);
    }


    /** 팔로워 수 조회 */
    @Override
    @Transactional(readOnly = true)
    public int countFollowers(Long userId) {
        return followRepository.countByFollowing_Id(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> getFollowers(Long userId) {
        return followRepository.findAllByFollowing_Id(userId).stream()
                .map(Follow::getFollower)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> getFollowings(Long userId) {
        return followRepository.findAllByFollower_Id(userId).stream()
                .map(Follow::getFollowing)
                .collect(Collectors.toList());
    }
}
