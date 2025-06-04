package com.test.foodtrip.domain.travel.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Entity
@Table(name = "TRAVELROUTETAGGING")
public class TravelRouteTagging {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "travel_route_tagging_seq")
    @SequenceGenerator(name = "travel_route_tagging_seq", sequenceName = "travel_route_tagging_seq", allocationSize = 1)
    @Column(name = "tagging_id")
    private Long taggingId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "route_id")
    private TravelRoute travelRoute;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tag_id")
    private TravelRouteTag tag;

    public void setRoute(TravelRoute route) {
        this.travelRoute = route;
    }

    public void setTag(TravelRouteTag tag) {
        this.tag = tag;
    }

}
