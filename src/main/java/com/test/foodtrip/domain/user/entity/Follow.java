package com.test.foodtrip.domain.user.entity;

import lombok.*;  // lombok import
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "FOLLOW", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"follower_id", "following_id"})
})
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
// ↓ 빌더용 생성자와 어노테이션 추가
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class Follow {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "follow_seq")
    @SequenceGenerator(name = "follow_seq", sequenceName = "follow_seq", allocationSize = 1)
    @Column(name = "follow_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "follower_id")
    private User follower;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "following_id")
    private User following;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}
