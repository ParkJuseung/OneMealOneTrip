package com.test.foodtrip.domain.user.repository;

import com.test.foodtrip.domain.user.entity.Follow;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FollowRepository extends JpaRepository<Follow, Long> {
    boolean existsByFollower_IdAndFollowing_Id(Long followerId, Long followeeId);
    void deleteByFollower_IdAndFollowing_Id(Long followerId, Long followeeId);

    int countByFollowing_Id(Long followeeId);

    // pageOwner를 팔로잉(followee)한 사람 목록 → 팔로워 리스트
    List<Follow> findAllByFollowing_Id(Long followeeId);

    // pageOwner가 팔로잉한 사람 목록 → 팔로잉 리스트
    List<Follow> findAllByFollower_Id(Long followerId);
}
