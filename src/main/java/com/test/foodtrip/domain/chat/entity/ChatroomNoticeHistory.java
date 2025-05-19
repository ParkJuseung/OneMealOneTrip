package com.test.foodtrip.domain.chat.entity;


import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "CHATROOMNOTICEHISTORY")
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatroomNoticeHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "chatroom_notice_history_seq")
    @SequenceGenerator(name = "chatroom_notice_history_seq", sequenceName = "chatroom_notice_history_seq", allocationSize = 1)
    @Column(name = "notice_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chatroom_id")
    private ChatRoom chatRoom;

    @Column(name = "content", length = 100)
    private String content;

    @Column(name = "description", length = 200)
    private String description;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}
