package com.test.foodtrip.domain.travel.repository;

import com.test.foodtrip.domain.travel.entity.TravelRouteTagging;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TravelRouteTaggingRepository extends JpaRepository<TravelRouteTagging, Long> {

    @Query("SELECT tag.tagName " +
            "FROM TravelRouteTagging tagging " +
            "JOIN tagging.tag tag " +
            "WHERE tagging.travelRoute.routeId = :routeId")
    List<String> findTagNamesByRouteId(@Param("routeId") Long routeId);

}
