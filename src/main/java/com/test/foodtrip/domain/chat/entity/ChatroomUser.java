package com.test.foodtrip.domain.chat.entity;


import com.test.foodtrip.domain.user.entity.User;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "CHATROOMUSER")
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatroomUser {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "chatroom_user_seq")
    @SequenceGenerator(name = "chatroom_user_seq", sequenceName = "chatroom_user_seq", allocationSize = 1)
    @Column(name = "chatroom_user_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chatroom_id")
    private ChatRoom chatRoom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "role", nullable = false, length = 10)
    private String role = "MEMBER";

    @Column(name = "profile_image_url", length = 255)
    private String profileImageUrl;

    @Column(name = "joined_at", nullable = false, updatable = false)
    private LocalDateTime joinedAt;

    @Column(name = "left_at")
    private LocalDateTime leftAt;

    @Column(name = "status", length = 10)
    private String status;

    @Column(name = "status_updated_at")
    private LocalDateTime statusUpdatedAt;

    @Column(name = "last_read_message_id")
    private Long lastReadMessageId;

    @PrePersist
    public void prePersist() {
        this.joinedAt = LocalDateTime.now();
    }
}
