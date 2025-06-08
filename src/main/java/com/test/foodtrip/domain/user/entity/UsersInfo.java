package com.test.foodtrip.domain.user.entity;

import lombok.*;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "USERS_INFO")
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class UsersInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "users_info_seq")
    @SequenceGenerator(name = "users_info_seq", sequenceName = "users_intro_seq", allocationSize = 1)
    @Column(name = "intro_id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", unique = true)
    private User user;

    @Column(name = "content", length = 2000)
    private String content;

    @Column(name = "tags", length = 1000)
    private String tags;

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
