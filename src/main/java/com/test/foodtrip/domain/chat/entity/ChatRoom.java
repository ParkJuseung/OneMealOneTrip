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
@Table(name = "chatroom")
@Getter 
@NoArgsConstructor(access = AccessLevel.PROTECTED) // JPA 내부용 기본 생성자
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
    private String isDeleted = "N"; // 기본값: 삭제되지 않음

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // 양방향 관계
    @OneToMany(mappedBy = "chatRoom", cascade = CascadeType.ALL, orphanRemoval = true)
    // 하나의 채팅방은 여러명의 사용자를 가질 수 있다.
    private List<ChatroomUser> users = new ArrayList<>();

    @OneToMany(mappedBy = "chatRoom", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ChatMessage> messages = new ArrayList<>();

    @OneToMany(mappedBy = "chatRoom", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ChatroomNoticeHistory> notices = new ArrayList<>();

    @OneToMany(mappedBy = "chatRoom", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ChatroomLike> likes = new ArrayList<>();

    @ManyToMany
    @JoinTable(
            name = "CHATROOMHASHTAG",
            joinColumns = @JoinColumn(name = "chatroom_id"),
            inverseJoinColumns = @JoinColumn(name = "hashtag_id")
    )
    private List<Hashtag> hashtags = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // 편의 메서드
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

    public void addHashtag(Hashtag hashtag) {
        hashtags.add(hashtag);
    }
}
