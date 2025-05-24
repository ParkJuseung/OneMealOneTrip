package com.test.foodtrip.domain.post.entity;

import com.test.foodtrip.domain.post.entity.enums.ReactionType;
import com.test.foodtrip.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "COMMENTREACTION",
        uniqueConstraints = @UniqueConstraint(columnNames = {"comment_id", "user_id"}),
        indexes = {
                @Index(name = "idx_reaction_comment_id", columnList = "comment_id"),
                @Index(name = "idx_reaction_user_id", columnList = "user_id"),
                @Index(name = "idx_reaction_type", columnList = "reaction_type")
        })
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CommentReaction {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "comment_reaction_seq")
    @SequenceGenerator(name = "comment_reaction_seq", sequenceName = "comment_reaction_seq", allocationSize = 1)
    @Column(name = "reaction_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comment_id", nullable = false)
    private Comment comment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "reaction_type", nullable = false, length = 10)
    @Enumerated(EnumType.STRING)
    private ReactionType reactionType;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // 생성자
    public CommentReaction(Comment comment, User user, ReactionType reactionType) {
        this.comment = comment;
        this.user = user;
        this.reactionType = reactionType;
    }

    // 팩토리 메서드 (정적 생성 메서드)
    public static CommentReaction createLike(Comment comment, User user) {
        return new CommentReaction(comment, user, ReactionType.LIKE);
    }

    public static CommentReaction createDislike(Comment comment, User user) {
        return new CommentReaction(comment, user, ReactionType.DISLIKE);
    }

    // 반응 타입 변경 메서드
    public void toggleReactionType() {
        this.reactionType = (this.reactionType == ReactionType.LIKE)
                ? ReactionType.DISLIKE
                : ReactionType.LIKE;
    }
}