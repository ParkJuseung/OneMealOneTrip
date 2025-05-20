package com.test.foodtrip.domain.post.service;

import com.test.foodtrip.domain.post.dto.PageRequestDTO;
import com.test.foodtrip.domain.post.dto.PageResultDTO;
import com.test.foodtrip.domain.post.dto.PostDTO;
import com.test.foodtrip.domain.post.entity.Post;
import com.test.foodtrip.domain.post.entity.PostTag;
import com.test.foodtrip.domain.user.entity.User;

import java.util.List;

public interface PostService {
    Long create(PostDTO dto);
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
                .build();

        post.setUser(user);

        return post;
    }


    default PostDTO entityToDto(Post post){
        return PostDTO.builder()
                .id(post.getId())
                .title(post.getTitle())
                .content(post.getContent())
                .viewCount(post.getViewCount())
                .latitude(post.getLatitude())
                .longitude(post.getLongitude())
                .placeName(post.getPlaceName())
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .userId(post.getUser() != null ? post.getUser().getId() : null)
                .imageUrls(post.getImages().stream()
                        .map(image -> image.getImageUrl())
                        .toList())
                .tags(post.getTags().stream()
                        .map(tagging -> tagging.getPostTag().getTagText())
                        .toList())
                .likeCount(post.getLikes().size())
                .commentCount(post.getComments().size())
                .build();
    }

}
