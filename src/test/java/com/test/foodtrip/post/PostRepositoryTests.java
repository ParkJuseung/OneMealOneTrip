package com.test.foodtrip.post;

import com.test.foodtrip.domain.post.dto.PageRequestDTO;
import com.test.foodtrip.domain.post.dto.PageResultDTO;
import com.test.foodtrip.domain.post.dto.PostDTO;
import com.test.foodtrip.domain.post.entity.Post;
import com.test.foodtrip.domain.post.repository.PostRepository;
import com.test.foodtrip.domain.post.service.PostService;
import com.test.foodtrip.domain.user.entity.User;
import com.test.foodtrip.domain.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.stream.IntStream;

@SpringBootTest
public class PostRepositoryTests {

    @Autowired
    PostRepository postRepository;

    @Autowired
    UserRepository userRepository;
    @Autowired
    private ServerProperties serverProperties;
    @Autowired
    private PostService postService;

    @Test
    @DisplayName("Repository 연동 확인하는 테스트")
    public void connClass(){
        System.out.println(postRepository.getClass().getName());
        System.out.println(userRepository.getClass().getName());
    }

    @Test
    @DisplayName("insert 확인하는 테스트")
    public void insertClass(){
        Optional<User> user = userRepository.findById(1L);

        if(user.isPresent()) {

            Post post = Post.builder()
                    .user(user.get())
                    .title("Test용")
                    .content("테스트용 포스트입니다.")
                    .viewCount(0)
                    .latitude(37.5665)
                    .longitude(126.9780)
                    .placeName("서울특별시")
                    .createdAt(LocalDateTime.now())
                    .build();

            postRepository.save(post);
        }
    }

//    @Test
//    @DisplayName("POST 더미 생성 테스트")
//    public void testinsertBummies(){
//
//        User user = userRepository.getById(1L);
//
//        IntStream.rangeClosed(1, 100).forEach(i -> {
//            Post post = Post.builder()
//                    .user(user)
//                    .title("Test["+i+"]")
//                    .content("테스트용 포스트입니다.")
//                    .viewCount(0)
//                    .latitude(37.5665)
//                    .longitude(126.9780)
//                    .placeName("서울특별시")
//                    .createdAt(LocalDateTime.now())
//                    .build();
//
//            postRepository.save(post);
//
//        });
//    }


    @Test
    @DisplayName("POST 1번 게시물 조회 테스트")
    public void testSelcet() {
        Long id = 1L;

        Optional<Post> post = postRepository.findById(id);
        System.out.println(post);
    }

    @Test
    @DisplayName("POST 1번 게시물 수정 테스트")
    public void testUpdate(){
        Post post = Post.builder()
                .id(1L)
                .title("Test 수정작업")
                .content("테스트용 수정된 포스트입니다.")
                .viewCount(1)
                .createdAt(LocalDateTime.now())
                .build();

        postRepository.save(post);
    }


    @Test
    @DisplayName("Post 게시물 삭제 테스트")
    public void testDelete(){
        Long id = 102L;
        postRepository.deleteById(id);
    }

    @Test
    @DisplayName("페이지네이션 테스트")
    public void testPageDefault(){
        Pageable pageable = PageRequest.of(0,10);
        Page<Post> result = postRepository.findAll(pageable);
        System.out.println(result);

    }

    @Test
    @DisplayName("페이지네이션 생성일 역순으로 정렬")
    public void testPageSort(){
        Sort sort = Sort.by("createdAt").descending();
        Pageable pageable = PageRequest.of(0,10, sort);
        Page<Post> result = postRepository.findAll(pageable);

        result.get().forEach(post -> {
            System.out.println(post.getTitle());
        });
    }

    @Test
    @DisplayName("페이지 테스트")
    public void testList(){
        PageRequestDTO pageRequestDTO = PageRequestDTO.builder().page(1).size(10).build();
        PageResultDTO<PostDTO, Post> resultDTO = postService.getList(pageRequestDTO);
        for(PostDTO postDTO : resultDTO.getDtoList()){
            System.out.println(postDTO);
        }
    }


}
