package com.test.foodtrip.domain.travel.controller;

import com.test.foodtrip.domain.travel.dto.CreateTravelRouteDTO;
import com.test.foodtrip.domain.travel.dto.UpdateTravelRouteDTO;
import com.test.foodtrip.domain.travel.service.TravelRouteService;
import com.test.foodtrip.domain.user.dto.UserPrincipal;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/travel")
@RequiredArgsConstructor
public class TravelRouteRestController {

    private final TravelRouteService travelRouteService;

    @PostMapping
    public ResponseEntity<?> createTravelRoute(@RequestBody CreateTravelRouteDTO dto, HttpSession session) {
        Long userId = (Long) session.getAttribute("user_id");
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인 정보 없음");
        }
        dto.setUserId(userId);
        travelRouteService.createTravelRoute(dto);
        return ResponseEntity.ok("저장 성공");
    }

    @GetMapping("/tags")
    public ResponseEntity<List<String>> getTags() {
        return ResponseEntity.ok(travelRouteService.getTagList());
    }

    @GetMapping("/photos")
    public ResponseEntity<List<String>> getPhotos(@RequestParam String placeId) {
        return ResponseEntity.ok(travelRouteService.getPhotoUrls(placeId));
    }

    @GetMapping("/thumbnail")
    public ResponseEntity<Map<String, String>> getThumbnail(@RequestParam Long routeId) {
        String imageUrl = travelRouteService.getThumbnailUrlByRouteId(routeId);
        return ResponseEntity.ok(Map.of("url", imageUrl));
    }

    @PatchMapping("/{routeId}/views")
    public ResponseEntity<Void> increaseViews(@PathVariable Long routeId, HttpSession session) {

        // 1. 세션에서 방문한 routeId 목록 가져오기
        List<Long> viewed = (List<Long>) session.getAttribute("viewedRouteIds");
        if (viewed == null) {
            viewed = new ArrayList<>();
        }

        // 2. 현재 routeId가 없다면 → 조회수 증가 + 세션에 추가
        if (!viewed.contains(routeId)) {
            travelRouteService.increaseViews(routeId);
            viewed.add(routeId);
            session.setAttribute("viewedRouteIds", viewed);
        }

        return ResponseEntity.ok().build();
    }

    @PutMapping("/{routeId}")
    public ResponseEntity<Void> updateRoute(@PathVariable Long routeId,
                                            @RequestBody UpdateTravelRouteDTO dto) {
        travelRouteService.updateRoute(routeId, dto);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{routeId}")
    public ResponseEntity<Void> deleteRoute(@PathVariable Long routeId) {
        travelRouteService.deleteRoute(routeId);
        return ResponseEntity.ok().build();
    }

}
