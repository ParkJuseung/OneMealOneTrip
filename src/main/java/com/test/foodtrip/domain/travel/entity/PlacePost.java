package com.test.foodtrip.domain.travel.entity;


import com.test.foodtrip.domain.user.entity.User;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "PlacePost")
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PlacePost {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "place_post_seq")
    @SequenceGenerator(name = "place_post_seq", sequenceName = "place_post_seq", allocationSize = 1)
    @Column(name = "review_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "place_name", nullable = false, length = 200)
    private String placeName;

    @Column(name = "address", length = 255)
    private String address;

    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

    @Column(name = "content", nullable = false, length = 500)
    private String content;

    @Column(name = "rating")
    private Double rating = 0.0;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}

