package com.test.foodtrip.domain.travel.repository;

import com.test.foodtrip.domain.travel.entity.RouteBookmark;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RouteBookmarkRepository extends JpaRepository<RouteBookmark, Integer> {
    Optional<RouteBookmark> findByTravelRoute_RouteIdAndUser_Id(Long routeId, Long userId);
}
