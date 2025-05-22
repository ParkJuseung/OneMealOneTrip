package com.test.foodtrip.domain.post.service;

import com.test.foodtrip.domain.post.dto.PageRequestDTO;
import com.test.foodtrip.domain.post.dto.PageResultDTO;
import com.test.foodtrip.domain.post.dto.PostDTO;
import com.test.foodtrip.domain.post.entity.Post;
import com.test.foodtrip.domain.post.repository.PostRepository;
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

import java.util.Optional;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostService{

    private final UserRepository userRepository;
    private final PostRepository postRepository;

    @Override
    public Long create(PostDTO dto){
        // 현재 로그인된 사용자 가져오기
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            throw new IllegalStateException("로그인된 사용자를 찾을 수 없습니다.");
        }

        Post post = dtoToEntity(dto, currentUser);
        postRepository.save(post);
        return post.getId();
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
            // 세션이 없거나 다른 예외가 발생한 경우
            return null;
        }
    }

    @Override
    @Transactional
    public PageResultDTO<PostDTO, Post> getList(PageRequestDTO requestDTO){
        Pageable pageable = requestDTO.getPageable(Sort.by("createdAt").descending());

        Page<Post> result = postRepository.findAll(pageable);

        Function<Post, PostDTO> fn = (entity -> entityToDto(entity));
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
            return entityToDto(post);
        }
        return null;
    }

    @Override
    @org.springframework.transaction.annotation.Transactional
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
        }
    }

    @Override
    @org.springframework.transaction.annotation.Transactional
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

            postRepository.deleteById(id);
        }
    }

    @Override
    @org.springframework.transaction.annotation.Transactional
    public void increaseViewCount(Long id) {
        Optional<Post> result = postRepository.findById(id);
        if (result.isPresent()) {
            Post post = result.get();
            post.setViewCount(post.getViewCount() + 1);
            postRepository.save(post);
        }
    }
}