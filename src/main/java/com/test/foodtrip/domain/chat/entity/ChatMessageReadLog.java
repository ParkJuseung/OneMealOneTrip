package com.test.foodtrip.domain.chat.entity;


import com.test.foodtrip.domain.user.entity.User;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "CHAT_MESSAGE_READ_LOG")
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatMessageReadLog {

    @EmbeddedId
    private ChatMessageReadLogId id;

    @MapsId("messageId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "message_id")
    private ChatMessage message;

    @MapsId("userId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "read_at", nullable = false, updatable = false)
    private LocalDateTime readAt;

    @PrePersist
    public void prePersist() {
        this.readAt = LocalDateTime.now();
    }

    // 복합키 클래스
    @Embeddable
    @Getter @Setter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class ChatMessageReadLogId implements java.io.Serializable {

        private static final long serialVersionUID = 1L;

        @Column(name = "message_id")
        private Long messageId;

        @Column(name = "user_id")
        private Long userId;

        public ChatMessageReadLogId(Long messageId, Long userId) {
            this.messageId = messageId;
            this.userId = userId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            ChatMessageReadLogId that = (ChatMessageReadLogId) o;

            if (!messageId.equals(that.messageId)) return false;
            return userId.equals(that.userId);
        }

        @Override
        public int hashCode() {
            int result = messageId.hashCode();
            result = 31 * result + userId.hashCode();
            return result;
        }
    }
}
