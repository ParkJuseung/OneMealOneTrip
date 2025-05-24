package com.test.foodtrip.domain.chat.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Objects;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class ChatRoomHashtagId implements Serializable {

    @Column(name = "chatroom_id")
    private Long chatRoomId;

    @Column(name = "hashtag_id")
    private Long hashtagId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ChatRoomHashtagId that)) return false;
        return Objects.equals(chatRoomId, that.chatRoomId) &&
                Objects.equals(hashtagId, that.hashtagId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(chatRoomId, hashtagId);
    }
}
