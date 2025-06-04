package com.test.foodtrip.domain.user.controller;

import com.test.foodtrip.domain.user.dto.MyPageDTO;
import com.test.foodtrip.domain.user.dto.VisitorStatsDTO;
import com.test.foodtrip.domain.user.service.VisitorService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

/**
 * 사용자별 방문자 통계 API
 */
@RestController
@RequestMapping("/users/{userId}/visitor-stats")
@RequiredArgsConstructor
public class VisitorController {

    private final VisitorService visitorService;


    /**
     * GET /users/{userId}/visitor-stats
     * pageOwnerId(userId)에 대한
     * 당일·주간·월간·누적 방문자 통계를 반환
     */
    @GetMapping
    public ResponseEntity<VisitorStatsDTO> getVisitorStats(
            @PathVariable("userId") Long pageOwnerId) {
        VisitorStatsDTO stats = visitorService.getStats(pageOwnerId);
        return ResponseEntity.ok(stats);
    }





}
