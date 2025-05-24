package com.test.foodtrip.domain.travel.repository;

import com.test.foodtrip.domain.travel.entity.TravelRoute;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TravelRouteRepository extends JpaRepository<TravelRoute, Long> {
    @Query("""
        SELECT r FROM TravelRoute r
        LEFT JOIN FETCH r.places
        WHERE r.routeId = :routeId
    """)
    Optional<TravelRoute> findByIdWithPlaces(@Param("routeId") Long routeId);

    @Query("SELECT DISTINCT r FROM TravelRoute r " +
            "LEFT JOIN r.tags t " +
            "LEFT JOIN r.user u " +
            "WHERE LOWER(r.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "   OR LOWER(t.tag.tagName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "   OR LOWER(u.nickname) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<TravelRoute> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);
}
