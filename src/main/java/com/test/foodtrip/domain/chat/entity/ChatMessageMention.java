package com.test.foodtrip.domain.chat.entity;


import com.test.foodtrip.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "CHATMESSAGEMENTION")
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatMessageMention {

    @EmbeddedId
    private ChatMessageMentionId id;

    @MapsId("messageId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "message_id")
    private ChatMessage message;

    @MapsId("mentionedUserId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mentioned_user_id")
    private User mentionedUser;

    // 복합키 클래스
    @Embeddable
    @Getter @Setter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class ChatMessageMentionId implements java.io.Serializable {

        private static final long serialVersionUID = 1L;

        @Column(name = "message_id")
        private Long messageId;

        @Column(name = "mentioned_user_id")
        private Long mentionedUserId;

        public ChatMessageMentionId(Long messageId, Long mentionedUserId) {
            this.messageId = messageId;
            this.mentionedUserId = mentionedUserId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            ChatMessageMentionId that = (ChatMessageMentionId) o;

            if (!messageId.equals(that.messageId)) return false;
            return mentionedUserId.equals(that.mentionedUserId);
        }

        @Override
        public int hashCode() {
            int result = messageId.hashCode();
            result = 31 * result + mentionedUserId.hashCode();
            return result;
        }
    }
}
