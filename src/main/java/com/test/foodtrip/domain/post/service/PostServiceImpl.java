package com.test.foodtrip.domain.post.service;

import com.test.foodtrip.domain.post.dto.PageRequestDTO;
import com.test.foodtrip.domain.post.dto.PageResultDTO;
import com.test.foodtrip.domain.post.dto.PostDTO;
import com.test.foodtrip.domain.post.entity.Post;
import com.test.foodtrip.domain.post.entity.PostTag;
import com.test.foodtrip.domain.post.entity.PostTagging;
import com.test.foodtrip.domain.post.repository.PostRepository;
import com.test.foodtrip.domain.post.repository.PostTagRepository;
import com.test.foodtrip.domain.post.repository.PostTaggingRepository;
import com.test.foodtrip.domain.user.entity.User;
import com.test.foodtrip.domain.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

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

    @Override
    @Transactional
    public Long create(PostDTO dto) {
        // 현재 로그인된 사용자 가져오기
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            throw new IllegalStateException("로그인된 사용자를 찾을 수 없습니다.");
        }

        Post post = dtoToEntity(dto, currentUser);
        postRepository.save(post);

        // ✅ 최적화된 태그 저장
        if (dto.getTags() != null && !dto.getTags().isEmpty()) {
            saveTagsOptimized(post, dto.getTags());
        }

        return post.getId();
    }

    @Override
    @Transactional
    public PageResultDTO<PostDTO, Post> getList(PageRequestDTO requestDTO) {
        Pageable pageable = requestDTO.getPageable(Sort.by("createdAt").descending());

        // ✅ 1단계: 기본 Post 정보만 페이징 조회
        Page<Post> result = postRepository.findAll(pageable);

        if (result.isEmpty()) {
            return new PageResultDTO<>(result, entity -> new PostDTO());
        }

        // ✅ 2단계: 해당 페이지의 모든 Post ID 수집
        List<Long> postIds = result.getContent().stream()
                .map(Post::getId)
                .collect(Collectors.toList());

        // ✅ 3단계: 모든 태그 정보를 한번에 조회 (N+1 해결!)
        List<Object[]> tagResults = postTaggingRepository.findTagsByPostIds(postIds);

        // ✅ 4단계: Post ID별로 태그를 그룹핑
        Map<Long, List<String>> tagsMap = tagResults.stream()
                .collect(Collectors.groupingBy(
                        arr -> (Long) arr[0], // Post ID
                        Collectors.mapping(arr -> (String) arr[1], Collectors.toList()) // Tag Text
                ));

        Function<Post, PostDTO> fn = (entity -> {
            PostDTO dto = entityToDto(entity); // ✅ 수정된 인터페이스 메서드 사용

            // ✅ 미리 조회한 태그 정보로 오버라이드 (추가 쿼리 없음!)
            List<String> tags = tagsMap.getOrDefault(entity.getId(), Collections.emptyList());
            dto.setTags(tags);

            return dto;
        });

        return new PageResultDTO<>(result, fn);
    }

    @Override
    public PostDTO read(Long id) {
        Optional<Post> result = postRepository.findById(id);
        if (result.isPresent()) {
            Post post = result.get();
            // 조회수 증가
            post.setViewCount(post.getViewCount() + 1);
            postRepository.save(post);

            PostDTO dto = entityToDto(post);
            // ✅ 태그 정보 추가 (별도 쿼리 1회)
            List<String> tags = postTaggingRepository.findTagsByPostId(id);
            dto.setTags(tags);

            return dto;
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

            // ✅ 최적화된 태그 업데이트
            updatePostTagsOptimized(post, dto.getTags());
        }
    }

    @Override
    @Transactional
    public void remove(Long id) {
        Optional<Post> result = postRepository.findById(id);
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

            // 태그 관계도 함께 삭제됨 (CASCADE)
            postRepository.deleteById(id);
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

    // ========== 최적화된 헬퍼 메서드들 ==========

    /**
     * ✅ 태그 저장 최적화 - 배치 처리로 N+1 해결
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
            // ✅ 2단계: 기존 태그들 일괄 조회 (N+1 방지!)
            List<PostTag> existingTags = postTagRepository.findAllByTagTextIn(cleanTagTexts);
            Map<String, PostTag> tagMap = existingTags.stream()
                    .collect(Collectors.toMap(PostTag::getTagText, Function.identity()));

            System.out.println("기존 태그 개수: " + existingTags.size());

            // ✅ 3단계: 새로운 태그들 생성
            List<PostTag> newTags = cleanTagTexts.stream()
                    .filter(tagText -> !tagMap.containsKey(tagText))
                    .map(PostTag::new)
                    .collect(Collectors.toList());

            if (!newTags.isEmpty()) {
                System.out.println("새 태그 생성 개수: " + newTags.size());
                List<PostTag> savedNewTags = postTagRepository.saveAll(newTags);
                savedNewTags.forEach(tag -> tagMap.put(tag.getTagText(), tag));
            }

            // ✅ 4단계: PostTagging 일괄 생성
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
     * ✅ 태그 업데이트 최적화
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
}