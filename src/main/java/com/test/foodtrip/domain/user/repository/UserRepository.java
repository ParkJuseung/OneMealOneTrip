package com.test.foodtrip.domain.user.repository;

import com.test.foodtrip.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}

