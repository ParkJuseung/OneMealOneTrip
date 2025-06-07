package com.test.foodtrip.domain.travel.controller;

import com.test.foodtrip.domain.travel.dto.CreateTravelRouteDTO;
import com.test.foodtrip.domain.travel.dto.UpdateTravelRouteDTO;
import com.test.foodtrip.domain.travel.service.TravelRouteService;
import com.test.foodtrip.domain.user.dto.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/travel")
@RequiredArgsConstructor
@Tag(name = "여행 경로 API", description = "여행 코스 등록, 조회, 북마크 관련 API")
public class TravelRouteRestController {

    private final TravelRouteService travelRouteService;

    @Operation(
            summary = "여행 코스 생성",
            description = "사용자가 여행 코스를 생성합니다. 로그인 사용자 정보가 필요합니다.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CreateTravelRouteDTO.class),
                            examples = {
                                    @ExampleObject(
                                            name = "코스 생성 예시",
                                            value = """
                    {
                      "title": "제주도 여행",
                      "description": "가족과 함께 떠나는 제주도 3박 4일",
                      "places": [...],
                      "tagIds": [1, 2]
                    }
                    """
                                    )
                            }
                    )
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "저장 성공"),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청",
                            content = @Content(schema = @Schema(example = "{\"error\": \"유효하지 않은 요청\"}"))),
                    @ApiResponse(responseCode = "401", description = "인증 실패",
                            content = @Content(schema = @Schema(example = "{\"error\": \"로그인 정보 없음\"}")))
            }
    )
    @PostMapping
    public ResponseEntity<?> createTravelRoute(
            @RequestBody CreateTravelRouteDTO dto,
            @Parameter(hidden = true) @AuthenticationPrincipal UserPrincipal loginUser) {
        if (loginUser == null || loginUser.getUserId() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인 정보 없음");
        }
        dto.setUserId(loginUser.getUserId());
        travelRouteService.createTravelRoute(dto);
        return ResponseEntity.ok("저장 성공");
    }

    @Operation(summary = "태그 목록 조회", description = "모든 등록된 태그 문자열 목록을 반환합니다.")
    @Parameter(hidden = true) // 없음
    @ApiResponse(
            responseCode = "200",
            description = "성공",
            content = @Content(array = @ArraySchema(schema = @Schema(example = "자연", type = "string")))
    )
    @GetMapping("/tags")
    public ResponseEntity<List<String>> getTags() {
        return ResponseEntity.ok(travelRouteService.getTagList());
    }

    @Operation(summary = "장소 이미지 조회", description = "placeId에 해당하는 장소 이미지 URL 목록을 반환합니다.")
    @ApiResponse(responseCode = "200", description = "성공")
    @Parameter(name = "placeId", description = "Google Place ID", example = "ChIJd...")
    @GetMapping("/photos")
    public ResponseEntity<List<String>> getPhotos(@RequestParam("placeId") String placeId) {
        return ResponseEntity.ok(travelRouteService.getPhotoUrls(placeId));
    }

    @Operation(summary = "썸네일 이미지 조회", description = "routeId에 해당하는 여행 경로의 대표 이미지 URL을 반환합니다.")
    @Parameter(name = "routeId", description = "여행 코스 ID", example = "1")
    @ApiResponse(
            responseCode = "200",
            description = "썸네일 URL 반환",
            content = @Content(schema = @Schema(example = "{\"url\": \"https://example.com/thumbnail.jpg\"}"))
    )
    @GetMapping("/thumbnail")
    public ResponseEntity<Map<String, String>> getThumbnail(@RequestParam("routeId") Long routeId) {
        String imageUrl = travelRouteService.getThumbnailUrlByRouteId(routeId);
        return ResponseEntity.ok(Map.of("url", imageUrl));
    }

    @Operation(summary = "조회수 증가", description = "세션 기준으로 중복 없는 경우 조회수를 1 증가시킵니다.")
    @Parameter(name = "routeId", description = "여행 코스 ID", example = "10")
    @ApiResponse(
            responseCode = "200",
            description = "조회수 증가 성공"
    )
    @PatchMapping("/{routeId}/views")
    public ResponseEntity<Void> increaseViews(@PathVariable("routeId") Long routeId, HttpSession session) {

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

    @Operation(
            summary = "여행 코스 수정",
            description = "routeId에 해당하는 코스를 수정합니다. 사용자 권한 체크 포함.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = UpdateTravelRouteDTO.class),
                            examples = @ExampleObject(
                                    name = "수정 요청 예시",
                                    value = """
                {
                  "userId": 5,
                  "title": "수정된 제주도 여행",
                  "description": "3박 4일 일정 변경",
                  "places": [...],
                  "tagIds": [1, 3]
                }
                """
                            )
                    )
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "수정 성공"),
                    @ApiResponse(responseCode = "403", description = "권한 없음",
                            content = @Content(schema = @Schema(example = "{\"error\": \"수정 권한 없음\"}")))
            }
    )
    @PutMapping("/{routeId}")
    public ResponseEntity<Void> updateRoute(@PathVariable("routeId") Long routeId,
                                            @RequestBody UpdateTravelRouteDTO dto,
                                            @AuthenticationPrincipal UserPrincipal loginUser) {

        if (loginUser == null || !loginUser.getUserId().equals(dto.getUserId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build(); // 403
        }

        travelRouteService.updateRoute(routeId, dto);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "여행 코스 삭제", description = "routeId에 해당하는 코스를 삭제합니다. 작성자만 삭제 가능.")
    @Parameter(name = "routeId", description = "삭제할 여행 코스 ID", example = "7")
    @ApiResponse(
            responseCode = "403",
            description = "삭제 권한 없음",
            content = @Content(schema = @Schema(example = "{\"error\": \"삭제 권한 없음\"}"))
    )
    @DeleteMapping("/{routeId}")
    public ResponseEntity<Void> deleteRoute(@PathVariable("routeId") Long routeId,
                                            @AuthenticationPrincipal UserPrincipal loginUser) {

        Long ownerId = travelRouteService.getRouteDetail(routeId).getUserId();

        if (loginUser == null || !loginUser.getUserId().equals(ownerId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        travelRouteService.deleteRoute(routeId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "북마크 토글", description = "해당 여행 코스에 대해 북마크를 등록 또는 해제합니다.")
    @Parameter(name = "routeId", description = "북마크할 코스 ID", example = "12")
    @ApiResponse(
            responseCode = "200",
            description = "북마크 상태 반환",
            content = @Content(schema = @Schema(example = "{\"bookmarked\": true}"))
    )
    @ApiResponse(
            responseCode = "401",
            description = "로그인 필요",
            content = @Content(schema = @Schema(example = "{\"error\": \"unauthorized\"}"))
    )
    @PostMapping("/{routeId}/bookmark")
    public ResponseEntity<Map<String, Object>> toggleBookmark(@PathVariable("routeId") Long routeId,
                                                              @AuthenticationPrincipal UserPrincipal loginUser) {
        if (loginUser == null || loginUser.getUserId() == null) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "unauthorized");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }

        boolean bookmarked = travelRouteService.toggleBookmark(routeId, loginUser.getUserId());

        Map<String, Object> result = new HashMap<>();
        result.put("bookmarked", bookmarked);
        return ResponseEntity.ok(result);
    }

}
