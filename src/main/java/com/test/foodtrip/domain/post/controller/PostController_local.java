package com.test.foodtrip.domain.post.controller;

import com.test.foodtrip.domain.post.dto.PageRequestDTO;
import com.test.foodtrip.domain.post.dto.PageResultDTO;
import com.test.foodtrip.domain.post.dto.PostDTO;
import com.test.foodtrip.domain.post.entity.Post;
import com.test.foodtrip.domain.post.service.PostService;
import com.test.foodtrip.domain.user.entity.User;
import com.test.foodtrip.domain.user.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpSession;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Tag(name = "게시글 HTML View (개발용)", description = "Thymeleaf 기반 게시글 관련 HTML 페이지 반환 컨트롤러 (로컬 개발용)")
@Controller
@Log4j2
public class PostController_local {

    @Value("${google.maps.key}")
    private String apiKey;


    private final PostService postService;
    private final UserRepository userRepository;

    public PostController_local(@Qualifier("postServiceImpl_local") PostService postService, UserRepository userRepository) {
        this.postService = postService;
        this.userRepository = userRepository;
    }

    @Operation(summary = "메인 페이지", description = "메인 페이지 접속 시 게시글 목록으로 리다이렉트합니다.")
    @GetMapping("/")
    public String index() {
        return "redirect:/post";
    }

    @Operation(summary = "게시글 목록 페이지", description = "페이징 처리된 게시글 목록을 보여주는 HTML 페이지를 반환합니다.")
    @GetMapping("/post")
    public String list(
            @Parameter(description = "페이지 요청 정보 (페이지 번호, 검색 조건 등)") PageRequestDTO pageRequestDTO,
            Model model) {
        log.info("PostController list() - pageRequestDTO: " + pageRequestDTO);

        PageResultDTO<PostDTO, Post> result = postService.getList(pageRequestDTO);
        model.addAttribute("result", result);
        model.addAttribute("apiKey", apiKey);

        return "post/post";
    }

    @Operation(summary = "게시글 상세 페이지", description = "특정 게시글의 상세 내용을 보여주는 HTML 페이지를 반환합니다.")
    @GetMapping("/post/{id}")
    public String detail(
            @Parameter(description = "게시글 ID") @PathVariable("id") Long id,
            @Parameter(description = "페이지 요청 정보") @ModelAttribute("requestDTO") PageRequestDTO requestDTO,
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
        model.addAttribute("apiKey", apiKey);
        return "post/detail-post";
    }

    @Operation(summary = "게시글 작성 페이지", description = "새 게시글을 작성하는 HTML 페이지를 반환합니다. 로그인이 필요합니다.")
    @GetMapping("/post/create")
    public String create(HttpSession session, Model model) {
        model.addAttribute("apiKey", apiKey);
        // 로그인 체크
        if (!isLoggedIn(session)) {
            return "redirect:/login?error=login_required";
        }
        return "post/create-post";
    }

    @Operation(summary = "게시글 저장", description = "작성한 게시글을 저장합니다. 로그인이 필요하며 이미지 파일을 첨부할 수 있습니다.")
    @PostMapping("/post/create")
    public String create(
            @Parameter(description = "게시글 정보") @ModelAttribute PostDTO dto,
            @Parameter(description = "태그 목록") @RequestParam(value = "tags", required = false) List<String> tags,
            @Parameter(description = "이미지 파일 목록") @RequestParam(value = "imageFiles", required = false) List<MultipartFile> imageFiles,
            @Parameter(description = "장소 ID") @RequestParam(value = "placeId", required = false) String placeId,
            @Parameter(description = "장소 이름") @RequestParam(value = "placeName", required = false) String placeName,
            @Parameter(description = "장소 주소") @RequestParam(value = "placeAddress", required = false) String placeAddress,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        log.info("PostController create() - dto: {}", dto);
        log.info("PostController create() - tags: {}", tags);
        log.info("PostController create() - imageFiles: {}개", imageFiles != null ? imageFiles.size() : 0);
        log.info("PostController create() - placeId: {}", placeId);

        // 🔍 추가 디버깅: 각 이미지 파일 정보 출력
        System.out.println("=== Controller 이미지 파일 디버깅 ===");
        System.out.println("받은 이미지 파일 수: " + (imageFiles != null ? imageFiles.size() : "null"));

        if (imageFiles != null) {
            for (int i = 0; i < imageFiles.size(); i++) {
                MultipartFile file = imageFiles.get(i);
                System.out.println("파일 " + i + ":");
                System.out.println("  - 파일명: " + file.getOriginalFilename());
                System.out.println("  - 크기: " + file.getSize() + " bytes");
                System.out.println("  - Content-Type: " + file.getContentType());
                System.out.println("  - 비어있음: " + file.isEmpty());
            }
        }

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

            // 🔍 배열 변환 전 추가 로그
            System.out.println("배열 변환 전 리스트 크기: " + (imageFiles != null ? imageFiles.size() : 0));

            // 이미지 배열로 변환
            MultipartFile[] imagesArray = imageFiles != null ? imageFiles.toArray(new MultipartFile[0]) : new MultipartFile[0];

            System.out.println("배열 변환 후 크기: " + imagesArray.length);

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

    @Operation(summary = "게시글 수정 페이지", description = "특정 게시글의 수정 페이지를 로드합니다. 본인이 작성한 게시글만 수정 가능합니다.")
    @GetMapping("/post/modify/{id}")
    public String modify(
            @Parameter(description = "수정할 게시글 ID") @PathVariable("id") Long id,
            @Parameter(description = "페이지 요청 정보") @ModelAttribute("requestDTO") PageRequestDTO requestDTO,
            HttpSession session,
            Model model) {

        // 로그인 체크
        if (!isLoggedIn(session)) {
            return "redirect:/login?error=login_required";
        }

        try {
            System.out.println("=== 게시글 수정 페이지 로드 시작 ===");
            System.out.println("게시글 ID: " + id);

            PostDTO dto = postService.read(id);

            System.out.println("=== PostDTO 확인 ===");
            if (dto != null) {
                System.out.println("제목: " + dto.getTitle());
                System.out.println("내용: " + dto.getContent());
                System.out.println("태그 수: " + (dto.getTags() != null ? dto.getTags().size() : 0));
                System.out.println("이미지 수: " + (dto.getImageUrls() != null ? dto.getImageUrls().size() : 0));
                System.out.println("위치명: " + dto.getPlaceName());

                if (dto.getTags() != null) {
                    System.out.println("태그 목록: " + dto.getTags());
                }
                if (dto.getImageUrls() != null) {
                    System.out.println("이미지 URL 목록: " + dto.getImageUrls());
                }
            } else {
                System.out.println("❌ DTO가 null입니다!");
                return "redirect:/post";
            }

            model.addAttribute("dto", dto);
            model.addAttribute("requestDTO", requestDTO);
            model.addAttribute("apiKey", apiKey);

            System.out.println("=== Model에 데이터 추가 완료 ===");
            System.out.println("템플릿 반환: post/modify-post");

            return "post/modify-post";
        } catch (Exception e) {
            System.err.println("❌ 게시글 수정 페이지 로드 중 오류: " + e.getMessage());
            e.printStackTrace();
            return "redirect:/post";
        }
    }

    @Operation(summary = "게시글 수정 처리", description = "수정된 게시글 정보를 저장합니다.")
    @PostMapping("/post/modify")
    public String modify(
            @Parameter(description = "수정할 게시글 정보") PostDTO dto,
            @Parameter(description = "태그 목록") @RequestParam(value = "tags", required = false) List<String> tags,
            @Parameter(description = "새로 업로드할 이미지 파일 목록") @RequestParam(value = "imageFiles", required = false) List<MultipartFile> imageFiles,
            @Parameter(description = "삭제할 이미지 인덱스 목록") @RequestParam(value = "deleteImageIndexes", required = false) List<Integer> deleteImageIndexes,
            @Parameter(description = "페이지 요청 정보") @ModelAttribute("requestDTO") PageRequestDTO requestDTO,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        log.info("PostController modify() - dto: " + dto);
        log.info("PostController modify() - tags: " + tags);
        log.info("PostController modify() - 새 이미지 파일 수: " + (imageFiles != null ? imageFiles.size() : 0));
        log.info("PostController modify() - 삭제할 이미지 인덱스: " + deleteImageIndexes);

        // 로그인 체크
        if (!isLoggedIn(session)) {
            return "redirect:/login?error=login_required";
        }

        try {
            // 태그 정보를 DTO에 설정
            dto.setTags(tags);

            // 이미지 배열로 변환
            MultipartFile[] imagesArray = imageFiles != null ?
                    imageFiles.toArray(new MultipartFile[0]) : new MultipartFile[0];

            // 수정 서비스 호출 (이미지도 함께 처리)
            postService.modify(dto, imagesArray, deleteImageIndexes);

            redirectAttributes.addAttribute("page", requestDTO.getPage());
            redirectAttributes.addAttribute("type", requestDTO.getType());
            redirectAttributes.addAttribute("keyword", requestDTO.getKeyword());

            return "redirect:/post/" + dto.getId();
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/post/" + dto.getId();
        } catch (Exception e) {
            log.error("게시글 수정 중 오류 발생", e);
            redirectAttributes.addFlashAttribute("error", "게시글 수정 중 오류가 발생했습니다.");
            return "redirect:/post/modify/" + dto.getId();
        }
    }

    @Operation(summary = "게시글 삭제", description = "특정 게시글을 삭제합니다. 본인이 작성한 게시글만 삭제 가능합니다.")
    @PostMapping("/post/remove")
    public String remove(
            @Parameter(description = "삭제할 게시글 ID") @RequestParam("id") Long id,
            @Parameter(description = "페이지 요청 정보") @ModelAttribute("requestDTO") PageRequestDTO requestDTO,
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