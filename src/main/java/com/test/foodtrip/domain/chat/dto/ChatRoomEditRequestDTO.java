package com.test.foodtrip.domain.chat.dto;

import lombok.Builder;
import lombok.Getter;
import java.util.List;

//채팅방 수정 요청 시 클라이언트가 보내는 데이터 구조
@Getter
@Builder
public class ChatRoomEditRequestDTO {
    private String title;         // 수정할 제목
    private String notice;        // 공지사항 텍스트
    private String description;   // 설명
    private List<String> hashtags; // 해시태그 목록
}
