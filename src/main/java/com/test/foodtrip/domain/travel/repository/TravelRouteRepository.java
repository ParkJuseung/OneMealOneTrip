package com.test.foodtrip.domain.travel.repository;

import com.test.foodtrip.domain.travel.dto.TravelRouteListItemDTO;
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

    @Query("SELECT new com.test.foodtrip.domain.travel.dto.TravelRouteListItemDTO(" +
            "t.routeId, t.title, u.name, u.profileImage, t.views, SIZE(t.places)) " +
            "FROM TravelRoute t " +
            "JOIN t.user u " +
            "WHERE (:keyword IS NULL OR LOWER(t.title) LIKE LOWER(CONCAT('%', :keyword, '%')))" +
            " ORDER BY t.routeId DESC")
    Page<TravelRouteListItemDTO> findPagedList(@Param("keyword") String keyword, Pageable pageable);

    @Query("""
    SELECT DISTINCT r FROM TravelRoute r
    LEFT JOIN r.tags t
    LEFT JOIN r.user u
    WHERE LOWER(r.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
       OR LOWER(t.tag.tagName) LIKE LOWER(CONCAT('%', :keyword, '%'))
       OR LOWER(u.nickname) LIKE LOWER(CONCAT('%', :keyword, '%'))
""")
    Page<TravelRoute> searchByTitleTagOrUser(@Param("keyword") String keyword, Pageable pageable);
}
