package com.test.foodtrip.domain.chat.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class MarkReadRequestDTO {
    private Long lastMessageId;
}
