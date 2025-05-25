package com.test.foodtrip.domain.chat.service;

import com.test.foodtrip.domain.chat.entity.ChatroomUser;
import com.test.foodtrip.domain.chat.repository.ChatRoomRepository;
import com.test.foodtrip.domain.chat.repository.ChatroomUserRepository;
import com.test.foodtrip.domain.chat.repository.HashtagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RequiredArgsConstructor
@Service
public class ChatSuggestionService {

    private final ChatRoomRepository chatRoomRepository;
    private final HashtagRepository hashtagRepository;
    private final ChatroomUserRepository chatroomUserRepository; // ✅ 추가

    public List<String> getSuggestions(String keyword) {
        // 1. 제목
        List<String> titleMatches = chatRoomRepository
                .findTop5ByTitleContainingIgnoreCaseOrderByTitleAsc(keyword)
                .stream()
                .map(room -> room.getTitle())
                .collect(Collectors.toList());

        // 2. 해시태그
        List<String> hashtagMatches = hashtagRepository
                .findTop5ByTagTextContainingIgnoreCaseOrderByTagTextAsc(keyword)
                .stream()
                .map(tag -> "#" + tag.getTagText())
                .collect(Collectors.toList());

        // 3. 방장 닉네임
        List<String> ownerMatches = chatroomUserRepository
                .findTop5ByRoleAndUserNicknameContainingIgnoreCaseOrderByUserNicknameAsc("OWNER", keyword)
                .stream()
                .map(u -> "@" + u.getUser().getNickname())
                .distinct()
                .collect(Collectors.toList());

        // 합쳐서 중복 제거 후 5개 제한
        return Stream.of(titleMatches, hashtagMatches, ownerMatches)
                .flatMap(List::stream)
                .distinct()
                .limit(5)
                .collect(Collectors.toList());
    }
}

