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
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

@Slf4j
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

        // 해시태그 처리
        if (dto.getTags() != null && !dto.getTags().isEmpty()) {
            saveTags(post, dto.getTags());
        }

        return post.getId();
    }

    @Override
    public PageResultDTO<PostDTO, Post> getList(PageRequestDTO requestDTO) {
        Pageable pageable = requestDTO.getPageable(Sort.by("createdAt").descending());

        // 📈 최적화: 연관관계 없이 기본 정보만 조회
        Page<Post> result = postRepository.findAllOptimized(pageable);

        Function<Post, PostDTO> fn = (entity -> {
            // 📈 최소한의 정보만 DTO로 변환 (연관관계 접근 안함)
            PostDTO dto = PostDTO.builder()
                    .id(entity.getId())
                    .title(entity.getTitle())
                    .content(entity.getContent())
                    .viewCount(entity.getViewCount())
                    .placeId(entity.getPlaceId())
                    .placeName(entity.getPlaceName())
                    .latitude(entity.getLatitude())
                    .longitude(entity.getLongitude())
                    .createdAt(entity.getCreatedAt())
                    .updatedAt(entity.getUpdatedAt())
                    .build();

            // 태그 정보 추가 (필요한 경우에만)
            if (requestDTO.isIncludeTags()) { // 태그 포함 여부를 요청에서 결정
                List<String> tags = postTaggingRepository.findTagsByPostId(entity.getId());
                dto.setTags(tags);
            }

            return dto;
        });

        return new PageResultDTO<>(result, fn);
    }

    @Override
    @Transactional
    public PostDTO read(Long id) {
        // 기본 정보 + 이미지 조회 (한 번의 쿼리)
        Post post = postRepository.findByIdWithImages(id)
                .orElseThrow(() -> new EntityNotFoundException("Post not found: " + id));

        // 조회수 증가 (별도 쿼리로 최적화)
        postRepository.incrementViewCount(id);

        // DTO 변환
        PostDTO dto = entityToDto(post);

        // 태그 정보 추가
        List<String> tags = postTaggingRepository.findTagsByPostId(id);
        dto.setTags(tags);

        return dto;
    }

    @Override
    @Transactional
    public void modify(PostDTO dto) {
        Optional<Post> result = postRepository.findById(dto.getId());
        if (result.isPresent()) {
            Post post = result.get();

            // 게시글 작성자 권한 체크
            validatePostAuthorization(post);

            // 게시글 기본 정보 수정
            updatePostBasicInfo(post, dto);
            postRepository.save(post);

            // 태그 정보 업데이트
            updatePostTags(post, dto.getTags());
        }
    }

    @Override
    @Transactional
    public void remove(Long id) {
        Optional<Post> result = postRepository.findById(id);
        if (result.isPresent()) {
            Post post = result.get();

            // 게시글 작성자 권한 체크
            validatePostAuthorization(post);

            // 태그 관계도 함께 삭제됨 (CASCADE)
            postRepository.deleteById(id);
        }
    }

    @Override
    @Transactional
    public void increaseViewCount(Long id) {
        // 최적화: 직접 쿼리로 조회수 증가 (엔티티 조회 없이)
        postRepository.incrementViewCount(id);
    }

    // === private 헬퍼 메서드들 ===

    /**
     * 게시글 기본 정보 업데이트
     */
    private void updatePostBasicInfo(Post post, PostDTO dto) {
        post.setTitle(dto.getTitle());
        post.setContent(dto.getContent());
        post.setLatitude(dto.getLatitude());
        post.setLongitude(dto.getLongitude());
        post.setPlaceName(dto.getPlaceName());
    }

    /**
     * 게시글 태그 정보 업데이트
     */
    private void updatePostTags(Post post, List<String> newTags) {
        // 기존 태그 삭제
        List<PostTagging> existingTaggings = postTaggingRepository.findByPostId(post.getId());
        postTaggingRepository.deleteAll(existingTaggings);

        // 새 태그 추가
        if (newTags != null && !newTags.isEmpty()) {
            saveTags(post, newTags);
        }
    }

    /**
     * 게시글 작성자 권한 검증
     */
    private void validatePostAuthorization(Post post) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            throw new IllegalStateException("로그인된 사용자를 찾을 수 없습니다.");
        }

        // 작성자 본인이거나 관리자인 경우만 허용
        if (!post.getUser().getId().equals(currentUser.getId()) &&
                !"ADMIN".equals(currentUser.getRole())) {
            throw new IllegalStateException("해당 게시글에 대한 권한이 없습니다.");
        }
    }

    /**
     * 태그 저장 메서드
     */
    private void saveTags(Post post, List<String> tagTexts) {
        log.debug("태그 저장 시작 - Post ID: {}, 태그 개수: {}",
                post.getId(), tagTexts != null ? tagTexts.size() : 0);

        if (tagTexts == null || tagTexts.isEmpty()) {
            return;
        }

        for (String tagText : tagTexts) {
            if (tagText != null && !tagText.trim().isEmpty()) {
                String cleanTagText = tagText.trim();

                try {
                    // 기존 태그 찾기 또는 새로 생성
                    PostTag postTag = postTagRepository.findByTagText(cleanTagText)
                            .orElseGet(() -> {
                                log.debug("새 태그 생성: {}", cleanTagText);
                                return postTagRepository.save(new PostTag(cleanTagText));
                            });

                    // 태그-게시글 연결
                    PostTagging postTagging = new PostTagging(post, postTag);

                    // 복합키 설정
                    if (postTagging.getId() == null) {
                        postTagging.setId(new PostTagging.PostTaggingId(post.getId(), postTag.getId()));
                    }

                    postTaggingRepository.save(postTagging);
                    log.debug("PostTagging 저장 완료: {} - {}", post.getId(), postTag.getId());

                } catch (Exception e) {
                    log.error("태그 저장 중 오류 발생 - 태그: {}, 오류: {}", cleanTagText, e.getMessage(), e);
                    // 태그 저장 실패가 전체 게시글 저장을 방해하지 않도록 예외를 던지지 않음
                }
            }
        }

        log.debug("태그 저장 완료");
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
                    return userRepository.findById(userId).orElse(null);
                }
            }
            return null;
        } catch (Exception e) {
            log.warn("현재 사용자 조회 실패: {}", e.getMessage());
            return null;
        }
    }
}