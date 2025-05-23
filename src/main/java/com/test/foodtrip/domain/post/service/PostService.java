package com.test.foodtrip.domain.post.service;

import com.test.foodtrip.domain.post.dto.PageRequestDTO;
import com.test.foodtrip.domain.post.dto.PageResultDTO;
import com.test.foodtrip.domain.post.dto.PostDTO;
import com.test.foodtrip.domain.post.entity.Post;
import com.test.foodtrip.domain.post.entity.PostTag;
import com.test.foodtrip.domain.user.entity.User;
import jakarta.transaction.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public interface PostService {

    @Transactional
    Long create(PostDTO dto, MultipartFile[] images);

    PageResultDTO<PostDTO, Post> getList(PageRequestDTO requestDTO);
    PostDTO read(Long id);

//    default Post dtoToEntity(PostDTO dto, User user, List<PostTag> tagList) {
//        Post post = Post.builder()
//                .title(dto.getTitle())
//                .content(dto.getContent())
//                .viewCount(dto.getViewCount() != null ? dto.getViewCount() : 0)
//                .latitude(dto.getLatitude())
//                .longitude(dto.getLongitude())
//                .placeName(dto.getPlaceName())
//                .build();
//
//        post.setUser(user);
//
//        if (tagList != null) {
//            for (PostTag tag : tagList) {
//                post.addTag(tag);
//            }
//        }
//
//        return post;
//    }


    default Post dtoToEntity(PostDTO dto, User user) {
        Post post = Post.builder()
                .title(dto.getTitle())
                .content(dto.getContent())
                .viewCount(dto.getViewCount() != null ? dto.getViewCount() : 0)
                .latitude(dto.getLatitude())
                .longitude(dto.getLongitude())
                .placeName(dto.getPlaceName())
                .placeAddress(dto.getPlaceAddress())
                .placeId(dto.getPlaceId())
                .build();

        post.setUser(user);

        if (post.getImages() == null) {
            post.setImages(new ArrayList<>());
        }

        return post;
    }

    /**
     * ✅ N+1 문제 해결을 위한 안전한 entityToDto
     * 연관관계는 초기화된 경우에만 접근
     */
    default PostDTO entityToDto(Post post){
        PostDTO.PostDTOBuilder builder = PostDTO.builder()
                .id(post.getId())
                .title(post.getTitle())
                .content(post.getContent())
                .viewCount(post.getViewCount())
                .latitude(post.getLatitude())
                .longitude(post.getLongitude())
                .placeName(post.getPlaceName())
                .placeAddress(post.getPlaceAddress())
                .placeId(post.getPlaceId())
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .userId(post.getUser() != null ? post.getUser().getId() : null);

        try {
            // ✅ 이미지 정보 (초기화된 경우만 접근)
            if (post.getImages() != null && org.hibernate.Hibernate.isInitialized(post.getImages())) {
                builder.imageUrls(post.getImages().stream()
                        .map(image -> image.getImageUrl())
                        .toList());
            } else {
                builder.imageUrls(Collections.emptyList());
            }

            // ✅ 태그 정보 (초기화된 경우만 접근)
            if (post.getTags() != null && org.hibernate.Hibernate.isInitialized(post.getTags())) {
                builder.tags(post.getTags().stream()
                        .map(tagging -> tagging.getPostTag().getTagText())
                        .toList());
            } else {
                builder.tags(Collections.emptyList());
            }

            // ✅ 좋아요 수 (초기화된 경우만 접근)
            if (post.getLikes() != null && org.hibernate.Hibernate.isInitialized(post.getLikes())) {
                builder.likeCount(post.getLikes().size());
            } else {
                builder.likeCount(0);
            }

            // ✅ 댓글 수 (초기화된 경우만 접근)
            if (post.getComments() != null && org.hibernate.Hibernate.isInitialized(post.getComments())) {
                builder.commentCount(post.getComments().size());
            } else {
                builder.commentCount(0);
            }

        } catch (Exception e) {
            // Lazy Loading 예외 발생 시 기본값 설정
            builder.imageUrls(Collections.emptyList())
                    .tags(Collections.emptyList())
                    .likeCount(0)
                    .commentCount(0);
        }

        return builder.build();
    }

    void modify(PostDTO dto);
    void remove(Long id);
    void increaseViewCount(Long id);

}