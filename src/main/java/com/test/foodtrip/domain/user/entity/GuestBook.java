package com.test.foodtrip.domain.user.entity;


import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "GUEST_BOOK")
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GuestBook {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "guestbook_seq")
    @SequenceGenerator(name = "guestbook_seq", sequenceName = "guestbook_seq", allocationSize = 1)
    @Column(name = "guestbook_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "writer_id")
    private User writer;

    @Column(name = "content", nullable = false, length = 500)
    private String content;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}
