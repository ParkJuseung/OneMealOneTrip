package com.test.foodtrip.domain.user.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "USERS_PHOTO")
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UsersPhoto {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "users_photo_seq")
    @SequenceGenerator(name = "users_photo_seq", sequenceName = "users_photo_seq", allocationSize = 1)
    @Column(name = "photo_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "photo_url", nullable = false, length = 255)
    private String photoUrl;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "is_featured", nullable = false, length = 1)
    private String isFeatured = "N";

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}
