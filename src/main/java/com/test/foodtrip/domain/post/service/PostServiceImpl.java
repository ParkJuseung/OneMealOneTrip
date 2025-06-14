package com.test.foodtrip.domain.post.service;

import com.test.foodtrip.domain.post.dto.PageRequestDTO;
import com.test.foodtrip.domain.post.dto.PageResultDTO;
import com.test.foodtrip.domain.post.dto.PostDTO;
import com.test.foodtrip.domain.post.entity.Post;
import com.test.foodtrip.domain.post.entity.PostImage;
import com.test.foodtrip.domain.post.entity.PostTag;
import com.test.foodtrip.domain.post.entity.PostTagging;
import com.test.foodtrip.domain.post.repository.PostImageRepository;
import com.test.foodtrip.domain.post.repository.PostRepository;
import com.test.foodtrip.domain.post.repository.PostTagRepository;
import com.test.foodtrip.domain.post.repository.PostTaggingRepository;
import com.test.foodtrip.domain.user.entity.User;
import com.test.foodtrip.domain.user.repository.UserRepository;
import com.test.foodtrip.common.aws.S3Service;
import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {

    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final PostTagRepository postTagRepository;
    private final PostTaggingRepository postTaggingRepository;
    private final PostImageRepository postImageRepository;
    private final S3Service s3Service;

    @Transactional
    @Override
    public Long create(PostDTO dto, MultipartFile[] images) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            throw new IllegalStateException("로그인된 사용자를 찾을 수 없습니다.");
        }

        System.out.println("=== 게시글 생성 시작 ===");
        System.out.println("DTO 이미지 URL 개수: " + (dto.getImageUrls() != null ? dto.getImageUrls().size() : 0));
        System.out.println("전달받은 MultipartFile 배열: " + (images != null ? images.length : "null"));

        Post post = dtoToEntity(dto, currentUser);

        // S3 URL 기반 이미지 처리 (Controller에서 이미 S3에 업로드됨)
        if (dto.getImageUrls() != null && !dto.getImageUrls().isEmpty()) {
            System.out.println("=== S3 URL 기반 이미지 처리 시작 ===");

            for (int i = 0; i < dto.getImageUrls().size(); i++) {
                String imageUrl = dto.getImageUrls().get(i);
                PostImage postImage = new PostImage();
                postImage.setImageUrl(imageUrl);
                postImage.setImageOrder(i);
                post.addImage(postImage);

                System.out.println("S3 이미지 URL 추가: " + imageUrl + " (순서: " + i + ")");
            }

            System.out.println("총 추가된 이미지 수: " + dto.getImageUrls().size());
        }

        // 기존 MultipartFile 처리 (하위 호환성 - 빈 배열이 아닌 경우)
        else if (images != null && images.length > 0) {
            System.out.println("=== 기존 MultipartFile 처리 (하위 호환성) ===");

            int savedImageCount = 0;
            for (int i = 0; i < images.length; i++) {
                MultipartFile image = images[i];
                if (!image.isEmpty()) {
                    try {
                        // S3에 업로드
                        String imageUrl = s3Service.upload(image, "posts");
                        PostImage postImage = new PostImage();
                        postImage.setImageUrl(imageUrl);
                        postImage.setImageOrder(i);
                        post.addImage(postImage);
                        savedImageCount++;

                        System.out.println("MultipartFile S3 업로드 성공: " + imageUrl);
                    } catch (IOException e) {
                        System.err.println("MultipartFile S3 업로드 실패: " + e.getMessage());
                        throw new RuntimeException("이미지 저장 실패: " + e.getMessage());
                    }
                }
            }
            System.out.println("MultipartFile로 저장된 이미지 수: " + savedImageCount);
        }

        System.out.println("Post에 연결된 최종 이미지 수: " + post.getImages().size());

        postRepository.save(post);
        System.out.println("게시글 저장 완료 ID: " + post.getId());

        // 태그 저장
        if (dto.getTags() != null && !dto.getTags().isEmpty()) {
            saveTagsOptimized(post, dto.getTags());
        }

        return post.getId();
    }

    @Override
    @Transactional
    public PageResultDTO<PostDTO, Post> getList(PageRequestDTO requestDTO) {
        Pageable pageable = requestDTO.getPageable(Sort.by("createdAt").descending());

        // 1단계: 기본 Post 정보만 페이징 조회
        Page<Post> result = postRepository.findAll(pageable);

        if (result.isEmpty()) {
            return new PageResultDTO<>(result, entity -> new PostDTO());
        }

        // 2단계: 해당 페이지의 모든 Post ID 수집
        List<Long> postIds = result.getContent().stream()
                .map(Post::getId)
                .collect(Collectors.toList());

        // 3단계: 모든 태그 정보를 한번에 조회 (N+1 해결)
        List<Object[]> tagResults = postTaggingRepository.findTagsByPostIds(postIds);

        // 4단계: Post ID별로 태그를 그룹핑
        Map<Long, List<String>> tagsMap = tagResults.stream()
                .collect(Collectors.groupingBy(
                        arr -> (Long) arr[0], // Post ID
                        Collectors.mapping(arr -> (String) arr[1], Collectors.toList()) // Tag Text
                ));

        // 추가: 모든 이미지 정보를 한번에 조회 (N+1 해결)
        List<PostImage> imageResults = postImageRepository.findByPostIdIn(postIds);

        // 추가: Post ID별로 이미지를 그룹핑
        Map<Long, List<PostImage>> imagesMap = imageResults.stream()
                .collect(Collectors.groupingBy(
                        image -> image.getPost().getId(),
                        Collectors.toList()
                ));

        Function<Post, PostDTO> fn = (entity -> {
            PostDTO dto = entityToDto(entity);

            // 미리 조회한 태그 정보로 오버라이드 (추가 쿼리 없음)
            List<String> tags = tagsMap.getOrDefault(entity.getId(), Collections.emptyList());
            dto.setTags(tags);

            // 추가: 미리 조회한 이미지 정보로 오버라이드 (추가 쿼리 없음)
            List<PostImage> images = imagesMap.getOrDefault(entity.getId(), Collections.emptyList());
            List<String> imageUrls = images.stream()
                    .sorted(Comparator.comparing(PostImage::getImageOrder)) // 순서대로 정렬
                    .map(PostImage::getImageUrl)
                    .collect(Collectors.toList());
            dto.setImageUrls(imageUrls);

            return dto;
        });

        return new PageResultDTO<>(result, fn);
    }

    @Override
    public PostDTO read(Long id) {
        System.out.println("=== 게시글 조회 시작 ===");
        System.out.println("조회할 게시글 ID: " + id);

        // 수정: 이미지와 함께 조회하도록 변경
        Optional<Post> result = postRepository.findByIdWithImages(id);

        if (result.isPresent()) {
            Post post = result.get();
            System.out.println("게시글 제목: " + post.getTitle());
            System.out.println("조회된 이미지 수: " + (post.getImages() != null ? post.getImages().size() : "null"));

            // 이미지 URL들 출력
            if (post.getImages() != null) {
                for (int i = 0; i < post.getImages().size(); i++) {
                    PostImage img = post.getImages().get(i);
                    System.out.println("이미지 " + i + ": " + img.getImageUrl() + " (순서: " + img.getImageOrder() + ")");
                }
            }

            // 조회수 증가
            postRepository.incrementViewCount(id);

            PostDTO dto = entityToDto(post);
            System.out.println("DTO 변환 후 이미지 URL 수: " + (dto.getImageUrls() != null ? dto.getImageUrls().size() : "null"));

            // DTO의 이미지 URL들도 출력
            if (dto.getImageUrls() != null) {
                for (int i = 0; i < dto.getImageUrls().size(); i++) {
                    System.out.println("DTO 이미지 " + i + ": " + dto.getImageUrls().get(i));
                }
            }

            // 태그 정보 추가
            List<String> tags = postTaggingRepository.findTagsByPostId(id);
            dto.setTags(tags);

            return dto;
        } else {
            System.out.println("게시글을 찾을 수 없음: " + id);
        }
        return null;
    }

    @Override
    @Transactional
    public void modify(PostDTO dto) {
        Optional<Post> result = postRepository.findById(dto.getId());
        if (result.isPresent()) {
            Post post = result.get();

            // 게시글 작성자 권한 체크
            User currentUser = getCurrentUser();
            if (currentUser == null) {
                throw new IllegalStateException("로그인된 사용자를 찾을 수 없습니다.");
            }

            // 작성자 본인이거나 관리자인 경우만 수정 허용
            if (!post.getUser().getId().equals(currentUser.getId()) &&
                    !"ADMIN".equals(currentUser.getRole())) {
                throw new IllegalStateException("게시글을 수정할 권한이 없습니다.");
            }

            post.setTitle(dto.getTitle());
            post.setContent(dto.getContent());
            post.setLatitude(dto.getLatitude());
            post.setLongitude(dto.getLongitude());
            post.setPlaceName(dto.getPlaceName());

            postRepository.save(post);

            // 최적화된 태그 업데이트
            updatePostTagsOptimized(post, dto.getTags());
        }
    }

    @Override
    @Transactional
    public void modify(PostDTO dto, MultipartFile[] images, List<Integer> deleteImageIndexes) {
        Optional<Post> result = postRepository.findByIdWithImages(dto.getId());
        if (result.isPresent()) {
            Post post = result.get();

            // 권한 체크
            User currentUser = getCurrentUser();
            if (currentUser == null) {
                throw new IllegalStateException("로그인된 사용자를 찾을 수 없습니다.");
            }

            if (!post.getUser().getId().equals(currentUser.getId()) &&
                    !"ADMIN".equals(currentUser.getRole())) {
                throw new IllegalStateException("게시글을 수정할 권한이 없습니다.");
            }

            System.out.println("=== 게시글 수정 시작 ===");
            System.out.println("DTO 이미지 URL 개수: " + (dto.getImageUrls() != null ? dto.getImageUrls().size() : 0));

            // 기본 정보 수정
            post.setTitle(dto.getTitle());
            post.setContent(dto.getContent());
            post.setLatitude(dto.getLatitude());
            post.setLongitude(dto.getLongitude());
            post.setPlaceName(dto.getPlaceName());
            post.setPlaceAddress(dto.getPlaceAddress());
            post.setPlaceId(dto.getPlaceId());

            // S3 URL 기반 이미지 업데이트 (Controller에서 이미 처리됨)
            if (dto.getImageUrls() != null) {
                System.out.println("=== S3 URL 기반 이미지 업데이트 ===");

                // 기존 이미지 모두 삭제
                List<PostImage> existingImages = new ArrayList<>(post.getImages());
                for (PostImage existingImage : existingImages) {
                    post.getImages().remove(existingImage);
                    postImageRepository.delete(existingImage);
                    System.out.println("기존 이미지 제거: " + existingImage.getImageUrl());
                }

                // 새 이미지 URL들로 교체
                for (int i = 0; i < dto.getImageUrls().size(); i++) {
                    String imageUrl = dto.getImageUrls().get(i);
                    PostImage postImage = new PostImage();
                    postImage.setImageUrl(imageUrl);
                    postImage.setImageOrder(i);
                    post.addImage(postImage);
                    System.out.println("새 이미지 추가: " + imageUrl + " (순서: " + i + ")");
                }

                System.out.println("최종 이미지 수: " + post.getImages().size());
            }

            // 기존 MultipartFile 처리 (하위 호환성 - Controller에서 이미 S3 처리했으면 빈 배열)
            else if (images != null && images.length > 0) {
                System.out.println("=== 기존 MultipartFile 처리 (하위 호환성) ===");

                // 기존 이미지 삭제 처리 (인덱스 기반)
                if (deleteImageIndexes != null && !deleteImageIndexes.isEmpty()) {
                    List<PostImage> currentImages = new ArrayList<>(post.getImages());
                    Collections.sort(deleteImageIndexes, Collections.reverseOrder());

                    for (Integer index : deleteImageIndexes) {
                        if (index >= 0 && index < currentImages.size()) {
                            PostImage imageToDelete = currentImages.get(index);

                            // S3에서 파일 삭제
                            try {
                                String fileName = s3Service.extractFileNameFromUrl(imageToDelete.getImageUrl());
                                s3Service.deleteFile(fileName);
                                System.out.println("S3 파일 삭제 성공: " + fileName);
                            } catch (Exception e) {
                                System.err.println("S3 파일 삭제 실패: " + e.getMessage());
                            }

                            post.getImages().remove(imageToDelete);
                            postImageRepository.delete(imageToDelete);
                            System.out.println("기존 이미지 삭제: " + imageToDelete.getImageUrl());
                        }
                    }

                    // 남은 이미지들의 순서 재정렬
                    List<PostImage> remainingImages = new ArrayList<>(post.getImages());
                    for (int i = 0; i < remainingImages.size(); i++) {
                        remainingImages.get(i).setImageOrder(i);
                    }
                }

                // 새 이미지 추가 처리
                int currentMaxOrder = post.getImages().stream()
                        .mapToInt(PostImage::getImageOrder)
                        .max()
                        .orElse(-1);

                int addedCount = 0;
                for (int i = 0; i < images.length; i++) {
                    MultipartFile image = images[i];
                    if (!image.isEmpty()) {
                        try {
                            String imageUrl = s3Service.upload(image, "posts");
                            PostImage postImage = new PostImage();
                            postImage.setImageUrl(imageUrl);
                            postImage.setImageOrder(currentMaxOrder + addedCount + 1);
                            post.addImage(postImage);
                            addedCount++;

                            System.out.println("새 이미지 S3 업로드: " + imageUrl);
                        } catch (IOException e) {
                            throw new RuntimeException("이미지 저장 실패: " + e.getMessage());
                        }
                    }
                }
            }

            // Post 저장
            postRepository.save(post);

            // 태그 업데이트
            updatePostTagsOptimized(post, dto.getTags());

            System.out.println("게시글 수정 완료 - ID: " + post.getId());
        }
    }

    @Override
    @Transactional
    public void remove(Long id) {
        Optional<Post> result = postRepository.findByIdWithImages(id);
        if (result.isPresent()) {
            Post post = result.get();

            // 게시글 작성자 권한 체크
            User currentUser = getCurrentUser();
            if (currentUser == null) {
                throw new IllegalStateException("로그인된 사용자를 찾을 수 없습니다.");
            }

            // 작성자 본인이거나 관리자인 경우만 삭제 허용
            if (!post.getUser().getId().equals(currentUser.getId()) &&
                    !"ADMIN".equals(currentUser.getRole())) {
                throw new IllegalStateException("게시글을 삭제할 권한이 없습니다.");
            }

            // 관련된 S3 이미지들 삭제
            if (post.getImages() != null && !post.getImages().isEmpty()) {
                System.out.println("=== 게시글 삭제 시 S3 이미지 삭제 ===");

                for (PostImage postImage : post.getImages()) {
                    try {
                        String fileName = s3Service.extractFileNameFromUrl(postImage.getImageUrl());
                        s3Service.deleteFile(fileName);
                        System.out.println("S3 파일 삭제 성공: " + fileName + " (URL: " + postImage.getImageUrl() + ")");
                    } catch (Exception e) {
                        System.err.println("S3 파일 삭제 실패: " + e.getMessage());
                    }
                }
            }

            // 태그 관계도 함께 삭제됨 (CASCADE)
            postRepository.deleteById(id);
            System.out.println("게시글 삭제 완료 - ID: " + id);
        }
    }

    @Override
    @Transactional
    public void increaseViewCount(Long id) {
        Optional<Post> result = postRepository.findById(id);
        if (result.isPresent()) {
            Post post = result.get();
            post.setViewCount(post.getViewCount() + 1);
            postRepository.save(post);
        }
    }

    @Override
    @Transactional
    public PageResultDTO<PostDTO, Post> searchPosts(String keyword, PageRequestDTO requestDTO) {
        Pageable pageable = requestDTO.getPageable(Sort.by("createdAt").descending());

        // 키워드가 null이거나 비어있으면 모든 게시글 반환
        Page<Post> result = postRepository.searchPosts(keyword, pageable);

        if (result.isEmpty()) {
            return new PageResultDTO<>(result, entity -> new PostDTO());
        }

        // 결과 후처리는 getList 메소드와 동일하게 진행
        List<Long> postIds = result.getContent().stream()
                .map(Post::getId)
                .collect(Collectors.toList());

        // 태그 정보 조회
        List<Object[]> tagResults = postTaggingRepository.findTagsByPostIds(postIds);
        Map<Long, List<String>> tagsMap = tagResults.stream()
                .collect(Collectors.groupingBy(
                        arr -> (Long) arr[0],
                        Collectors.mapping(arr -> (String) arr[1], Collectors.toList())
                ));

        // 이미지 정보 조회
        List<PostImage> imageResults = postImageRepository.findByPostIdIn(postIds);
        Map<Long, List<PostImage>> imagesMap = imageResults.stream()
                .collect(Collectors.groupingBy(
                        image -> image.getPost().getId(),
                        Collectors.toList()
                ));

        Function<Post, PostDTO> fn = (entity -> {
            PostDTO dto = entityToDto(entity);

            // 태그 정보 설정
            List<String> tags = tagsMap.getOrDefault(entity.getId(), Collections.emptyList());
            dto.setTags(tags);

            // 이미지 정보 설정
            List<PostImage> images = imagesMap.getOrDefault(entity.getId(), Collections.emptyList());
            List<String> imageUrls = images.stream()
                    .sorted(Comparator.comparing(PostImage::getImageOrder))
                    .map(PostImage::getImageUrl)
                    .collect(Collectors.toList());
            dto.setImageUrls(imageUrls);

            return dto;
        });

        return new PageResultDTO<>(result, fn);
    }


    @Override
    @Transactional
    public PageResultDTO<PostDTO, Post> searchPostsByTag(String tagKeyword, PageRequestDTO requestDTO) {
        Pageable pageable = requestDTO.getPageable(Sort.by("createdAt").descending());

        // 태그 키워드로 게시글 검색
        Page<Post> result = postRepository.searchPostsByTag(tagKeyword, pageable);

        if (result.isEmpty()) {
            return new PageResultDTO<>(result, entity -> new PostDTO());
        }

        // 이하 로직은 searchPosts와 동일
        List<Long> postIds = result.getContent().stream()
                .map(Post::getId)
                .collect(Collectors.toList());

        List<Object[]> tagResults = postTaggingRepository.findTagsByPostIds(postIds);
        Map<Long, List<String>> tagsMap = tagResults.stream()
                .collect(Collectors.groupingBy(
                        arr -> (Long) arr[0],
                        Collectors.mapping(arr -> (String) arr[1], Collectors.toList())
                ));

        List<PostImage> imageResults = postImageRepository.findByPostIdIn(postIds);
        Map<Long, List<PostImage>> imagesMap = imageResults.stream()
                .collect(Collectors.groupingBy(
                        image -> image.getPost().getId(),
                        Collectors.toList()
                ));

        Function<Post, PostDTO> fn = (entity -> {
            PostDTO dto = entityToDto(entity);

            List<String> tags = tagsMap.getOrDefault(entity.getId(), Collections.emptyList());
            dto.setTags(tags);

            List<PostImage> images = imagesMap.getOrDefault(entity.getId(), Collections.emptyList());
            List<String> imageUrls = images.stream()
                    .sorted(Comparator.comparing(PostImage::getImageOrder))
                    .map(PostImage::getImageUrl)
                    .collect(Collectors.toList());
            dto.setImageUrls(imageUrls);

            return dto;
        });

        return new PageResultDTO<>(result, fn);
    }

    // 최적화된 헬퍼 메서드들

    /**
     * 태그 저장 최적화 - 배치 처리로 N+1 해결
     */
    private void saveTagsOptimized(Post post, List<String> tagTexts) {
        System.out.println("=== 태그 저장 시작 (최적화) ===");
        System.out.println("Post ID: " + post.getId());
        System.out.println("태그 개수: " + (tagTexts != null ? tagTexts.size() : 0));

        if (tagTexts == null || tagTexts.isEmpty()) {
            System.out.println("태그가 없어서 저장하지 않음");
            return;
        }

        // 1단계: 태그 텍스트 정리
        List<String> cleanTagTexts = tagTexts.stream()
                .filter(tag -> tag != null && !tag.trim().isEmpty())
                .map(String::trim)
                .distinct()
                .collect(Collectors.toList());

        if (cleanTagTexts.isEmpty()) {
            return;
        }

        System.out.println("정리된 태그들: " + cleanTagTexts);

        try {
            // 2단계: 기존 태그들 일괄 조회 (N+1 방지)
            List<PostTag> existingTags = postTagRepository.findAllByTagTextIn(cleanTagTexts);
            Map<String, PostTag> tagMap = existingTags.stream()
                    .collect(Collectors.toMap(PostTag::getTagText, Function.identity()));

            System.out.println("기존 태그 개수: " + existingTags.size());

            // 3단계: 새로운 태그들 생성
            List<PostTag> newTags = cleanTagTexts.stream()
                    .filter(tagText -> !tagMap.containsKey(tagText))
                    .map(PostTag::new)
                    .collect(Collectors.toList());

            if (!newTags.isEmpty()) {
                System.out.println("새 태그 생성 개수: " + newTags.size());
                List<PostTag> savedNewTags = postTagRepository.saveAll(newTags);
                savedNewTags.forEach(tag -> tagMap.put(tag.getTagText(), tag));
            }

            // 4단계: PostTagging 일괄 생성
            List<PostTagging> postTaggings = cleanTagTexts.stream()
                    .map(tagText -> {
                        PostTag postTag = tagMap.get(tagText);
                        PostTagging postTagging = new PostTagging(post, postTag);
                        postTagging.setId(new PostTagging.PostTaggingId(post.getId(), postTag.getId()));
                        return postTagging;
                    })
                    .collect(Collectors.toList());

            postTaggingRepository.saveAll(postTaggings);
            System.out.println("PostTagging 저장 완료: " + postTaggings.size() + "개");

        } catch (Exception e) {
            System.err.println("태그 저장 중 오류: " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println("=== 태그 저장 완료 (최적화) ===");
    }

    /**
     * 태그 업데이트 최적화
     */
    private void updatePostTagsOptimized(Post post, List<String> newTags) {
        // 기존 태그 삭제
        List<PostTagging> existingTaggings = postTaggingRepository.findByPostId(post.getId());
        if (!existingTaggings.isEmpty()) {
            postTaggingRepository.deleteAll(existingTaggings);
        }

        // 새 태그 추가
        if (newTags != null && !newTags.isEmpty()) {
            saveTagsOptimized(post, newTags);
        }
    }

    /**
     * 현재 로그인된 사용자를 세션에서 가져오는 메서드
     */
    private User getCurrentUser() {
        try {
            ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
            HttpSession session = attr.getRequest().getSession(false);

            if (session != null) {
                Long userId = (Long) session.getAttribute("user_id");
                if (userId != null) {
                    Optional<User> user = userRepository.findById(userId);
                    return user.orElse(null);
                }
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public List<String> getTopTags(int limit) {
        List<Object[]> results = postTagRepository.findTopTags();
        return results.stream()
                .map(row -> (String) row[0])  // 첫 번째 열은 태그 텍스트
                .limit(limit)
                .collect(Collectors.toList());
    }
}