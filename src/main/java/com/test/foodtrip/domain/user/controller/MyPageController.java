// 파일 경로: src/main/java/com/test/foodtrip/domain/user/controller/MyPageController.java

package com.test.foodtrip.domain.user.controller;

import com.test.foodtrip.domain.user.dto.MyPageDTO;
import com.test.foodtrip.domain.user.dto.UsersInfoDTO;
import com.test.foodtrip.domain.user.entity.User;
import com.test.foodtrip.domain.user.service.FileStorageService;
import com.test.foodtrip.domain.user.service.FollowService;
import com.test.foodtrip.domain.user.service.MyPageService;
import com.test.foodtrip.domain.user.service.VisitorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class MyPageController {



    private final MyPageService myPageService;

    private final FollowService followService;

    private final VisitorService visitorService;

    private final FileStorageService fileStorageService;

    /**
     * 마이페이지 조회
     * 1. HttpSession에서 "user_id" 꺼내기
     * 2. 서비스에 userId 전달해서 MyPageDTO 받기
     * 3. Model에 담아서 mypage.html에 렌더링
     */
   /* @GetMapping("/mypage")
    public String showMyPage(HttpSession session, Model model) {
        // 세션에서 user_id 가져오기
        Long userId = (Long) session.getAttribute("user_id");
        if (userId == null) {
            // 세션에 user_id가 없으면 로그인 페이지로 리다이렉트
            return "redirect:/login";
        }

        // 서비스 호출 → MyPageDTO 생성
        MyPageDTO dto = myPageService.getMyPage(userId);

        // 뷰에 전달
        model.addAttribute("user", dto);
        model.addAttribute("sessionUserId", userId);
        model.addAttribute("pageOwnerId", dto.getId());
        return "mypage";
    }*/

    /**
     * 다른 사람 마이페이지 조회
     */
    // 이 하나의 메서드가 /mypage 와 /users/{pageOwnerId} 둘 다 처리
    @GetMapping({ "/mypage", "/users/{pageOwnerId}" })
    public String viewUserPage(
            // pageOwnerId가 없으면(null) 내 페이지 요청으로 간주
            @PathVariable(value = "pageOwnerId", required = false) Long pageOwnerId,
            HttpSession session,
            Model model
    ) {
        // 1) 세션에서 로그인한 유저 ID
        Long sessionUserId = (Long) session.getAttribute("user_id");
        if (pageOwnerId == null) {
            // /mypage 로 왔을 때, pageOwnerId를 내 ID로 설정
            if (sessionUserId == null) {
                return "redirect:/login";
            }
            pageOwnerId = sessionUserId;
        }

        // ★ 방문 기록 저장
        visitorService.recordVisit(pageOwnerId, sessionUserId);

        // 2) MyPageDTO 조회
        MyPageDTO dto = myPageService.getMyPage(pageOwnerId);
        model.addAttribute("user", dto);
        model.addAttribute("pageOwnerId", pageOwnerId);
        model.addAttribute("sessionUserId", sessionUserId);

        // 3) 주인 여부
        boolean isOwner = sessionUserId != null && sessionUserId.equals(pageOwnerId);
        model.addAttribute("isOwner", isOwner);

        // 4) 주인이 아니면 팔로우 여부 체크
        if (!isOwner && sessionUserId != null) {
            boolean isFollowing = followService.isFollowing(sessionUserId, pageOwnerId);
            model.addAttribute("isFollowing", isFollowing);
        }

        return "mypage";
    }

    /**
     * 프로필 수정 폼 보여주기
     */
    @GetMapping("/mypage/edit")
    public String showEditForm(HttpSession session, Model model) {
        Long userId = (Long) session.getAttribute("user_id");
        if (userId == null) {
            return "redirect:/login";
        }

        MyPageDTO dto = myPageService.getMyPage(userId);
        model.addAttribute("user", dto);
        return "mypage";  // 모달이 같은 페이지에 있으면 기존 템플릿 재사용
    }



    /**
     * 프로필 수정 처리 (AJAX)
     */
    @PostMapping("/mypage/edit")
    @ResponseBody
    public ResponseEntity<Void> updateProfileAjax(
            HttpSession session,
            @ModelAttribute("user") MyPageDTO formDto
    ) {
        Long userId = (Long) session.getAttribute("user_id");
        if (userId == null) {
            // 인증이 안 된 경우 401 반환
            return ResponseEntity.status(401).build();
        }

        // 1) 파일 저장
        MultipartFile file = formDto.getProfileImageFile();
        if (file != null && !file.isEmpty()) {
            String filename = fileStorageService.storeFile(file);
            formDto.setProfileImage(filename);
        } else if (formDto.isRemoveProfileImage()) {
            // 삭제 플래그가 켜져 있으면 프로필 이미지 비움
            formDto.setProfileImage(null);
        }

        myPageService.updateProfile(userId, formDto);
        // 변경 완료, HTTP 200 OK 만 내려줌
        return ResponseEntity.ok().build();
    }



    /** 나의 소개 생성 */
    @PostMapping("/mypage/intro")
    @ResponseBody
    public ResponseEntity<UsersInfoDTO> createIntro(
            HttpSession session,
            @RequestBody UsersInfoDTO introDto
    ) {
        Long sessionUserId = (Long) session.getAttribute("user_id");
        if (sessionUserId == null) {
            return ResponseEntity.status(401).build();
        }
        UsersInfoDTO saved = myPageService.createIntro(sessionUserId, introDto);
        return ResponseEntity.ok(saved);
    }

    /** 나의 소개 수정 */
    @PutMapping("/mypage/intro")
    @ResponseBody
    public ResponseEntity<UsersInfoDTO> updateIntro(
            HttpSession session,
            @RequestBody UsersInfoDTO introDto
    ) {
        Long sessionUserId = (Long) session.getAttribute("user_id");
        if (sessionUserId == null) {
            return ResponseEntity.status(401).build();
        }
        UsersInfoDTO updated = myPageService.updateIntro(sessionUserId, introDto);
        return ResponseEntity.ok(updated);
    }

    /** 나의 소개 삭제 */
    @DeleteMapping("/mypage/intro")
    @ResponseBody
    public ResponseEntity<Void> deleteIntro(HttpSession session) {
        Long sessionUserId = (Long) session.getAttribute("user_id");
        if (sessionUserId == null) {
            return ResponseEntity.status(401).build();
        }
        myPageService.deleteIntro(sessionUserId);
        return ResponseEntity.ok().build();
    }

    /**
     * 팔로우 요청 처리 (AJAX)
     */
    @PostMapping("/follow/{followeeId}")
    @ResponseBody
    public ResponseEntity<Map<String, Integer>> followUser(
            @PathVariable Long followeeId,
            HttpSession session
    ) {
        Long followerId = (Long) session.getAttribute("user_id");
        if (followerId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // 실제 팔로우 로직 호출
        followService.follow(followerId, followeeId);

        // 변경된 팔로워 수 반환
        int newCount = followService.countFollowers(followeeId);
        return ResponseEntity.ok(Map.of("followerCount", newCount));
    }

    /**
     * 언팔로우 요청 처리 (AJAX)
     */
    @DeleteMapping("/follow/{followeeId}")
    @ResponseBody
    public ResponseEntity<Map<String, Integer>> unfollowUser(
            @PathVariable Long followeeId,
            HttpSession session
    ) {
        Long followerId = (Long) session.getAttribute("user_id");
        if (followerId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // 실제 언팔로우 로직 호출
        followService.unfollow(followerId, followeeId);

        // 변경된 팔로워 수 반환
        int newCount = followService.countFollowers(followeeId);
        return ResponseEntity.ok(Map.of("followerCount", newCount));
    }

    // 팔로워 리스트 JSON 반환
    @GetMapping("/users/{userId}/followers")
    @ResponseBody
    public ResponseEntity<List<Map<String, Object>>> getFollowers(
            @PathVariable Long userId) {

        List<User> list = followService.getFollowers(userId);

        // Map.of 대신 HashMap 사용
        List<Map<String, Object>> body = list.stream()
                .map(u -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("id",       u.getId());
                    m.put("nickname", u.getNickname());
                    return m;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(body);
    }

    // 팔로잉 리스트 JSON 반환
    @GetMapping("/users/{userId}/followings")
    @ResponseBody
    public ResponseEntity<List<Map<String, Object>>> getFollowings(
            @PathVariable Long userId,
            HttpSession session) {

        List<User> list = followService.getFollowings(userId);
        Long sessionUserId = (Long) session.getAttribute("user_id");
        boolean isOwner = sessionUserId != null && sessionUserId.equals(userId);

        List<Map<String,Object>> body = list.stream()
                .map(u -> {
                    Map<String,Object> m = new HashMap<>();
                    m.put("id", u.getId());
                    m.put("nickname", u.getNickname());
                    // 본인 페이지일 때만 “언팔로우” 버튼 노출용 flag
                    m.put("canUnfollow", isOwner);
                    return m;
                })
                .collect(Collectors.toList());
        return ResponseEntity.ok(body);
    }

}

