package com.test.foodtrip.domain.post.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "POSTTAG")
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PostTag {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "post_tag_seq")
    @SequenceGenerator(name = "post_tag_seq", sequenceName = "post_tag_seq", allocationSize = 1)
    @Column(name = "post_tag_id")
    private Long id;

    @Column(name = "tag_text", nullable = false, unique = true, length = 30)
    private String tagText;

    @OneToMany(mappedBy = "postTag", cascade = CascadeType.ALL)
    private List<PostTagging> posts = new ArrayList<>();

    // 생성자
    public PostTag(String tagText) {
        this.tagText = tagText;
    }
}