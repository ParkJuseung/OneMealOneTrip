package com.test.foodtrip.domain.chat.entity;

import lombok.*;

import java.io.Serializable;
import java.util.Objects;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChatRoomHashtagId implements Serializable {
    private Long chatRoom;
    private Long hashtag;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ChatRoomHashtagId)) return false;
        ChatRoomHashtagId that = (ChatRoomHashtagId) o;
        return Objects.equals(chatRoom, that.chatRoom) &&
                Objects.equals(hashtag, that.hashtag);
    }

    @Override
    public int hashCode() {
        return Objects.hash(chatRoom, hashtag);
    }
}
