package com.test.foodtrip.domain.post.service;

import com.test.foodtrip.domain.post.dto.CommentCreateRequest;
import com.test.foodtrip.domain.post.dto.CommentDTO;
import com.test.foodtrip.domain.post.dto.CommentUpdateRequest;
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
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentService {

    private final CommentRepository commentRepository;
    private final CommentReactionRepository commentReactionRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    /**
     * 게시글에 댓글 생성
     */
    @Transactional
    public CommentDTO createComment(Long postId, CommentCreateRequest request, Long userId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        Comment comment;

        // 부모 댓글 ID가 있으면 대댓글 생성
        if (request.getParentId() != null) {
            Comment parentComment = commentRepository.findById(request.getParentId())
                    .orElseThrow(() -> new IllegalArgumentException("부모 댓글을 찾을 수 없습니다."));

            comment = new Comment(post, user, request.getContent(), parentComment);
        } else {
            comment = new Comment(post, user, request.getContent());
        }

        Comment savedComment = commentRepository.save(comment);
        return CommentDTO.fromEntity(savedComment, false, false);
    }

    /**
     * 게시글의 댓글 목록 조회 (페이징)
     */
    public Page<CommentDTO> getCommentsByPost(Long postId, int page, int size, Long currentUserId) {
        Pageable pageable = PageRequest.of(page, size);
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));

        // 인기 댓글 먼저 가져오기
        List<Comment> popularComments = commentRepository.findTopPopularComments(post, PageRequest.of(0, 3));

        // 일반 댓글 페이징 처리하여 가져오기
        Page<Comment> comments = commentRepository.findByPostAndParentIsNullOrderByCreatedAtDesc(post, pageable);

        return comments.map(comment -> {
            boolean isPopular = popularComments.contains(comment);

            // 현재 사용자의 좋아요/싫어요 여부 확인
            boolean isLiked = false;
            boolean isDisliked = false;

            if (currentUserId != null) {
                Optional<CommentReaction> userReaction = commentReactionRepository
                        .findByCommentAndUser(comment, userRepository.getReferenceById(currentUserId));

                if (userReaction.isPresent()) {
                    isLiked = userReaction.get().getReactionType() == ReactionType.LIKE;
                    isDisliked = userReaction.get().getReactionType() == ReactionType.DISLIKE;
                }
            }

            CommentDTO dto = CommentDTO.fromEntity(comment, isLiked, isDisliked);

            // 대댓글 가져오기
            List<CommentDTO> replyDtos = commentRepository.findByParentOrderByCreatedAtAsc(comment)
                    .stream()
                    .map(reply -> {
                        boolean replyLiked = false;
                        boolean replyDisliked = false;

                        if (currentUserId != null) {
                            Optional<CommentReaction> replyReaction = commentReactionRepository
                                    .findByCommentAndUser(reply, userRepository.getReferenceById(currentUserId));

                            if (replyReaction.isPresent()) {
                                replyLiked = replyReaction.get().getReactionType() == ReactionType.LIKE;
                                replyDisliked = replyReaction.get().getReactionType() == ReactionType.DISLIKE;
                            }
                        }

                        return CommentDTO.fromEntity(reply, replyLiked, replyDisliked);
                    })
                    .collect(Collectors.toList());

            dto.setReplies(replyDtos);
            return dto;
        });
    }

    /**
     * 댓글 수정
     */
    @Transactional
    public CommentDTO updateComment(Long commentId, CommentUpdateRequest request, Long userId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("댓글을 찾을 수 없습니다."));

        // 작성자 확인
        if (!comment.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("댓글 작성자만 수정할 수 있습니다.");
        }

        // 삭제된 댓글인지 확인
        if (comment.isDeleted()) {
            throw new IllegalArgumentException("삭제된 댓글은 수정할 수 없습니다.");
        }

        comment.setContent(request.getContent());
        Comment updatedComment = commentRepository.save(comment);

        // 현재 사용자의 좋아요/싫어요 여부 확인
        User user = userRepository.getReferenceById(userId);
        Optional<CommentReaction> userReaction = commentReactionRepository.findByCommentAndUser(updatedComment, user);
        boolean isLiked = userReaction.isPresent() && userReaction.get().getReactionType() == ReactionType.LIKE;
        boolean isDisliked = userReaction.isPresent() && userReaction.get().getReactionType() == ReactionType.DISLIKE;

        return CommentDTO.fromEntity(updatedComment, isLiked, isDisliked);
    }

    /**
     * 댓글 삭제
     */
    @Transactional
    public void deleteComment(Long commentId, Long userId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("댓글을 찾을 수 없습니다."));

        // 작성자 또는 관리자 확인 (관리자 권한 체크 로직은 실제 구현에 맞게 수정 필요)
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        boolean isAdmin = "ADMIN".equals(user.getRole());
        boolean isAuthor = comment.getUser().getId().equals(userId);

        if (!isAdmin && !isAuthor) {
            throw new IllegalArgumentException("댓글 작성자 또는 관리자만 삭제할 수 있습니다.");
        }

        // 댓글 삭제 처리 (실제로는 삭제 플래그만 설정)
        comment.setIsDeleted(DeleteStatus.Y);
        commentRepository.save(comment);
    }

    /**
     * 댓글 좋아요/싫어요 처리
     */
    @Transactional
    public CommentDTO reactToComment(Long commentId, ReactionType reactionType, Long userId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("댓글을 찾을 수 없습니다."));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 이미 존재하는 반응 확인
        Optional<CommentReaction> existingReaction = commentReactionRepository.findByCommentAndUser(comment, user);

        if (existingReaction.isPresent()) {
            CommentReaction reaction = existingReaction.get();
            // 같은 타입의 반응이면 취소
            if (reaction.getReactionType() == reactionType) {
                commentReactionRepository.delete(reaction);
                return CommentDTO.fromEntity(comment, false, false);
            } else {
                // 다른 타입의 반응이면 변경
                reaction.setReactionType(reactionType);
                commentReactionRepository.save(reaction);
                boolean isLiked = reactionType == ReactionType.LIKE;
                boolean isDisliked = reactionType == ReactionType.DISLIKE;
                return CommentDTO.fromEntity(comment, isLiked, isDisliked);
            }
        } else {
            // 새로운 반응 생성
            CommentReaction reaction = new CommentReaction(comment, user, reactionType);
            commentReactionRepository.save(reaction);
            boolean isLiked = reactionType == ReactionType.LIKE;
            boolean isDisliked = reactionType == ReactionType.DISLIKE;
            return CommentDTO.fromEntity(comment, isLiked, isDisliked);
        }
    }

    /**
     * 댓글 상세 조회
     */
    public CommentDTO getComment(Long commentId, Long currentUserId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("댓글을 찾을 수 없습니다."));

        // 현재 사용자의 좋아요/싫어요 여부 확인
        boolean isLiked = false;
        boolean isDisliked = false;

        if (currentUserId != null) {
            Optional<CommentReaction> userReaction = commentReactionRepository
                    .findByCommentAndUser(comment, userRepository.getReferenceById(currentUserId));

            if (userReaction.isPresent()) {
                isLiked = userReaction.get().getReactionType() == ReactionType.LIKE;
                isDisliked = userReaction.get().getReactionType() == ReactionType.DISLIKE;
            }
        }

        return CommentDTO.fromEntity(comment, isLiked, isDisliked);
    }
}