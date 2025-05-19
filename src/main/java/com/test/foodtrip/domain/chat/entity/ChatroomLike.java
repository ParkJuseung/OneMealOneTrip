package com.test.foodtrip.domain.chat.entity;


import com.test.foodtrip.domain.user.entity.User;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "CHATROOMLIKE", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"chatroom_id", "user_id"})
})
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatroomLike {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "chatroom_like_seq")
    @SequenceGenerator(name = "chatroom_like_seq", sequenceName = "chatroom_like_seq", allocationSize = 1)
    @Column(name = "like_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chatroom_id")
    private ChatRoom chatRoom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "is_active", nullable = false, length = 1)
    private String isActive = "Y";

    @Column(name = "liked_at", nullable = false, updatable = false)
    private LocalDateTime likedAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        this.likedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
