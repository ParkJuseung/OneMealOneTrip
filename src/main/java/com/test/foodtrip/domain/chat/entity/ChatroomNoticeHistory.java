package com.test.foodtrip.domain.chat.entity;


import lombok.*;
import jakarta.persistence.*;
import java.time.LocalDateTime;


@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder // ← 추가!
@Entity
@Table(name = "CHATROOM_NOTICE_HISTORY")
public class ChatroomNoticeHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "chatroom_notice_seq_generator")
    @SequenceGenerator(
            name = "chatroom_notice_seq_generator",
            sequenceName = "chatroom_notice_history_seq",
            allocationSize = 1
    )
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
