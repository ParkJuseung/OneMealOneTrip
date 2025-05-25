package com.test.foodtrip.domain.travel.controller;

import com.test.foodtrip.domain.travel.dto.TravelRouteDTO;
import com.test.foodtrip.domain.travel.dto.TravelRouteListItemDTO;
import com.test.foodtrip.domain.travel.service.TravelRouteService;
import com.test.foodtrip.domain.user.dto.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class TravelController {

    private final TravelRouteService travelRouteService;

    @GetMapping("/travel")
    public String getCourseList(@RequestParam(defaultValue = "0") int page,
                                @RequestParam(required = false) String keyword,
                                Model model) {

        Page<TravelRouteListItemDTO> routes = travelRouteService.getPagedRouteList(page, 8, keyword);

        model.addAttribute("routes", routes);
        model.addAttribute("currentPage", routes.getNumber());
        model.addAttribute("totalPages", routes.getTotalPages());
        model.addAttribute("keyword", keyword);

        return "travel/course-list";
    }

    @GetMapping("/travel/{routeId}")
    public String courseDetail(@PathVariable("routeId") Long routeId, Model model,
                               @AuthenticationPrincipal UserPrincipal loginUser) {
        TravelRouteDTO route = travelRouteService.getRouteDetail(routeId);

        boolean editable = false;
        if (loginUser != null && loginUser.getUserId() != null) {
            Long loginUserId = loginUser.getUserId();
            editable = loginUserId.equals(route.getUserId());
        }

        System.out.println("로그인 유저: " + (loginUser != null ? loginUser.getUserId() : "null"));

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


    @GetMapping("/travel/add")
    public String courseCreatePage(@AuthenticationPrincipal UserPrincipal loginUser, Model model) {
        model.addAttribute("profileImage", loginUser.getProfileImage());
        model.addAttribute("loginUserId", loginUser.getUserId());
        model.addAttribute("userNickname", loginUser.getNickname());
        return "travel/course-create";
    }

    @GetMapping("/travel/{routeId}/edit")
    public String editPage(@AuthenticationPrincipal UserPrincipal loginUser, @PathVariable Long routeId, Model model) {
        TravelRouteDTO dto = travelRouteService.getRouteDetail(routeId);
        model.addAttribute("profileImage", loginUser.getProfileImage());
        model.addAttribute("loginUserId", loginUser.getUserId());
        model.addAttribute("userNickname", loginUser.getNickname());
        model.addAttribute("route", dto);
        return "travel/course-update";
    }

}
