package com.test.foodtrip.domain.travel.entity;


import lombok.*;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Entity
@Table(name = "RoutePlace")
public class RoutePlace {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "route_place_seq")
    @SequenceGenerator(name = "route_place_seq", sequenceName = "route_place_seq", allocationSize = 1)
    @Column(name = "place_id")
    private Long placeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "route_id")
    private TravelRoute travelRoute;

    @Column(name = "api_place_id", nullable = false, length = 100)
    private String apiPlaceId;

    @Column(name = "place_name", nullable = false, length = 200)
    private String placeName;

    @Column(name = "address", length = 255)
    private String address;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "latitude", nullable = false)
    private Double latitude;

    @Column(name = "longitude", nullable = false)
    private Double longitude;

    @Column(name = "visit_order", nullable = false)
    private Integer visitOrder;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    public void setTravelRoute(TravelRoute travelRoute) {
        this.travelRoute = travelRoute;
    }

    public void setRoute(TravelRoute route) {
        this.travelRoute = route;
    }

}
