package com.test.foodtrip.domain.chat.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "ChatRoomHashtag")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@IdClass(ChatRoomHashtagId.class)
public class ChatRoomHashtag {

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chatroom_id")
    private ChatRoom chatRoom;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hashtag_id")
    private Hashtag hashtag;
}
