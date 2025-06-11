package com.test.foodtrip.domain.chat.controller;

import com.test.foodtrip.domain.chat.service.ChatSuggestionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/suggestions")
public class ChatSuggestionController {

    private final ChatSuggestionService chatSuggestionService;

    @Operation(
            summary = "채팅방 추천 검색어 조회",
            description = "입력한 키워드를 바탕으로 자동완성 형태의 추천 검색어 목록을 반환합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "추천어 목록 반환"),
            @ApiResponse(responseCode = "400", description = "쿼리 파라미터 누락 또는 잘못된 입력")
    })
    @GetMapping
    public List<String> getSuggestions(@RequestParam("keyword") String keyword) {
        return chatSuggestionService.getSuggestions(keyword);
    }
}
