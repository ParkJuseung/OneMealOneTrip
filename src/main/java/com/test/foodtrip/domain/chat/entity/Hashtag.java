package com.test.foodtrip.domain.chat.entity;


import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "HASHTAG")
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Hashtag {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "hashtag_seq")
    @SequenceGenerator(name = "hashtag_seq", sequenceName = "hashtag_seq", allocationSize = 1)
    @Column(name = "hashtag_id")
    private Long id;

    @Column(name = "tag_text", nullable = false, length = 30, unique = true)
    private String tagText;

    @ManyToMany(mappedBy = "hashtags")
    private List<ChatRoom> chatRooms = new ArrayList<>();
}
