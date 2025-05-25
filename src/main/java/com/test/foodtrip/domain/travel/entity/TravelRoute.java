package com.test.foodtrip.domain.travel.entity;


import com.test.foodtrip.domain.user.entity.User;
import lombok.*;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder(toBuilder = true)
@Entity
@Table(name = "TravelRoute")
public class TravelRoute {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "travel_route_seq")
    @SequenceGenerator(name = "travel_route_seq", sequenceName = "travel_route_seq", allocationSize = 1)
    @Column(name = "route_id")
    private Long routeId;

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

    @Column(name = "views")
    private Integer views;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // 양방향 관계
    @Builder.Default
    @OneToMany(mappedBy = "travelRoute", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RoutePlace> places = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "travelRoute", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RouteBookmark> bookmarks = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "travelRoute", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TravelRouteTagging> tags = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // 연관관계 편의 메서드
    public void addPlace(RoutePlace place) {
        places.add(place);
        place.setRoute(this);
    }

    public void addTagging(TravelRouteTagging tagging) {
        tags.add(tagging);
        tagging.setRoute(this);
    }

    public void increaseViews() {
        this.views = (this.views == null ? 1 : this.views + 1);
    }

    public void update(String title, String description, Double totalDistance, Integer totalTime) {
        this.title = title;
        this.description = description;
        this.totalDistance = totalDistance;
        this.totalTime = totalTime;
    }

    public void clearPlaces() {
        if (this.places != null) {
            this.places.clear(); // 연관관계 제거
        }
    }

    public void clearTags() {
        if (this.tags != null) {
            this.tags.clear(); // 연관관계 제거
        }
    }

}
