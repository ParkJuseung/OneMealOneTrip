package com.test.foodtrip.domain.user.service;

import com.test.foodtrip.domain.user.entity.User;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface FollowService {
    boolean isFollowing(Long followerId, Long followeeId);
    void follow(Long followerId, Long followeeId);
    void unfollow(Long followerId, Long followeeId);

    // ★ 팔로워 수 집계 메서드 추가
    int countFollowers(Long userId);


    /** 팔로워(User) 리스트 */
    List<User> getFollowers(Long userId);

    /** 팔로잉(User) 리스트 */
    List<User> getFollowings(Long userId);
}
