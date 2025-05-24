package com.test.foodtrip.domain.travel.repository;

import com.test.foodtrip.domain.travel.entity.RoutePlace;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoutePlaceRepository extends JpaRepository<RoutePlace, Long> {
    Optional<RoutePlace> findTopByTravelRoute_RouteIdOrderByVisitOrderAsc(Long routeId);
}
