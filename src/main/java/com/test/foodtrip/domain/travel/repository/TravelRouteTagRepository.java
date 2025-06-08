package com.test.foodtrip.domain.travel.repository;

import com.test.foodtrip.domain.travel.entity.TravelRouteTag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TravelRouteTagRepository extends JpaRepository<TravelRouteTag, Long> {
    Optional<TravelRouteTag> findByTagName(String tagName);
}
