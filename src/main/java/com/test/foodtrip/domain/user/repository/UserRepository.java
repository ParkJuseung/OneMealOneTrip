package com.test.foodtrip.domain.user.repository;

import com.test.foodtrip.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findBySocialTypeAndSocialEmail(String socialType, String socialEmail);
    boolean existsByNickname(String nickname);


}
