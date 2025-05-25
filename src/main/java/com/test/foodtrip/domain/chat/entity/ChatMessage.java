package com.test.foodtrip.domain.chat.entity;


import com.test.foodtrip.domain.user.entity.User;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "CHATMESSAGE")
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "chat_message_seq")
    @SequenceGenerator(name = "chat_message_seq", sequenceName = "chat_message_seq", allocationSize = 1)
    @Column(name = "message_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chatroom_id")
    private ChatRoom chatRoom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "message_type", nullable = false, length = 20)
    private String messageType = "TEXT";

    @Lob
    @Column(name = "message_content")
    private String messageContent;

    @Column(name = "file_url", length = 255)
    private String fileUrl;

    @Column(name = "file_type", length = 50)
    private String fileType;

    private Double latitude;

    private Double longitude;

    @Column(name = "location_description", length = 255)
    private String locationDescription;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // 양방향 관계
    @OneToMany(mappedBy = "message", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ChatMessageReadLog> readLogs = new ArrayList<>();

    @OneToMany(mappedBy = "message", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ChatMessageMention> mentions = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    // 편의 메서드
    public void addReadLog(ChatMessageReadLog readLog) {
        readLogs.add(readLog);
        readLog.setMessage(this);
    }

    public void addMention(ChatMessageMention mention) {
        mentions.add(mention);
        mention.setMessage(this);
    }
    
    public static ChatMessage create(ChatRoom chatRoom, User user, String messageContent) {
        ChatMessage message = new ChatMessage();
        message.chatRoom = chatRoom; // chatRoom이 메시지가 속한 채팅방
        message.user = user; // user 메시지를 보낸 사용자
        message.messageType = "TEXT"; 
        message.messageContent = messageContent; // messageContent 메시지 내용
        message.createdAt = LocalDateTime.now(); // 혹은 prePersist로 자동 처리됨
        return message;
    }
}
