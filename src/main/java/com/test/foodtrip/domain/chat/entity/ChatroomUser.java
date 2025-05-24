package com.test.foodtrip.domain.chat.entity;


import com.test.foodtrip.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Entity
@Table(name = "chatroomuser")
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
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Builder.Default
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
    
    // 사용자가 채팅방에 나갔다가 다시 들어왔을때
    public void rejoin(Long lastMessageId) {
        this.status = "JOINED";
        this.joinedAt = LocalDateTime.now();
        this.statusUpdatedAt = LocalDateTime.now();
        this.lastReadMessageId = lastMessageId;
    }
    
    // 채팅방 삭제 및 채팅방 나갈경우 status = LEFT로 수정함.
    public void leave() {
        this.status = "LEFT";
        this.leftAt = LocalDateTime.now();
        this.statusUpdatedAt = LocalDateTime.now();
    }

    // 채팅방에서 사용자 강퇴 할 경우 KICKED 로 상태 바꿈.
    public void kick() {
        this.status = "KICKED";
        this.leftAt = LocalDateTime.now();
        this.statusUpdatedAt = LocalDateTime.now();
    }
}
