package com.test.foodtrip.domain.post.controller;

import com.test.foodtrip.domain.post.dto.PageRequestDTO;
import com.test.foodtrip.domain.post.dto.PageResultDTO;
import com.test.foodtrip.domain.post.dto.PostDTO;
import com.test.foodtrip.domain.post.entity.Post;
import com.test.foodtrip.domain.post.service.PostService;
import com.test.foodtrip.domain.user.entity.User;
import com.test.foodtrip.domain.user.repository.UserRepository;
import com.test.foodtrip.domain.user.service.UserService;
import jakarta.servlet.http.HttpSession;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
@Log4j2
public class PostController {

    private final PostService postService;
    private final UserRepository userRepository;

    public PostController(PostService postService, UserRepository userRepository) {
        this.postService = postService;
        this.userRepository = userRepository;
    }

    @GetMapping("/")
    public String index() {
        return "redirect:/post";
    }

    @GetMapping("/post")
    public String list(PageRequestDTO pageRequestDTO, Model model) {
        log.info("PostController list() - pageRequestDTO: " + pageRequestDTO);

        PageResultDTO<PostDTO, Post> result = postService.getList(pageRequestDTO);
        model.addAttribute("result", result);

        return "post/post";
    }

    @GetMapping("/post/{id}")
    public String detail(@PathVariable("id") Long id,
                         @ModelAttribute("requestDTO") PageRequestDTO requestDTO,
                         Model model) {
        PostDTO dto = postService.read(id);
        String placeId = dto.getPlaceId();
        model.addAttribute("dto", dto);

        // 게시글 작성자 정보 조회하여 모델에 추가
        if (dto.getUserId() != null) {
            Optional<User> authorOpt = userRepository.findById(dto.getUserId());
            if (authorOpt.isPresent()) {
                User author = authorOpt.get();
                model.addAttribute("author", author);
            }
        }


        model.addAttribute("placeId", placeId);
        return "post/detail-post";
    }

    @GetMapping("/post/create")
    public String create(HttpSession session, Model model) {
        // 로그인 체크
        if (!isLoggedIn(session)) {
            return "redirect:/login?error=login_required";
        }
        return "post/create-post";
    }

    @PostMapping("/post/create")
    public String create(@ModelAttribute PostDTO dto,
                         @RequestParam(value = "tags", required = false) List<String> tags,
                         @RequestParam(value = "imageFiles", required = false) List<MultipartFile> imageFiles,
                         @RequestParam(value = "placeId", required = false) String placeId,
                         @RequestParam(value = "placeName", required = false) String placeName,
                         @RequestParam(value = "placeAddress", required = false) String placeAddress,
                         HttpSession session,
                         RedirectAttributes redirectAttributes) {
        log.info("PostController create() - dto: {}", dto);
        log.info("PostController create() - tags: {}", tags);
        log.info("PostController create() - imageFiles: {}개", imageFiles != null ? imageFiles.size() : 0);
        log.info("PostController create() - placeId: {}", placeId);

        // 로그인 체크
        if (!isLoggedIn(session)) {
            return "redirect:/login?error=login_required";
        }

        try {
            // 유저 정보 세션에서 가져와서 DTO에 설정
            Long userId = (Long) session.getAttribute("user_id");
            dto.setUserId(userId);

            // 태그 정보 설정
            dto.setTags(tags);

            // 위치 정보 설정
            dto.setPlaceId(placeId);
            dto.setPlaceName(placeName);
            dto.setPlaceAddress(placeAddress);

            // 이미지 배열로 변환
            MultipartFile[] imagesArray = imageFiles != null ? imageFiles.toArray(new MultipartFile[0]) : new MultipartFile[0];

            // 서비스 호출
            Long pno = postService.create(dto, imagesArray);

            redirectAttributes.addFlashAttribute("msg", pno);
            return "redirect:/post";
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/login";
        } catch (Exception e) {
            log.error("게시글 생성 중 오류 발생", e);
            redirectAttributes.addFlashAttribute("error", "게시글 생성 중 오류가 발생했습니다.");
            return "redirect:/post/create";
        }
    }


    // 이미지 파일 유효성 검사 메서드
    private boolean isValidImageFile(MultipartFile file) {
        if (file.isEmpty()) {
            return false;
        }

        // 파일 크기 검사 (5MB)
        if (file.getSize() > 5 * 1024 * 1024) {
            return false;
        }

        // 파일 타입 검사
        String contentType = file.getContentType();
        return contentType != null &&
                (contentType.equals("image/jpeg") ||
                        contentType.equals("image/png") ||
                        contentType.equals("image/gif"));
    }

    @GetMapping("/post/modify/{id}")
    public String modify(@PathVariable("id") Long id,
                         @ModelAttribute("requestDTO") PageRequestDTO requestDTO,
                         HttpSession session,
                         Model model) {
        // 로그인 체크
        if (!isLoggedIn(session)) {
            return "redirect:/login?error=login_required";
        }

        PostDTO dto = postService.read(id);
        model.addAttribute("dto", dto);
        return "post/modify-post";
    }

    @PostMapping("/post/modify")
    public String modify(PostDTO dto,
                         @RequestParam(value = "tags", required = false) List<String> tags,
                         @ModelAttribute("requestDTO") PageRequestDTO requestDTO,
                         HttpSession session,
                         RedirectAttributes redirectAttributes) {
        log.info("PostController modify() - dto: " + dto);
        log.info("PostController modify() - tags: " + tags);

        // 로그인 체크
        if (!isLoggedIn(session)) {
            return "redirect:/login?error=login_required";
        }

        try {
            // 태그 정보를 DTO에 설정
            dto.setTags(tags);

            postService.modify(dto);

            redirectAttributes.addAttribute("page", requestDTO.getPage());
            redirectAttributes.addAttribute("type", requestDTO.getType());
            redirectAttributes.addAttribute("keyword", requestDTO.getKeyword());

            return "redirect:/post/" + dto.getId();
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/post/" + dto.getId();
        }
    }

    @PostMapping("/post/remove")
    public String remove(Long id,
                         @ModelAttribute("requestDTO") PageRequestDTO requestDTO,
                         HttpSession session,
                         RedirectAttributes redirectAttributes) {
        log.info("PostController remove() - id: " + id);

        // 로그인 체크
        if (!isLoggedIn(session)) {
            return "redirect:/login?error=login_required";
        }

        try {
            postService.remove(id);

            redirectAttributes.addAttribute("page", 1);
            redirectAttributes.addAttribute("type", requestDTO.getType());
            redirectAttributes.addAttribute("keyword", requestDTO.getKeyword());

            return "redirect:/post";
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/post";
        }
    }

    // 로그인 상태를 확인하는 헬퍼 메서드
    private boolean isLoggedIn(HttpSession session) {
        return session != null && session.getAttribute("user_id") != null;
    }


}