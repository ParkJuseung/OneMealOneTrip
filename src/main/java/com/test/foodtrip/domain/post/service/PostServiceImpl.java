package com.test.foodtrip.domain.post.service;

import com.test.foodtrip.domain.post.dto.PageRequestDTO;
import com.test.foodtrip.domain.post.dto.PageResultDTO;
import com.test.foodtrip.domain.post.dto.PostDTO;
import com.test.foodtrip.domain.post.entity.Post;
import com.test.foodtrip.domain.post.repository.PostRepository;
import com.test.foodtrip.domain.user.entity.User;
import com.test.foodtrip.domain.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostService{

    private final UserRepository userRepository;
    private final PostRepository postRepository;

    @Override
    public Long create(PostDTO dto){
        //임시유저 사용
        Optional<User> user = userRepository.findById(1L);
        Post post = dtoToEntity(dto, user.get());
        postRepository.save(post);
        return post.getId();
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
        return result.isPresent()? entityToDto(result.get()) : null;
    }




}
