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
import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostService{

    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final PostTagRepository postTagRepository;
    private final PostTaggingRepository postTaggingRepository;

    @Override
    @Transactional
    public Long create(PostDTO dto){
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
    @Transactional
    public PageResultDTO<PostDTO, Post> getList(PageRequestDTO requestDTO){
        Pageable pageable = requestDTO.getPageable(Sort.by("createdAt").descending());
        Page<Post> result = postRepository.findAll(pageable);

        Function<Post, PostDTO> fn = (entity -> {
            PostDTO dto = entityToDto(entity);
            // 태그 정보 추가
            List<String> tags = postTaggingRepository.findTagsByPostId(entity.getId());
            dto.setTags(tags);
            return dto;
        });

        return new PageResultDTO<>(result, fn);
    }

    @Override
    public PostDTO read(Long id){
        Optional<Post> result = postRepository.findById(id);
        if (result.isPresent()) {
            Post post = result.get();
            // 조회수 증가
            post.setViewCount(post.getViewCount() + 1);
            postRepository.save(post);

            PostDTO dto = entityToDto(post);
            // 태그 정보 추가
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

            // 기존 태그 삭제 후 새 태그 추가
            List<PostTagging> existingTaggings = postTaggingRepository.findByPostId(dto.getId());
            postTaggingRepository.deleteAll(existingTaggings);

            if (dto.getTags() != null && !dto.getTags().isEmpty()) {
                saveTags(post, dto.getTags());
            }
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

    // 태그 저장 메서드 - 수정된 버전
    private void saveTags(Post post, List<String> tagTexts) {
        System.out.println("=== 태그 저장 시작 ===");
        System.out.println("Post ID: " + post.getId());
        System.out.println("태그 개수: " + (tagTexts != null ? tagTexts.size() : 0));

        if (tagTexts == null || tagTexts.isEmpty()) {
            System.out.println("태그가 없어서 저장하지 않음");
            return;
        }

        for (String tagText : tagTexts) {
            if (tagText != null && !tagText.trim().isEmpty()) {
                String cleanTagText = tagText.trim();
                System.out.println("처리 중인 태그: " + cleanTagText);

                try {
                    // 기존 태그 찾기 또는 새로 생성
                    PostTag postTag = postTagRepository.findByTagText(cleanTagText)
                            .orElseGet(() -> {
                                System.out.println("새 태그 생성: " + cleanTagText);
                                PostTag newTag = new PostTag(cleanTagText);
                                return postTagRepository.save(newTag);
                            });

                    System.out.println("PostTag ID: " + postTag.getId());

                    // 태그-게시글 연결
                    PostTagging postTagging = new PostTagging(post, postTag);

                    // ID를 수동으로 설정 (Post와 PostTag 모두 ID가 있는 상태)
                    if (postTagging.getId() == null) {
                        postTagging.setId(new PostTagging.PostTaggingId(post.getId(), postTag.getId()));
                    }

                    PostTagging saved = postTaggingRepository.save(postTagging);
                    System.out.println("PostTagging 저장 완료: " + saved.getId().getPostId() + " - " + saved.getId().getPostTagId());

                } catch (Exception e) {
                    System.err.println("태그 저장 중 오류: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }
        System.out.println("=== 태그 저장 완료 ===");
    }

    // 현재 로그인된 사용자를 세션에서 가져오는 메서드
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