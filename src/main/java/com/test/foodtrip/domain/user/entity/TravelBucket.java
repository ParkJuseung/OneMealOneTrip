package com.test.foodtrip.domain.user.entity;


import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "TRAVEL_BUCKET")
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TravelBucket {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "travel_bucket_seq")
    @SequenceGenerator(name = "travel_bucket_seq", sequenceName = "travel_bucket_seq", allocationSize = 1)
    @Column(name = "bucket_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "content", nullable = false, length = 500)
    private String content;

    @Column(name = "is_completed", nullable = false, length = 1)
    private String isCompleted = "N";

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}