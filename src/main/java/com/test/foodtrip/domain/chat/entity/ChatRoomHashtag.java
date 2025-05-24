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
public class ChatRoomHashtag {

    @EmbeddedId
    private ChatRoomHashtagId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("chatRoomId")
    @JoinColumn(name = "chatroom_id")
    private ChatRoom chatRoom;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("hashtagId")
    @JoinColumn(name = "hashtag_id")
    private Hashtag hashtag;

    public static ChatRoomHashtag of(ChatRoom chatRoom, Hashtag hashtag) {
        return ChatRoomHashtag.builder()
                .id(new ChatRoomHashtagId(chatRoom.getId(), hashtag.getId()))
                .chatRoom(chatRoom)
                .hashtag(hashtag)
                .build();
    }
}
