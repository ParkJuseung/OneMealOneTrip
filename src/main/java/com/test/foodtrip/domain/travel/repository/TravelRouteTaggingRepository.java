package com.test.foodtrip.domain.travel.repository;

import com.test.foodtrip.domain.travel.entity.TravelRouteTagging;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TravelRouteTaggingRepository extends JpaRepository<TravelRouteTagging, Long> {
}
