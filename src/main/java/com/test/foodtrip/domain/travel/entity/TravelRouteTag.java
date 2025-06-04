package com.test.foodtrip.domain.travel.entity;


import lombok.*;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Entity
@Table(name = "TRAVELROUTETAG")
public class TravelRouteTag {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "travel_route_tag_seq")
    @SequenceGenerator(name = "travel_route_tag_seq", sequenceName = "travel_route_tag_seq", allocationSize = 1)
    @Column(name = "tag_id")
    private Long tagId;

    @Column(name = "tag_name", nullable = false, length = 50)
    private String tagName;

    public TravelRouteTag(String tagName) {
        this.tagName = tagName;
    }

}
