package com.test.foodtrip.domain.post.controller;//package com.test.foodtrip.domain.post.controller;
//
//import com.test.foodtrip.domain.post.dto.PageRequestDTO;
//import com.test.foodtrip.domain.post.dto.PageResultDTO;
//import com.test.foodtrip.domain.post.dto.PostDTO;
//import com.test.foodtrip.domain.post.entity.Post;
//import com.test.foodtrip.domain.post.service.PostService;
//import com.test.foodtrip.domain.user.entity.User;
//import com.test.foodtrip.domain.user.repository.UserRepository;
//import com.test.foodtrip.common.aws.S3Service;
//import jakarta.servlet.http.HttpSession;
//import lombok.extern.log4j.Log4j2;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Controller;
//import org.springframework.ui.Model;
//import org.springframework.web.bind.annotation.*;
//import org.springframework.web.multipart.MultipartFile;
//import org.springframework.web.servlet.mvc.support.RedirectAttributes;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Optional;
//
//@Controller
//@Log4j2
//public class PostController {
//
//    @Value("${google.maps.key}")
//    private String apiKey;
//
//    private final PostService postService;
//    private final UserRepository userRepository;
//    private final S3Service s3Service;
//
//    public PostController(PostService postService, UserRepository userRepository, S3Service s3Service) {
//        this.postService = postService;
//        this.userRepository = userRepository;
//        this.s3Service = s3Service;
//    }
//
//    @GetMapping("/")
//    public String index() {
//        return "redirect:/post";
//    }
//
//    @GetMapping("/post")
//    public String list(PageRequestDTO pageRequestDTO, Model model) {
//        log.info("PostController list() - pageRequestDTO: " + pageRequestDTO);
//
//        PageResultDTO<PostDTO, Post> result = postService.getList(pageRequestDTO);
//        model.addAttribute("result", result);
//        model.addAttribute("apiKey", apiKey);
//
//        return "post/post";
//    }
//
//    @GetMapping("/post/{id}")
//    public String detail(@PathVariable("id") Long id,
//                         @ModelAttribute("requestDTO") PageRequestDTO requestDTO,
//                         Model model) {
//        PostDTO dto = postService.read(id);
//        String placeId = dto.getPlaceId();
//        model.addAttribute("dto", dto);
//
//        // 게시글 작성자 정보 조회하여 모델에 추가
//        if (dto.getUserId() != null) {
//            Optional<User> authorOpt = userRepository.findById(dto.getUserId());
//            if (authorOpt.isPresent()) {
//                User author = authorOpt.get();
//                model.addAttribute("author", author);
//            }
//        }
//
//        model.addAttribute("placeId", placeId);
//        model.addAttribute("apiKey", apiKey);
//        return "post/detail-post";
//    }
//
//    @GetMapping("/post/create")
//    public String create(HttpSession session, Model model) {
//        model.addAttribute("apiKey", apiKey);
//        // 로그인 체크
//        if (!isLoggedIn(session)) {
//            return "redirect:/login?error=login_required";
//        }
//        return "post/create-post";
//    }
//
//    @PostMapping("/post/create")
//    public String create(@ModelAttribute PostDTO dto,
//                         @RequestParam(value = "tags", required = false) List<String> tags,
//                         @RequestParam(value = "imageFiles", required = false) List<MultipartFile> imageFiles,
//                         @RequestParam(value = "placeId", required = false) String placeId,
//                         @RequestParam(value = "placeName", required = false) String placeName,
//                         @RequestParam(value = "placeAddress", required = false) String placeAddress,
//                         HttpSession session,
//                         RedirectAttributes redirectAttributes) {
//        log.info("PostController create() - dto: {}", dto);
//        log.info("PostController create() - tags: {}", tags);
//        log.info("PostController create() - imageFiles: {}개", imageFiles != null ? imageFiles.size() : 0);
//        log.info("PostController create() - placeId: {}", placeId);
//
//        // 로그인 체크
//        if (!isLoggedIn(session)) {
//            return "redirect:/login?error=login_required";
//        }
//
//        try {
//            // 유저 정보 세션에서 가져와서 DTO에 설정
//            Long userId = (Long) session.getAttribute("user_id");
//            dto.setUserId(userId);
//
//            // 태그 정보 설정
//            dto.setTags(tags);
//
//            // 위치 정보 설정
//            dto.setPlaceId(placeId);
//            dto.setPlaceName(placeName);
//            dto.setPlaceAddress(placeAddress);
//
//            // S3에 이미지 업로드 처리
//            List<String> imageUrls = new ArrayList<>();
//            if (imageFiles != null && !imageFiles.isEmpty()) {
//                log.info("=== S3 이미지 업로드 시작 ===");
//
//                for (MultipartFile file : imageFiles) {
//                    if (!file.isEmpty() && isValidImageFile(file)) {
//                        try {
//                            String imageUrl = s3Service.upload(file, "posts");
//                            imageUrls.add(imageUrl);
//                            log.info("S3 업로드 성공: {}", imageUrl);
//                        } catch (Exception e) {
//                            log.error("S3 업로드 실패: {}", e.getMessage());
//                        }
//                    }
//                }
//
//                log.info("총 업로드된 이미지 수: {}", imageUrls.size());
//            }
//
//            // DTO에 S3 URL들 설정
//            dto.setImageUrls(imageUrls);
//
//            // 서비스 호출
//            Long pno = postService.create(dto, new MultipartFile[0]);
//
//            redirectAttributes.addFlashAttribute("msg", pno);
//            return "redirect:/post";
//
//        } catch (IllegalStateException e) {
//            redirectAttributes.addFlashAttribute("error", e.getMessage());
//            return "redirect:/login";
//        } catch (Exception e) {
//            log.error("게시글 생성 중 오류 발생", e);
//            redirectAttributes.addFlashAttribute("error", "게시글 생성 중 오류가 발생했습니다.");
//            return "redirect:/post/create";
//        }
//    }
//
//    // 이미지 파일 유효성 검사 메서드
//    private boolean isValidImageFile(MultipartFile file) {
//        if (file.isEmpty()) {
//            return false;
//        }
//
//        // 파일 크기 검사 (5MB)
//        if (file.getSize() > 5 * 1024 * 1024) {
//            return false;
//        }
//
//        // 파일 타입 검사
//        String contentType = file.getContentType();
//        return contentType != null &&
//                (contentType.equals("image/jpeg") ||
//                        contentType.equals("image/png") ||
//                        contentType.equals("image/gif"));
//    }
//
//    @GetMapping("/post/modify/{id}")
//    public String modify(@PathVariable("id") Long id,
//                         @ModelAttribute("requestDTO") PageRequestDTO requestDTO,
//                         HttpSession session,
//                         Model model) {
//
//        // 로그인 체크
//        if (!isLoggedIn(session)) {
//            return "redirect:/login?error=login_required";
//        }
//
//        try {
//            System.out.println("=== 게시글 수정 페이지 로드 시작 ===");
//            System.out.println("게시글 ID: " + id);
//
//            PostDTO dto = postService.read(id);
//
//            System.out.println("=== PostDTO 확인 ===");
//            if (dto != null) {
//                System.out.println("제목: " + dto.getTitle());
//                System.out.println("내용: " + dto.getContent());
//                System.out.println("태그 수: " + (dto.getTags() != null ? dto.getTags().size() : 0));
//                System.out.println("이미지 수: " + (dto.getImageUrls() != null ? dto.getImageUrls().size() : 0));
//                System.out.println("위치명: " + dto.getPlaceName());
//
//                if (dto.getTags() != null) {
//                    System.out.println("태그 목록: " + dto.getTags());
//                }
//                if (dto.getImageUrls() != null) {
//                    System.out.println("이미지 URL 목록: " + dto.getImageUrls());
//                }
//            } else {
//                System.out.println("DTO가 null입니다!");
//                return "redirect:/post";
//            }
//
//            model.addAttribute("dto", dto);
//            model.addAttribute("requestDTO", requestDTO);
//            model.addAttribute("apiKey", apiKey);
//
//            System.out.println("=== Model에 데이터 추가 완료 ===");
//            System.out.println("템플릿 반환: post/modify-post");
//
//            return "post/modify-post";
//        } catch (Exception e) {
//            System.err.println("게시글 수정 페이지 로드 중 오류: " + e.getMessage());
//            e.printStackTrace();
//            return "redirect:/post";
//        }
//    }
//
//    @PostMapping("/post/modify")
//    public String modify(PostDTO dto,
//                         @RequestParam(value = "tags", required = false) List<String> tags,
//                         @RequestParam(value = "imageFiles", required = false) List<MultipartFile> imageFiles,
//                         @RequestParam(value = "deleteImageIndexes", required = false) List<Integer> deleteImageIndexes,
//                         @ModelAttribute("requestDTO") PageRequestDTO requestDTO,
//                         HttpSession session,
//                         RedirectAttributes redirectAttributes) {
//
//        log.info("PostController modify() - dto: " + dto);
//        log.info("PostController modify() - tags: " + tags);
//        log.info("PostController modify() - 새 이미지 파일 수: " + (imageFiles != null ? imageFiles.size() : 0));
//        log.info("PostController modify() - 삭제할 이미지 인덱스: " + deleteImageIndexes);
//
//        // 로그인 체크
//        if (!isLoggedIn(session)) {
//            return "redirect:/login?error=login_required";
//        }
//
//        try {
//            // 태그 정보를 DTO에 설정
//            dto.setTags(tags);
//
//            // 기존 이미지 URL들 가져오기
//            PostDTO existingPost = postService.read(dto.getId());
//            List<String> currentImageUrls = new ArrayList<>(existingPost.getImageUrls() != null ?
//                    existingPost.getImageUrls() : new ArrayList<>());
//
//            // 삭제할 이미지들 S3에서 삭제 및 URL 리스트에서 제거
//            if (deleteImageIndexes != null && !deleteImageIndexes.isEmpty()) {
//                log.info("=== S3 이미지 삭제 시작 ===");
//
//                // 인덱스를 역순으로 정렬해서 삭제 (리스트 인덱스 오류 방지)
//                deleteImageIndexes.sort((a, b) -> b.compareTo(a));
//
//                for (Integer index : deleteImageIndexes) {
//                    if (index >= 0 && index < currentImageUrls.size()) {
//                        String imageUrl = currentImageUrls.get(index);
//                        try {
//                            // S3에서 파일 삭제
//                            String fileName = s3Service.extractFileNameFromUrl(imageUrl);
//                            s3Service.deleteFile(fileName);
//                            log.info("S3 이미지 삭제 성공: {}", imageUrl);
//
//                            // URL 리스트에서도 제거
//                            currentImageUrls.remove((int)index);
//                        } catch (Exception e) {
//                            log.error("S3 이미지 삭제 실패: {}", e.getMessage());
//                        }
//                    }
//                }
//            }
//
//            // 새로운 이미지들 S3에 업로드
//            if (imageFiles != null && !imageFiles.isEmpty()) {
//                log.info("=== 새 이미지 S3 업로드 시작 ===");
//
//                for (MultipartFile file : imageFiles) {
//                    if (!file.isEmpty() && isValidImageFile(file)) {
//                        try {
//                            String imageUrl = s3Service.upload(file, "posts");
//                            currentImageUrls.add(imageUrl);
//                            log.info("새 이미지 S3 업로드 성공: {}", imageUrl);
//                        } catch (Exception e) {
//                            log.error("새 이미지 S3 업로드 실패: {}", e.getMessage());
//                        }
//                    }
//                }
//            }
//
//            // DTO에 업데이트된 이미지 URL들 설정
//            dto.setImageUrls(currentImageUrls);
//
//            // 수정 서비스 호출
//            postService.modify(dto, new MultipartFile[0], null);
//
//            redirectAttributes.addAttribute("page", requestDTO.getPage());
//            redirectAttributes.addAttribute("type", requestDTO.getType());
//            redirectAttributes.addAttribute("keyword", requestDTO.getKeyword());
//
//            return "redirect:/post/" + dto.getId();
//
//        } catch (IllegalStateException e) {
//            redirectAttributes.addFlashAttribute("error", e.getMessage());
//            return "redirect:/post/" + dto.getId();
//        } catch (Exception e) {
//            log.error("게시글 수정 중 오류 발생", e);
//            redirectAttributes.addFlashAttribute("error", "게시글 수정 중 오류가 발생했습니다.");
//            return "redirect:/post/modify/" + dto.getId();
//        }
//    }
//
//    @PostMapping("/post/remove")
//    public String remove(@RequestParam("id") Long id,
//                         @ModelAttribute("requestDTO") PageRequestDTO requestDTO,
//                         HttpSession session,
//                         RedirectAttributes redirectAttributes) {
//        log.info("PostController remove() - id: " + id);
//
//        // 로그인 체크
//        if (!isLoggedIn(session)) {
//            return "redirect:/login?error=login_required";
//        }
//
//        try {
//            // 게시글 삭제 전에 S3 이미지들도 삭제
//            PostDTO postToDelete = postService.read(id);
//            if (postToDelete.getImageUrls() != null && !postToDelete.getImageUrls().isEmpty()) {
//                log.info("=== 게시글 삭제 시 S3 이미지들 삭제 시작 ===");
//
//                for (String imageUrl : postToDelete.getImageUrls()) {
//                    try {
//                        String fileName = s3Service.extractFileNameFromUrl(imageUrl);
//                        s3Service.deleteFile(fileName);
//                        log.info("게시글 삭제 시 S3 이미지 삭제 성공: {}", imageUrl);
//                    } catch (Exception e) {
//                        log.error("게시글 삭제 시 S3 이미지 삭제 실패: {}", e.getMessage());
//                    }
//                }
//            }
//
//            postService.remove(id);
//
//            redirectAttributes.addAttribute("page", 1);
//            redirectAttributes.addAttribute("type", requestDTO.getType());
//            redirectAttributes.addAttribute("keyword", requestDTO.getKeyword());
//
//            return "redirect:/post";
//        } catch (IllegalStateException e) {
//            redirectAttributes.addFlashAttribute("error", e.getMessage());
//            return "redirect:/post";
//        }
//    }
//
//    // 로그인 상태를 확인하는 헬퍼 메서드
//    private boolean isLoggedIn(HttpSession session) {
//        return session != null && session.getAttribute("user_id") != null;
//    }
//}