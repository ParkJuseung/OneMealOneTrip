package com.test.foodtrip.domain.travel.entity;


import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "TRAVEL_ROUTE_TAG")
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TravelRouteTag {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "travel_route_tag_seq")
    @SequenceGenerator(name = "travel_route_tag_seq", sequenceName = "travel_route_tag_seq", allocationSize = 1)
    @Column(name = "tag_id")
    private Long id;

    @Column(name = "tag_name", nullable = false, length = 50)
    private String tagName;

    @ManyToMany(mappedBy = "tags")
    private List<TravelRoute> travelRoutes = new ArrayList<>();
}
