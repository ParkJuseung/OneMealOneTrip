// src/main/java/com/test/foodtrip/domain/user/repository/UsersInfoRepository.java
package com.test.foodtrip.domain.user.repository;

import com.test.foodtrip.domain.user.entity.UsersInfo;
import com.test.foodtrip.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsersInfoRepository extends JpaRepository<UsersInfo, Long> {
    Optional<UsersInfo> findByUser(User user);
    void deleteByUser(User user);
}
