package com.test.foodtrip.domain.post.entity;

import com.test.foodtrip.domain.user.entity.User;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "COMMENTREACTION",
        uniqueConstraints = @UniqueConstraint(columnNames = {"comment_id", "user_id"}))
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

    // 반응 타입 enum
    public enum ReactionType {
        LIKE, DISLIKE
    }

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
}