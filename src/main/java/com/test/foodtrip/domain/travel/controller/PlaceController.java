package com.test.foodtrip.domain.travel.controller;

import com.test.foodtrip.domain.travel.service.GooglePlaceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/place")
@RequiredArgsConstructor
public class PlaceController {

    private final GooglePlaceService googlePlaceService;

    @GetMapping("/photos")
    public ResponseEntity<List<String>> getPlacePhotos(@RequestParam String placeId) {
        List<String> photoUrls = googlePlaceService.getPhotoUrlsByPlaceId(placeId);
        return ResponseEntity.ok(photoUrls);
    }

    /**
     * 첫 번째 사진만 반환 (썸네일 용)
     */
    @GetMapping("/photos/first")
    public ResponseEntity<Map<String, String>> getFirstPhoto(@RequestParam String placeId) {
        List<String> urls = googlePlaceService.getPhotoUrlsByPlaceId(placeId);
        if (urls.isEmpty()) {
            return ResponseEntity.ok(Map.of("url", "/images/default-thumbnail.jpg"));
        }
        return ResponseEntity.ok(Map.of("url", urls.get(0)));
    }

}
