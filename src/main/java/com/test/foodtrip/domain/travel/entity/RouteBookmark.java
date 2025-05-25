package com.test.foodtrip.domain.travel.entity;


import com.test.foodtrip.domain.user.entity.User;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "RouteBookmark", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"route_id", "user_id"})
})
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RouteBookmark {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "route_bookmark_seq")
    @SequenceGenerator(name = "route_bookmark_seq", sequenceName = "route_bookmark_seq", allocationSize = 1)
    @Column(name = "bookmark_id")
    private Long bookmarkId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "route_id")
    private TravelRoute travelRoute;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}