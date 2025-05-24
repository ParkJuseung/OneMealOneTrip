package com.test.foodtrip.domain.chat.entity;


import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "HASHTAG")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Hashtag {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "hashtag_seq")
    @SequenceGenerator(name = "hashtag_seq", sequenceName = "hashtag_seq", allocationSize = 1)
    @Column(name = "hashtag_id")
    private Long id;

    @Column(name = "tag_text", nullable = false, length = 30, unique = true)
    private String tagText;

    @OneToMany(mappedBy = "hashtag", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ChatRoomHashtag> chatRoomHashtags = new ArrayList<>();
}
