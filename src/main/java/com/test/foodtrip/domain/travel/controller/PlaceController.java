package com.test.foodtrip.domain.travel.controller;

import com.test.foodtrip.domain.travel.service.GooglePlaceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@Tag(name = "장소 이미지 API", description = "Google Places를 통한 장소 이미지 조회 API")
@RestController
@RequestMapping("/api/place")
@RequiredArgsConstructor
public class PlaceController {

    private final GooglePlaceService googlePlaceService;

    @Operation(
            summary = "장소 사진 목록 조회",
            description = "Google Places API를 통해 해당 placeId의 사진 목록을 가져옵니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "사진 목록 조회 성공",
                            content = @Content(array = @ArraySchema(schema = @Schema(implementation = String.class)))),
                    @ApiResponse(
                            responseCode = "400",
                            description = "잘못된 요청 파라미터",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(example = "{\"error\": \"Invalid placeId\"}")
                            )
                    ),
                    @ApiResponse(responseCode = "500", description = "서버 오류")
            }
    )
    @GetMapping("/photos")
    public ResponseEntity<List<String>> getPhotoUrls(
            @Parameter(description = "Google Place ID", example = "ChIJDbdkHFQayUwR7-8fITgxTmU")
            @RequestParam String placeId) {
        List<String> references = googlePlaceService.getPhotoReferences(placeId);
        return ResponseEntity.ok(references);
    }

    @Operation(
            summary = "장소 대표 사진 조회",
            description = "placeId로 첫 번째 사진만 반환합니다 (썸네일 용도).",
            responses = {
                    @ApiResponse(responseCode = "200", description = "대표 사진 URL 반환",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(example = "{\"url\": \"/images/default-thumbnail.jpg\"}"))),
                    @ApiResponse(responseCode = "404", description = "이미지를 찾을 수 없음"),
                    @ApiResponse(responseCode = "500", description = "서버 오류")
            }
    )
    @GetMapping("/photos/first")
    public ResponseEntity<Map<String, String>> getFirstPhoto(
            @Parameter(
                    description = "Google Place ID",
                    example = "ChIJDbdkHFQayUwR7-8fITgxTmU",
                    required = true
            )
            @RequestParam("placeId") String placeId) {
        List<String> urls = googlePlaceService.getPhotoUrlsByPlaceId(placeId);
        if (urls.isEmpty()) {
            return ResponseEntity.ok(Map.of("url", "/images/default-thumbnail.jpg"));
        }
        return ResponseEntity.ok(Map.of("url", urls.get(0)));
    }

    @Operation(
            summary = "사진 프록시 전달",
            description = "사진 참조값(photoReference)을 이용해 이미지 바이트 데이터를 직접 반환합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "이미지 바이트 반환",
                            content = @Content(mediaType = "image/jpeg")),
                    @ApiResponse(responseCode = "400", description = "유효하지 않은 photoReference"),
                    @ApiResponse(responseCode = "404", description = "이미지를 찾을 수 없음"),
                    @ApiResponse(responseCode = "500", description = "서버 오류")
            }
    )
    @GetMapping("/photo-proxy")
    public ResponseEntity<byte[]> proxyPhoto(
            @Parameter(description = "Google Photo Reference", example = "Aap_uECi...")
            @RequestParam String photoReference) {
        byte[] image = googlePlaceService.getPhotoImage(photoReference);
        return ResponseEntity
                .ok()
                .contentType(MediaType.IMAGE_JPEG)
                .body(image);
    }

    @GetMapping("/api/place/photo-references")
    public ResponseEntity<List<String>> getPhotoReferences(@RequestParam String placeId) {
        List<String> references = googlePlaceService.getPhotoReferences(placeId);

        if (references.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(references);
    }

}
