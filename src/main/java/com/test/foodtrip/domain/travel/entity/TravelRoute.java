package com.test.foodtrip.domain.travel.entity;


import com.test.foodtrip.domain.user.entity.User;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "TRAVEL_ROUTE")
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TravelRoute {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "travel_route_seq")
    @SequenceGenerator(name = "travel_route_seq", sequenceName = "travel_route_seq", allocationSize = 1)
    @Column(name = "route_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "description", length = 1000)
    private String description;

    @Column(name = "total_distance")
    private Double totalDistance;

    @Column(name = "total_time")
    private Integer totalTime;

    @Column(name = "is_featured", nullable = false, length = 1)
    private String isFeatured = "N";

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // 양방향 관계
    @OneToMany(mappedBy = "travelRoute", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RoutePlace> places = new ArrayList<>();

    @OneToMany(mappedBy = "travelRoute", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RouteBookmark> bookmarks = new ArrayList<>();

    @ManyToMany
    @JoinTable(
            name = "TRAVEL_ROUTE_TAGGING",
            joinColumns = @JoinColumn(name = "route_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private List<TravelRouteTag> tags = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // 편의 메서드
    public void addPlace(RoutePlace place) {
        places.add(place);
        place.setTravelRoute(this);
    }

    public void addTag(TravelRouteTag tag) {
        tags.add(tag);
    }
}
