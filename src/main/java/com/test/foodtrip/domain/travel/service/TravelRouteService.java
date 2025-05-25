// TravelRouteService.java
package com.test.foodtrip.domain.travel.service;

import com.test.foodtrip.domain.travel.dto.*;
import com.test.foodtrip.domain.travel.entity.RoutePlace;
import com.test.foodtrip.domain.travel.entity.TravelRoute;
import com.test.foodtrip.domain.travel.entity.TravelRouteTag;
import com.test.foodtrip.domain.travel.entity.TravelRouteTagging;
import com.test.foodtrip.domain.travel.repository.RoutePlaceRepository;
import com.test.foodtrip.domain.travel.repository.TravelRouteRepository;
import com.test.foodtrip.domain.travel.repository.TravelRouteTagRepository;
import com.test.foodtrip.domain.user.entity.User;
import com.test.foodtrip.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TravelRouteService {

    private final GooglePlaceService googlePlaceService;
    private final TravelRouteRepository travelRouteRepository;
    private final TravelRouteTagRepository tagRepository;
    private final UserRepository userRepository;
    private final RoutePlaceRepository routePlaceRepository;

    public List<TravelRoute> getAllRoutes() {
        return travelRouteRepository.findAll(); // 추후 정렬/필터 추가 가능
    }

    public Page<TravelRouteListItemDTO> getPagedRouteList(int page, int size, String keyword) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<TravelRoute> routePage;

        if (keyword != null && !keyword.trim().isEmpty()) {
            routePage = travelRouteRepository.searchByKeyword(keyword, pageable); // ✅ 검색 쿼리
        } else {
            routePage = travelRouteRepository.findAll(pageable);
        }

        return routePage.map(route -> TravelRouteListItemDTO.builder()
                        .routeId(route.getRouteId())
                        .title(route.getTitle())
                        .placeCount(route.getPlaces().size())
                        .views(route.getViews())
                        .userId(route.getUser().getId())
                        .userName(route.getUser().getNickname())
                        .tagNames(route.getTags().stream()
                                        .map(tagging -> tagging.getTag().getTagName())
                                        .collect(Collectors.toList()))
                        .build());
    }

    public void createTravelRoute(CreateTravelRouteDTO dto) {

        // 🔹 유저 조회
        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 사용자 ID"));

        TravelRoute route = TravelRoute.builder()
                .title(dto.getTitle())
                .description(dto.getDescription())
                .user(User.builder().id(dto.getUserId()).build())
                .totalDistance(dto.getTotalDistance())
                .totalTime(dto.getTotalTime())
                .views(dto.getViews())
                .build();

        for (RoutePlaceDTO p : dto.getPlaces()) {
            RoutePlace place = RoutePlace.builder()
                    .placeName(p.getPlaceName())
                    .address(p.getAddress())
                    .description(p.getDescription())
                    .latitude(p.getLatitude())
                    .longitude(p.getLongitude())
                    .visitOrder(p.getVisitOrder())
                    .apiPlaceId(p.getApiPlaceId())
                    .build();

            route.addPlace(place);
        }

        // 태그 저장
        for (String tagName : dto.getTags()) {
            TravelRouteTag tag = tagRepository.findByTagName(tagName)
                    .orElseGet(() -> tagRepository.save(new TravelRouteTag(tagName)));

            TravelRouteTagging tagging = TravelRouteTagging.builder()
                    .travelRoute(route)
                    .tag(tag)
                    .build();

            route.addTagging(tagging); // 편의 메서드로 관리
        }

        travelRouteRepository.save(route);
    }

    /**
     * 장소의 Google 사진 URL 리스트 반환
     */
    public List<String> getPhotoUrls(String placeId) {
        return googlePlaceService.getPhotoUrlsByPlaceId(placeId);
    }

    /**
     * 정적 태그 리스트 반환
     */
    public List<String> getTagList() {
        return List.of("힐링", "맛집", "커플", "자연", "역사", "아이와 함께", "SNS 핫플", "걷기 좋은", "드라이브");
    }

    /**
     * (예시) 상세 조회
     */
    public TravelRouteDTO getRouteDetail(Long id) {
        TravelRoute route = travelRouteRepository.findByIdWithPlaces(id)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 경로"));

        List<RoutePlaceDTO> places = route.getPlaces().stream()
                .sorted(Comparator.comparing(RoutePlace::getVisitOrder))
                .map(place -> {
                    List<String> photos;
                    try {
                        photos = googlePlaceService.getPhotoUrlsByPlaceId(place.getApiPlaceId());
                    } catch (Exception e) {
                        photos = List.of(); // 또는 Collections.emptyList()
                        log.warn("사진 조회 실패: " + place.getPlaceName());
                    }
                    return RoutePlaceDTO.fromEntity(place, photos);
                })
                .collect(Collectors.toList());

        return TravelRouteDTO.fromEntity(route, places);
    }

    /**
     * routeId로 첫 번째 장소를 조회하여 해당 장소의 썸네일 이미지 URL 생성
     */
    public String getThumbnailUrlByRouteId(Long routeId) {

        return routePlaceRepository.findTopByTravelRoute_RouteIdOrderByVisitOrderAsc(routeId)
                .map(place -> {
                    String apiPlaceId = place.getApiPlaceId();
                    if (apiPlaceId == null || apiPlaceId.isBlank()) {
                        return "/images/default-thumbnail.jpg";
                    }
                    return "/api/place/photos/first?placeId=" + apiPlaceId;
                })
                .orElseGet(() -> {
                    return "/images/default-thumbnail.jpg";
                });
    }

    @Transactional
    public void increaseViews(Long routeId) {
        TravelRoute route = travelRouteRepository.findById(routeId)
                .orElseThrow(() -> new IllegalArgumentException("해당 여행 경로가 없습니다: " + routeId));
        route.increaseViews(); // 조회수 증가
    }

    @Transactional
    public void updateRoute(Long routeId, UpdateTravelRouteDTO dto) {
        TravelRoute route = travelRouteRepository.findById(routeId)
                .orElseThrow(() -> new RuntimeException("Route not found"));

        // 기본 정보 수정
        route.update(dto.getTitle(), dto.getDescription(), dto.getTotalDistance(), dto.getTotalTime());

        // 🔥 장소 초기화 후 재등록
        route.clearPlaces();
        for (RoutePlaceDTO p : dto.getPlaces()) {
            RoutePlace place = RoutePlace.builder()
                    .placeName(p.getPlaceName())
                    .address(p.getAddress())
                    .description(p.getDescription())
                    .latitude(p.getLatitude())
                    .longitude(p.getLongitude())
                    .visitOrder(p.getVisitOrder())
                    .apiPlaceId(p.getApiPlaceId())
                    .build();
            route.addPlace(place);
        }

        // 🔥 태그 초기화 후 재등록
        route.clearTags();
        for (String tagName : dto.getTags()) {
            TravelRouteTag tag = tagRepository.findByTagName(tagName)
                    .orElseGet(() -> tagRepository.save(new TravelRouteTag(tagName)));

            TravelRouteTagging tagging = TravelRouteTagging.builder()
                    .travelRoute(route)
                    .tag(tag)
                    .build();

            route.addTagging(tagging);
        }

    }

    @Transactional
    public void deleteRoute(Long routeId) {
        travelRouteRepository.deleteById(routeId);
    }



}
