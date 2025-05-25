package com.test.foodtrip.domain.post.entity;

import com.test.foodtrip.domain.post.entity.enums.DeleteStatus;
import com.test.foodtrip.domain.post.entity.enums.ReactionType;
import com.test.foodtrip.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "COMMENTS",
        indexes = {
                @Index(name = "idx_comment_post_id", columnList = "post_id"),
                @Index(name = "idx_comment_parent_id", columnList = "parent_id")
        })
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "comment_seq")
    @SequenceGenerator(name = "comment_seq", sequenceName = "comment_seq", allocationSize = 1)
    @Column(name = "comment_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Lob
    @Column(name = "content", nullable = false)
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Comment parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL)
    private List<Comment> replies = new ArrayList<>();

    @Column(name = "is_deleted", nullable = false, length = 1)
    @Enumerated(EnumType.STRING)
    private DeleteStatus isDeleted = DeleteStatus.N;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "comment", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CommentReaction> reactions = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // 생성자
    public Comment(Post post, User user, String content) {
        this.post = post;
        this.user = user;
        this.content = content;
    }

    // 대댓글 생성 생성자
    public Comment(Post post, User user, String content, Comment parent) {
        this(post, user, content);
        this.parent = parent;
    }

    // 편의 메서드
    public void addReply(Comment reply) {
        this.replies.add(reply);
        reply.setParent(this);
    }

    public void addReaction(CommentReaction reaction) {
        this.reactions.add(reaction);
        reaction.setComment(this);
    }

    // 좋아요 개수 계산
    public long getLikeCount() {
        return reactions.stream()
                .filter(r -> r.getReactionType() == ReactionType.LIKE)
                .count();
    }

    // 싫어요 개수 계산
    public long getDislikeCount() {
        return reactions.stream()
                .filter(r -> r.getReactionType() == ReactionType.DISLIKE)
                .count();
    }

    // 댓글이 삭제되었는지 확인
    public boolean isDeleted() {
        return this.isDeleted == DeleteStatus.Y;
    }

    // 댓글이 블라인드 처리되었는지 확인
    public boolean isBlinded() {
        return getDislikeCount() >= 10;
    }

    // 댓글 삭제 처리
    public void delete() {
        this.isDeleted = DeleteStatus.Y;
    }

    // 화면에 표시될 내용 반환
    public String getDisplayContent() {
        if (isDeleted()) {
            return "삭제된 댓글입니다";
        } else if (isBlinded()) {
            return "블라인드 처리된 댓글입니다";
        } else {
            return content;
        }
    }

    // 인기 댓글인지 확인 (좋아요 5개 이상)
    public boolean isPopular() {
        return getLikeCount() >= 5;
    }
}