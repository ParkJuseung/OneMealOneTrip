package com.test.foodtrip.domain.chat.controller;

import com.test.foodtrip.domain.chat.service.ChatSuggestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/suggestions")
public class ChatSuggestionController {

    private final ChatSuggestionService chatSuggestionService;

    @GetMapping
    public List<String> getSuggestions(@RequestParam("keyword") String keyword) {
        return chatSuggestionService.getSuggestions(keyword);
    }
}
