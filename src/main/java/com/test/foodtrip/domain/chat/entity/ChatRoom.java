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

@Entity
@Table(name = "CHATROOM")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ChatRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "chat_room_seq")
    @SequenceGenerator(name = "chat_room_seq", sequenceName = "chat_room_seq", allocationSize = 1)
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

    public void softDelete() {
        this.isDeleted = "Y";
        this.updatedAt = LocalDateTime.now();
    }

    public void updateTitle(String title) {
        this.title = title;
    }

    public void markAsDeleted() {
        this.isDeleted = "Y";
    }

}
