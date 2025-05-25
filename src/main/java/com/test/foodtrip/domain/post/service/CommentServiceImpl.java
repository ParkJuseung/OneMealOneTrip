package com.test.foodtrip.domain.post.service;

import com.test.foodtrip.domain.post.dto.*;
import com.test.foodtrip.domain.post.entity.Comment;
import com.test.foodtrip.domain.post.entity.CommentReaction;
import com.test.foodtrip.domain.post.entity.Post;
import com.test.foodtrip.domain.post.entity.enums.DeleteStatus;
import com.test.foodtrip.domain.post.entity.enums.ReactionType;
import com.test.foodtrip.domain.post.repository.CommentReactionRepository;
import com.test.foodtrip.domain.post.repository.CommentRepository;
import com.test.foodtrip.domain.post.repository.PostRepository;
import com.test.foodtrip.domain.user.entity.User;
import com.test.foodtrip.domain.user.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final CommentReactionRepository commentReactionRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

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

    // Entity to DTO 변환 메서드 구현
    @Override
    public CommentDTO entityToDto(Comment comment) {
        CommentDTO dto = CommentDTO.builder()
                .id(comment.getId())
                .postId(comment.getPost().getId())
                .userId(comment.getUser().getId())
                .userNickname(comment.getUser().getNickname())
                .userProfileImage(comment.getUser().getProfileImage())
                .content(comment.getContent())
                .parentId(comment.getParent() != null ? comment.getParent().getId() : null)
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .isDeleted(comment.getIsDeleted())
                .hasReplies(!comment.getReplies().isEmpty())
                .build();

        // 반응 정보는 별도로 계산
        Long likeCount = commentReactionRepository.countByCommentAndReactionType(comment, ReactionType.LIKE);
        Long dislikeCount = commentReactionRepository.countByCommentAndReactionType(comment, ReactionType.DISLIKE);

        dto.setLikeCount(likeCount);
        dto.setDislikeCount(dislikeCount);
        dto.setIsPopular(likeCount >= 5);
        dto.setIsBlinded(dislikeCount >= 10);

        // 현재 사용자의 반응 확인
        User currentUser = getCurrentUser();
        if (currentUser != null) {
            boolean isLiked = commentReactionRepository.existsByCommentAndUserAndReactionType(
                    comment, currentUser, ReactionType.LIKE);
            boolean isDisliked = commentReactionRepository.existsByCommentAndUserAndReactionType(
                    comment, currentUser, ReactionType.DISLIKE);

            dto.setIsLikedByCurrentUser(isLiked);
            dto.setIsDislikedByCurrentUser(isDisliked);
        } else {
            dto.setIsLikedByCurrentUser(false);
            dto.setIsDislikedByCurrentUser(false);
        }

        return dto;
    }

    @Override
    public CommentDTO createComment(CommentCreateRequest request) {
        // 현재 로그인된 사용자 가져오기
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            throw new IllegalStateException("로그인된 사용자를 찾을 수 없습니다.");
        }

        // 게시글 조회
        Post post = postRepository.findById(request.getPostId())
                .orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다."));

        // 부모 댓글 조회 (대댓글인 경우)
        Comment parent = null;
        if (request.getParentId() != null) {
            parent = commentRepository.findById(request.getParentId())
                    .orElseThrow(() -> new RuntimeException("부모 댓글을 찾을 수 없습니다."));
        }

        // 댓글 생성 (현재 로그인된 사용자로)
        Comment comment = new Comment(post, currentUser, request.getContent(), parent);
        commentRepository.save(comment);

        return entityToDto(comment);
    }

    @Override
    public CommentDTO updateComment(Long commentId, CommentUpdateRequest request) {
        // 현재 로그인된 사용자 가져오기
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            throw new IllegalStateException("로그인된 사용자를 찾을 수 없습니다.");
        }

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("댓글을 찾을 수 없습니다."));

        // 작성자 확인 (현재 로그인된 사용자와 비교)
        if (!comment.getUser().getId().equals(currentUser.getId())) {
            throw new RuntimeException("댓글 수정 권한이 없습니다.");
        }

        // 삭제된 댓글은 수정할 수 없음
        if (comment.getIsDeleted() == DeleteStatus.Y) {
            throw new RuntimeException("삭제된 댓글은 수정할 수 없습니다.");
        }

        comment.setContent(request.getContent());
        commentRepository.save(comment);

        return entityToDto(comment);
    }

    @Override
    public void deleteComment(Long commentId) {
        // 현재 로그인된 사용자 가져오기
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            throw new IllegalStateException("로그인된 사용자를 찾을 수 없습니다.");
        }

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("댓글을 찾을 수 없습니다."));

        // 작성자 본인이거나 관리자만 삭제 가능
        if (!comment.getUser().getId().equals(currentUser.getId()) &&
                !"ADMIN".equals(currentUser.getRole())) {
            throw new RuntimeException("댓글 삭제 권한이 없습니다.");
        }

        // 논리적 삭제 (하위 댓글이 있을 수 있으므로)
        comment.delete();
        commentRepository.save(comment);
    }

    @Override
    @Transactional(readOnly = true)
    public CommentDTO getComment(Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("댓글을 찾을 수 없습니다."));
        return entityToDto(comment);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResultDTO<CommentDTO, Comment> getCommentsByPostId(Long postId, Pageable pageable) {
        // 최상위 댓글만 조회 (대댓글은 별도로 로드)
        Page<Comment> comments = commentRepository.findTopLevelCommentsByPostId(postId, pageable);

        Function<Comment, CommentDTO> fn = this::entityToDto;

        return new PageResultDTO<>(comments, fn);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentDTO> getPopularComments(Long postId) {
        // 단순화: 모든 댓글을 조회한 후 Java에서 필터링
        List<Comment> allComments = commentRepository.findAllActiveCommentsByPostId(postId);

        return allComments.stream()
                .filter(comment -> comment.getParent() == null) // 최상위 댓글만
                .map(this::entityToDto)
                .filter(dto -> dto.getLikeCount() >= 5) // 인기 댓글 필터링
                .sorted((a, b) -> Long.compare(b.getLikeCount(), a.getLikeCount())) // 좋아요 순 정렬
                .limit(3) // 최대 3개
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Long getTotalCommentCount(Long postId) {
        return commentRepository.countByPostIdAndIsDeleted(postId, DeleteStatus.N);
    }

    @Override
    public CommentReactionResultDTO toggleReaction(Long commentId, CommentReactionRequest request) {
        // 현재 로그인된 사용자 가져오기
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            throw new IllegalStateException("로그인된 사용자를 찾을 수 없습니다.");
        }

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("댓글을 찾을 수 없습니다."));

        // 기존 반응 조회 (현재 로그인된 사용자로)
        Optional<CommentReaction> existingReaction =
                commentReactionRepository.findByCommentAndUser(comment, currentUser);

        String action;
        boolean isToggled;

        if (existingReaction.isPresent()) {
            CommentReaction reaction = existingReaction.get();
            if (reaction.getReactionType() == request.getReactionType()) {
                // 같은 반응이면 제거
                commentReactionRepository.delete(reaction);
                action = "REMOVED";
                isToggled = true;
            } else {
                // 다른 반응이면 변경
                reaction.setReactionType(request.getReactionType());
                commentReactionRepository.save(reaction);
                action = "CHANGED";
                isToggled = true;
            }
        } else {
            // 새로운 반응 추가 (현재 로그인된 사용자로)
            CommentReaction newReaction = new CommentReaction(comment, currentUser, request.getReactionType());
            commentReactionRepository.save(newReaction);
            action = "ADDED";
            isToggled = true;
        }

        // 반응 통계 다시 계산
        Long likeCount = commentReactionRepository.countByCommentAndReactionType(comment, ReactionType.LIKE);
        Long dislikeCount = commentReactionRepository.countByCommentAndReactionType(comment, ReactionType.DISLIKE);

        boolean isLiked = commentReactionRepository.existsByCommentAndUserAndReactionType(
                comment, currentUser, ReactionType.LIKE);
        boolean isDisliked = commentReactionRepository.existsByCommentAndUserAndReactionType(
                comment, currentUser, ReactionType.DISLIKE);

        return CommentReactionResultDTO.builder()
                .isToggled(isToggled)
                .action(action)
                .likeCount(likeCount)
                .dislikeCount(dislikeCount)
                .isLikedByCurrentUser(isLiked)
                .isDislikedByCurrentUser(isDisliked)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public CommentStatisticsDTO getCommentStatistics(Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("댓글을 찾을 수 없습니다."));

        Long likeCount = commentReactionRepository.countByCommentAndReactionType(comment, ReactionType.LIKE);
        Long dislikeCount = commentReactionRepository.countByCommentAndReactionType(comment, ReactionType.DISLIKE);
        Long replyCount = commentRepository.countByParentAndIsDeleted(comment, DeleteStatus.N);

        return CommentStatisticsDTO.builder()
                .commentId(commentId)
                .likeCount(likeCount)
                .dislikeCount(dislikeCount)
                .replyCount(replyCount)
                .isPopular(likeCount >= 5)
                .isBlinded(dislikeCount >= 10)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public CommentReactionSummaryDTO getUserReactionSummary(Long commentId, Long userId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("댓글을 찾을 수 없습니다."));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        Long likeCount = commentReactionRepository.countByCommentAndReactionType(comment, ReactionType.LIKE);
        Long dislikeCount = commentReactionRepository.countByCommentAndReactionType(comment, ReactionType.DISLIKE);

        boolean userLiked = commentReactionRepository.existsByCommentAndUserAndReactionType(
                comment, user, ReactionType.LIKE);
        boolean userDisliked = commentReactionRepository.existsByCommentAndUserAndReactionType(
                comment, user, ReactionType.DISLIKE);

        return CommentReactionSummaryDTO.builder()
                .commentId(commentId)
                .likeCount(likeCount)
                .dislikeCount(dislikeCount)
                .userLiked(userLiked)
                .userDisliked(userDisliked)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentDTO> getReplies(Long parentCommentId) {
        List<Comment> replies = commentRepository.findRepliesByParentId(parentCommentId);
        return replies.stream()
                .map(this::entityToDto)
                .collect(Collectors.toList());
    }
}