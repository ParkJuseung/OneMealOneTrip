package com.test.foodtrip.domain.chat.entity;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.test.foodtrip.domain.user.entity.User;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "CHATROOM")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class ChatRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "chat_room_seq")
    @SequenceGenerator(name = "chat_room_seq", sequenceName = "CHAT_ROOM_SEQ", allocationSize = 1)
    @Column(name = "chatroom_id")
    private Long id;

    @Column(name = "title", length = 100, nullable = false)
    private String title;

    @Column(name = "thumbnail_image_url", length = 255)
    private String thumbnailImageUrl;

    @Builder.Default
    @Column(name = "is_deleted", length = 1, nullable = false)
    private String isDeleted = "N";

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Builder.Default
    @OneToMany(mappedBy = "chatRoom", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ChatroomUser> users = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "chatRoom", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ChatMessage> messages = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "chatRoom", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ChatroomNoticeHistory> notices = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "chatRoom", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ChatroomLike> likes = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "chatRoom", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ChatRoomHashtag> chatRoomHashtags = new ArrayList<>();
    
    // 채팅방 유저에 대한 log 처리용 채팅방 역할 컬럼을 나누기 위해서
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User user; // 방장 


    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public void addUser(ChatroomUser user) {
        users.add(user);
        user.setChatRoom(this);
    }

    public void addMessage(ChatMessage message) {
        messages.add(message);
        message.setChatRoom(this);
    }

    public void addNotice(ChatroomNoticeHistory notice) {
        notices.add(notice);
        notice.setChatRoom(this);
    }

    public void updateTitle(String title) {
        this.title = title;
    }

    public void updateThumbnailImageUrl(String url) {
        this.thumbnailImageUrl = url;
    }

    public void markAsDeleted() {
        this.isDeleted = "Y";
    }

}
