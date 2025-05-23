package com.test.foodtrip.domain.post.entity;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "POSTTAGGING")
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PostTagging {

    @EmbeddedId
    private PostTaggingId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("postId")
    @JoinColumn(name = "post_id")
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("postTagId")
    @JoinColumn(name = "post_tag_id")
    private PostTag postTag;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        // PrePersist에서 ID가 null인 경우 설정
        if (this.id == null && this.post != null && this.postTag != null) {
            this.id = new PostTaggingId(this.post.getId(), this.postTag.getId());
        }
    }

    // 생성자 - ID 설정을 나중에 하도록 수정
    public PostTagging(Post post, PostTag postTag) {
        this.post = post;
        this.postTag = postTag;
        // Post가 이미 저장되어 ID가 있는 경우에만 복합키 생성
        if (post.getId() != null && postTag.getId() != null) {
            this.id = new PostTaggingId(post.getId(), postTag.getId());
        }
    }

    // 복합키 클래스
    @Embeddable
    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class PostTaggingId implements java.io.Serializable {

        @Column(name = "post_id")
        private Long postId;

        @Column(name = "post_tag_id")
        private Long postTagId;

        public PostTaggingId(Long postId, Long postTagId) {
            this.postId = postId;
            this.postTagId = postTagId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            PostTaggingId that = (PostTaggingId) o;

            if (!postId.equals(that.postId)) return false;
            return postTagId.equals(that.postTagId);
        }

        @Override
        public int hashCode() {
            int result = postId.hashCode();
            result = 31 * result + postTagId.hashCode();
            return result;
        }
    }
}