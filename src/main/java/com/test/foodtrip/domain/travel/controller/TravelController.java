package com.test.foodtrip.domain.travel.controller;

import com.test.foodtrip.domain.travel.dto.TravelRouteDTO;
import com.test.foodtrip.domain.travel.dto.TravelRouteListItemDTO;
import com.test.foodtrip.domain.travel.service.TravelRouteService;
import com.test.foodtrip.domain.user.dto.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "여행 코스 HTML View", description = "Thymeleaf 기반 여행 코스 관련 HTML 페이지 반환 컨트롤러")
@Controller
@RequiredArgsConstructor
@RequestMapping("/travel")
public class TravelController {

    private final TravelRouteService travelRouteService;

    @Operation(summary = "여행 코스 리스트 페이지 (HTML View)",
            description = "페이징 및 검색어로 필터링된 여행 코스 목록을 포함한 HTML 페이지를 반환합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공적으로 목록 페이지 반환"),
            @ApiResponse(responseCode = "500", description = "서버 오류 발생 시")
    })
    @GetMapping
    public String getCourseList(
            @Parameter(hidden = true) @AuthenticationPrincipal UserPrincipal loginUser,
            @Parameter(description = "페이지 번호 (0부터 시작)", required = false, example = "0") @RequestParam(name= "page", defaultValue = "0") int page,
            @Parameter(description = "검색어 (선택)", required = false, example = "제주") @RequestParam(name= "keyword", required = false) String keyword,
            Model model) {

        Long loginUserId = loginUser != null ? loginUser.getUserId() : null;

        Page<TravelRouteListItemDTO> routes =
                travelRouteService.getPagedRouteListWithBookmarks(loginUserId, page, 8, keyword);

        model.addAttribute("routes", routes);
        model.addAttribute("currentPage", routes.getNumber());
        model.addAttribute("totalPages", routes.getTotalPages());
        model.addAttribute("keyword", keyword);
        model.addAttribute("loginUserId", loginUserId);

        return "travel/course-list";
    }

    @Operation(summary = "여행 코스 상세 페이지", description = "선택된 여행 코스의 상세 정보를 표시하는 HTML 페이지를 반환합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "정상적으로 상세 정보 조회됨"),
            @ApiResponse(responseCode = "404",
                    description = "해당 경로 ID의 정보가 존재하지 않음",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(example = "{\"error\": \"존재하지 않는 코스입니다.\"}")
                    )),
    })
    @GetMapping("/{routeId}")
    public String courseDetail(
            @Parameter(description = "여행 코스 ID") @PathVariable("routeId") Long routeId,
            Model model,
            @Parameter(hidden = true) @AuthenticationPrincipal UserPrincipal loginUser) {
        TravelRouteDTO route = travelRouteService.getRouteDetail(routeId);

        boolean editable = false;
        if (loginUser != null && loginUser.getUserId() != null) {
            Long loginUserId = loginUser.getUserId();
            editable = loginUserId.equals(route.getUserId());
        }

        // 거리: 소수점 1자리 km 단위
        String formattedDistance = String.format("%.1f", route.getTotalDistance() / 1000);

        // 시간: 분 단위 → 시간 + 분 형태로
        int totalSeconds = route.getTotalTime(); // totalTime이 초 단위라고 가정해야 정확
        int hours = totalSeconds / 3600;
        int minutes = (totalSeconds % 3600) / 60;

        String formattedTime = hours > 0
                ? hours + "시간 " + minutes + "분"
                : minutes + "분";

        model.addAttribute("editable", editable);
        model.addAttribute("route", route);
        model.addAttribute("formattedDistance", formattedDistance);
        model.addAttribute("formattedTime", formattedTime);

        return "travel/course-detail";
    }

    @Operation(summary = "여행 코스 생성 페이지", description = "새 여행 코스를 등록하는 HTML 페이지를 반환합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "정상 접근"),
            @ApiResponse(responseCode = "401", description = "비로그인 사용자의 접근 시 오류") // 선택
    })
    @GetMapping("/add")
    public String courseCreatePage(
            @Parameter(hidden = true) @AuthenticationPrincipal UserPrincipal loginUser, Model model) {
        model.addAttribute("loginUserId", loginUser.getUserId());
        return "travel/course-create";
    }

    @Operation(
            summary = "여행 경로 수정 페이지 진입",
            description = "로그인한 사용자 본인의 게시글일 경우 수정 페이지로 이동합니다. 그렇지 않으면 /travel로 리디렉션됩니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수정 페이지 진입 성공"),
            @ApiResponse(responseCode = "302",
                    description = "본인 게시물이 아닌 경우 리디렉션 (Swagger UI에서는 처리되지 않음)"),
    })
    @GetMapping("/{routeId}/edit")
    public String editPage(
            @Parameter(hidden = true) @AuthenticationPrincipal UserPrincipal loginUser,
            @Parameter(description = "여행 경로 ID", example = "5") @PathVariable("routeId") Long routeId, Model model) {
        TravelRouteDTO dto = travelRouteService.getRouteDetail(routeId);

        // 로그인 여부 또는 본인 게시물인지 확인
        if (loginUser == null || !loginUser.getUserId().equals(dto.getUserId())) {
            return "redirect:/travel";
        }

        model.addAttribute("loginUserId", loginUser.getUserId());
        model.addAttribute("route", dto);
        return "travel/course-update";
    }

}
