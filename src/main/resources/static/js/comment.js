/**
 * 댓글 관련 JavaScript 기능
 */

class CommentManager {
    constructor(postId) {
        this.postId = postId;
        this.currentPage = 0;
        this.pageSize = 20;
        this.isLoading = false;
        this.hasMoreComments = true;

        this.init();
    }

    init() {
        this.bindEvents();
        this.loadComments();
        this.loadPopularComments();
    }

    bindEvents() {
        // 댓글 작성
        $('.comment-submit').on('click', () => {
            this.createComment();
        });

        // 댓글 더보기
        $('.show-more-comments').on('click', () => {
            this.loadMoreComments();
        });

        // 댓글 좋아요/싫어요 이벤트는 동적으로 바인딩
        $(document).on('click', '.comment-like-btn', (e) => {
            this.toggleReaction(e, 'LIKE');
        });

        $(document).on('click', '.comment-dislike-btn', (e) => {
            this.toggleReaction(e, 'DISLIKE');
        });

        // 대댓글 작성
        $(document).on('click', '.reply-btn', (e) => {
            this.showReplyForm(e);
        });

        // 댓글 수정
        $(document).on('click', '.comment-edit-btn', (e) => {
            this.showEditForm(e);
        });

        // 댓글 삭제
        $(document).on('click', '.comment-delete-btn', (e) => {
            this.deleteComment(e);
        });

        // 대댓글 토글
        $(document).on('click', '.toggle-replies-btn', (e) => {
            this.toggleReplies(e);
        });
    }

    // 댓글 목록 로드
    async loadComments() {
        if (this.isLoading) return;

        this.isLoading = true;
        try {
            const response = await fetch(`/api/comments/post/${this.postId}?page=${this.currentPage}&size=${this.pageSize}`);
            const result = await response.json();

            console.log('API 전체 응답:', result); // 디버깅용

            if (result.success) {
                // PageResultDTO 구조에 맞게 수정
                let comments = [];
                if (result.data && result.data.dtoList) {
                    comments = result.data.dtoList;
                    this.hasMoreComments = result.data.next;
                    this.updateCommentCount(result.data.totalCount);
                } else if (Array.isArray(result.data)) {
                    // 직접 배열인 경우
                    comments = result.data;
                } else {
                    console.warn('예상과 다른 응답 구조:', result);
                    comments = [];
                }

                console.log('추출된 댓글 리스트:', comments); // 디버깅용

                if (comments.length > 0) {
                    this.renderComments(comments);
                } else {
                    console.log('댓글이 없습니다');
                    if (this.currentPage === 0) {
                        $('.comments-list').append('<p class="no-comments">아직 댓글이 없습니다.</p>');
                    }
                }
            } else {
                console.error('API 응답 실패:', result.message);
                this.showToast('댓글을 불러오는데 실패했습니다.', 'error');
            }
        } catch (error) {
            console.error('댓글 로드 실패:', error);
            this.showToast('댓글을 불러오는데 실패했습니다.', 'error');
        } finally {
            this.isLoading = false;
        }
    }

    // 인기 댓글 로드
    async loadPopularComments() {
        try {
            const response = await fetch(`/api/comments/post/${this.postId}/popular`);
            const result = await response.json();

            if (result.success && result.data.popularComments.length > 0) {
                this.renderPopularComments(result.data.popularComments);
            }
        } catch (error) {
            console.error('인기 댓글 로드 실패:', error);
        }
    }

    // 더 많은 댓글 로드
    async loadMoreComments() {
        if (!this.hasMoreComments || this.isLoading) return;

        this.currentPage++;
        await this.loadComments();
    }

    // 댓글 작성
    async createComment(parentId = null) {
        const content = parentId ?
            $(`.reply-form[data-parent-id="${parentId}"] .reply-input`).val().trim() :
            $('.comment-input').val().trim();

        if (!content) {
            this.showToast('댓글 내용을 입력해주세요.', 'warning');
            return;
        }

        try {
            const response = await fetch('/api/comments', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({
                    postId: this.postId,
                    userId: 1, // 임시 사용자 ID
                    content: content,
                    parentId: parentId
                })
            });

            const result = await response.json();

            if (result.success) {
                if (parentId) {
                    // 대댓글인 경우 해당 댓글의 대댓글 목록에 추가
                    this.addReplyToComment(parentId, result.data);
                    $(`.reply-form[data-parent-id="${parentId}"]`).hide();
                    $(`.reply-form[data-parent-id="${parentId}"] .reply-input`).val('');
                } else {
                    // 일반 댓글인 경우 댓글 목록 상단에 추가
                    this.prependComment(result.data);
                    $('.comment-input').val('');
                }
                this.showToast('댓글이 등록되었습니다.', 'success');
                this.updateCommentCount();
            } else {
                this.showToast(result.message || '댓글 작성에 실패했습니다.', 'error');
            }
        } catch (error) {
            console.error('댓글 작성 실패:', error);
            this.showToast('댓글 작성에 실패했습니다.', 'error');
        }
    }

    // 댓글 수정
    async updateComment(commentId, content) {
        try {
            const response = await fetch(`/api/comments/${commentId}`, {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({
                    userId: 1, // 임시 사용자 ID
                    content: content
                })
            });

            const result = await response.json();

            if (result.success) {
                this.updateCommentInDOM(commentId, result.data);
                this.showToast('댓글이 수정되었습니다.', 'success');
            } else {
                this.showToast(result.message || '댓글 수정에 실패했습니다.', 'error');
            }
        } catch (error) {
            console.error('댓글 수정 실패:', error);
            this.showToast('댓글 수정에 실패했습니다.', 'error');
        }
    }

    // 댓글 삭제
    async deleteComment(event) {
        const commentId = $(event.target).closest('.comment-item').data('comment-id');

        if (!confirm('정말 이 댓글을 삭제하시겠습니까?')) {
            return;
        }

        try {
            const response = await fetch(`/api/comments/${commentId}`, {
                method: 'DELETE'
            });

            const result = await response.json();

            if (result.success) {
                this.markCommentAsDeleted(commentId);
                this.showToast('댓글이 삭제되었습니다.', 'success');
                this.updateCommentCount();
            } else {
                this.showToast(result.message || '댓글 삭제에 실패했습니다.', 'error');
            }
        } catch (error) {
            console.error('댓글 삭제 실패:', error);
            this.showToast('댓글 삭제에 실패했습니다.', 'error');
        }
    }

    // 댓글 반응 토글 (좋아요/싫어요)
    async toggleReaction(event, reactionType) {
        event.preventDefault();
        const commentId = $(event.target).closest('.comment-item').data('comment-id');

        try {
            const response = await fetch(`/api/comments/${commentId}/reaction`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({
                    userId: 1, // 임시 사용자 ID
                    reactionType: reactionType
                })
            });

            const result = await response.json();

            if (result.success) {
                this.updateReactionUI(commentId, result.data);
            } else {
                this.showToast(result.message || '반응 처리에 실패했습니다.', 'error');
            }
        } catch (error) {
            console.error('반응 처리 실패:', error);
            this.showToast('반응 처리에 실패했습니다.', 'error');
        }
    }

    // 댓글 렌더링 - 개선된 버전
    renderComments(comments) {
        console.log('렌더링 시작, 댓글 개수:', comments.length); // 디버깅용
        console.log('렌더링할 댓글들:', comments); // 디버깅용

        const commentsContainer = $('.comments-list');
        console.log('댓글 컨테이너 찾음:', commentsContainer.length > 0); // 디버깅용

        if (commentsContainer.length === 0) {
            console.error('댓글 컨테이너(.comments-list)를 찾을 수 없습니다!');
            return;
        }

        comments.forEach((comment, index) => {
            console.log(`댓글 ${index + 1} 렌더링 중:`, comment); // 디버깅용
            try {
                const commentHtml = this.createCommentHTML(comment);
                console.log(`댓글 ${index + 1} HTML 생성 완료`); // 디버깅용
                commentsContainer.append(commentHtml);
                console.log(`댓글 ${index + 1} DOM에 추가 완료`); // 디버깅용
            } catch (error) {
                console.error(`댓글 ${index + 1} 렌더링 실패:`, error);
            }
        });

        console.log('렌더링 완료, 현재 댓글 요소 수:', $('.comment-item').length); // 디버깅용

        // 더보기 버튼 업데이트
        if (!this.hasMoreComments) {
            $('.show-more-comments').text('더 이상 댓글이 없습니다').prop('disabled', true);
        } else {
            $('.show-more-comments').show();
        }
    }

    // 인기 댓글 렌더링
    renderPopularComments(popularComments) {
        if (popularComments.length === 0) return;

        const popularSection = `
            <div class="popular-comments-section">
                <h4 class="popular-comments-title">인기 댓글</h4>
                <div class="popular-comments-list">
                    ${popularComments.map(comment => this.createCommentHTML(comment, true)).join('')}
                </div>
                <div class="popular-comments-divider"></div>
            </div>
        `;

        $('.comments-list').prepend(popularSection);
    }

    // 댓글 HTML 생성
    createCommentHTML(comment, isPopular = false) {
        const isBlinded = comment.isBlinded;
        const displayContent = isBlinded ? '블라인드 처리된 댓글입니다' : comment.getDisplayContent();
        const popularBadge = isPopular ? '<span class="popular-badge">인기</span>' : '';

        return `
            <div class="comment-item ${isPopular ? 'popular-comment' : ''}" data-comment-id="${comment.id}">
                <img src="${comment.userProfileImage || '/api/placeholder/40/40'}" 
                     alt="${comment.userNickname}" class="comment-avatar">
                <div class="comment-content">
                    <div class="comment-header">
                        <span class="comment-author">${comment.userNickname}</span>
                        ${popularBadge}
                        <span class="comment-date">${this.formatDate(comment.createdAt)}</span>
                    </div>
                    <p class="comment-text">${displayContent}</p>
                    ${!isBlinded ? this.createCommentActionsHTML(comment) : ''}
                    ${comment.hasReplies ? this.createRepliesToggleHTML(comment) : ''}
                    <div class="replies-container" style="display: none;"></div>
                    <div class="reply-form" data-parent-id="${comment.id}" style="display: none;">
                        <textarea class="reply-input" placeholder="답글을 입력하세요..." rows="2"></textarea>
                        <div class="reply-actions">
                            <button class="reply-cancel-btn">취소</button>
                            <button class="reply-submit-btn" onclick="commentManager.createComment(${comment.id})">등록</button>
                        </div>
                    </div>
                </div>
            </div>
        `;
    }

    // 댓글 액션 버튼 HTML 생성
    createCommentActionsHTML(comment) {
        return `
            <div class="comment-actions">
                <div class="comment-action comment-like-btn ${comment.isLikedByCurrentUser ? 'active' : ''}">
                    <i class="fas fa-heart"></i>
                    <span class="like-count">${comment.likeCount || 0}</span>
                </div>
                <div class="comment-action comment-dislike-btn ${comment.isDislikedByCurrentUser ? 'active' : ''}">
                    <i class="fas fa-heart-broken"></i>
                    <span class="dislike-count">${comment.dislikeCount || 0}</span>
                </div>
                <div class="comment-action reply-btn">
                    <i class="far fa-comment"></i>
                    <span>답글</span>
                </div>
                <div class="comment-options">
                    <button class="comment-edit-btn">수정</button>
                    <button class="comment-delete-btn">삭제</button>
                </div>
            </div>
        `;
    }

    // 대댓글 토글 HTML 생성
    createRepliesToggleHTML(comment) {
        return `
            <div class="toggle-replies-btn" data-comment-id="${comment.id}">
                <i class="fas fa-chevron-down"></i>
                <span>답글 보기</span>
            </div>
        `;
    }

    // 반응 UI 업데이트
    updateReactionUI(commentId, reactionData) {
        const commentItem = $(`.comment-item[data-comment-id="${commentId}"]`);

        // 좋아요 버튼 업데이트
        const likeBtn = commentItem.find('.comment-like-btn');
        likeBtn.find('.like-count').text(reactionData.likeCount);
        likeBtn.toggleClass('active', reactionData.isLikedByCurrentUser);

        // 싫어요 버튼 업데이트
        const dislikeBtn = commentItem.find('.comment-dislike-btn');
        dislikeBtn.find('.dislike-count').text(reactionData.dislikeCount);
        dislikeBtn.toggleClass('active', reactionData.isDislikedByCurrentUser);
    }

    // 대댓글 토글
    async toggleReplies(event) {
        const commentId = $(event.target).closest('.toggle-replies-btn').data('comment-id');
        const repliesContainer = $(`.comment-item[data-comment-id="${commentId}"] .replies-container`);
        const toggleBtn = $(event.target).closest('.toggle-replies-btn');

        if (repliesContainer.is(':visible')) {
            // 대댓글 숨기기
            repliesContainer.hide();
            toggleBtn.find('i').removeClass('fa-chevron-up').addClass('fa-chevron-down');
            toggleBtn.find('span').text('답글 보기');
        } else {
            // 대댓글 로드 및 표시
            if (repliesContainer.children().length === 0) {
                await this.loadReplies(commentId);
            }
            repliesContainer.show();
            toggleBtn.find('i').removeClass('fa-chevron-down').addClass('fa-chevron-up');
            toggleBtn.find('span').text('답글 숨기기');
        }
    }

    // 대댓글 로드
    async loadReplies(parentCommentId) {
        try {
            const response = await fetch(`/api/comments/${parentCommentId}/replies`);
            const result = await response.json();

            if (result.success) {
                const repliesContainer = $(`.comment-item[data-comment-id="${parentCommentId}"] .replies-container`);
                const repliesHtml = result.data.map(reply => this.createReplyHTML(reply)).join('');
                repliesContainer.html(repliesHtml);
            }
        } catch (error) {
            console.error('대댓글 로드 실패:', error);
        }
    }

    // 대댓글 HTML 생성
    createReplyHTML(reply) {
        const displayContent = reply.isBlinded ? '블라인드 처리된 댓글입니다' : reply.getDisplayContent();

        return `
            <div class="comment-item reply-item" data-comment-id="${reply.id}">
                <img src="${reply.userProfileImage || '/api/placeholder/40/40'}" 
                     alt="${reply.userNickname}" class="comment-avatar">
                <div class="comment-content">
                    <div class="comment-header">
                        <span class="comment-author">${reply.userNickname}</span>
                        <span class="comment-date">${this.formatDate(reply.createdAt)}</span>
                    </div>
                    <p class="comment-text">${displayContent}</p>
                    ${!reply.isBlinded ? this.createCommentActionsHTML(reply) : ''}
                </div>
            </div>
        `;
    }

    // 답글 폼 표시
    showReplyForm(event) {
        const commentItem = $(event.target).closest('.comment-item');
        const replyForm = commentItem.find('.reply-form');

        // 다른 답글 폼들 숨기기
        $('.reply-form').not(replyForm).hide();

        replyForm.toggle();
        if (replyForm.is(':visible')) {
            replyForm.find('.reply-input').focus();
        }

        // 취소 버튼 이벤트
        replyForm.find('.reply-cancel-btn').off('click').on('click', () => {
            replyForm.hide();
            replyForm.find('.reply-input').val('');
        });
    }

    // 수정 폼 표시
    showEditForm(event) {
        const commentItem = $(event.target).closest('.comment-item');
        const commentText = commentItem.find('.comment-text');
        const currentContent = commentText.text();
        const commentId = commentItem.data('comment-id');

        const editForm = `
            <div class="edit-form">
                <textarea class="edit-input" rows="3">${currentContent}</textarea>
                <div class="edit-actions">
                    <button class="edit-cancel-btn">취소</button>
                    <button class="edit-submit-btn">수정</button>
                </div>
            </div>
        `;

        commentText.after(editForm);
        commentText.hide();
        commentItem.find('.comment-actions').hide();

        // 취소 버튼
        commentItem.find('.edit-cancel-btn').on('click', () => {
            commentItem.find('.edit-form').remove();
            commentText.show();
            commentItem.find('.comment-actions').show();
        });

        // 수정 완료 버튼
        commentItem.find('.edit-submit-btn').on('click', () => {
            const newContent = commentItem.find('.edit-input').val().trim();
            if (newContent) {
                this.updateComment(commentId, newContent);
            }
        });
    }

    // DOM에서 댓글 업데이트
    updateCommentInDOM(commentId, updatedComment) {
        const commentItem = $(`.comment-item[data-comment-id="${commentId}"]`);
        commentItem.find('.comment-text').text(updatedComment.content);
        commentItem.find('.edit-form').remove();
        commentItem.find('.comment-text').show();
        commentItem.find('.comment-actions').show();
    }

    // 댓글을 삭제된 상태로 표시
    markCommentAsDeleted(commentId) {
        const commentItem = $(`.comment-item[data-comment-id="${commentId}"]`);
        commentItem.find('.comment-text').text('삭제된 댓글입니다');
        commentItem.find('.comment-actions').remove();
        commentItem.addClass('deleted-comment');
    }

    // 새 댓글을 목록 상단에 추가
    prependComment(comment) {
        const commentHtml = this.createCommentHTML(comment);
        $('.comments-list').prepend(commentHtml);
    }

    // 대댓글을 해당 댓글에 추가
    addReplyToComment(parentId, reply) {
        const parentComment = $(`.comment-item[data-comment-id="${parentId}"]`);
        let repliesContainer = parentComment.find('.replies-container');

        if (repliesContainer.length === 0) {
            repliesContainer = $('<div class="replies-container"></div>');
            parentComment.find('.comment-content').append(repliesContainer);

            // 답글 토글 버튼 추가
            if (parentComment.find('.toggle-replies-btn').length === 0) {
                const toggleBtn = this.createRepliesToggleHTML({id: parentId});
                parentComment.find('.comment-actions').after(toggleBtn);
            }
        }

        const replyHtml = this.createReplyHTML(reply);
        repliesContainer.append(replyHtml);
        repliesContainer.show();

        // 토글 버튼 상태 업데이트
        const toggleBtn = parentComment.find('.toggle-replies-btn');
        toggleBtn.find('i').removeClass('fa-chevron-down').addClass('fa-chevron-up');
        toggleBtn.find('span').text('답글 숨기기');
    }

    // 댓글 수 업데이트
    updateCommentCount(count = null) {
        if (count !== null) {
            $('.comments-count').text(count);
        } else {
            // 현재 댓글 수에서 +1
            const currentCount = parseInt($('.comments-count').text()) || 0;
            $('.comments-count').text(currentCount + 1);
        }
    }

    // 날짜 포맷팅
    formatDate(dateString) {
        const date = new Date(dateString);
        const now = new Date();
        const diffMs = now - date;
        const diffMins = Math.floor(diffMs / 60000);
        const diffHours = Math.floor(diffMins / 60);
        const diffDays = Math.floor(diffHours / 24);

        if (diffMins < 1) return '방금 전';
        if (diffMins < 60) return `${diffMins}분 전`;
        if (diffHours < 24) return `${diffHours}시간 전`;
        if (diffDays < 7) return `${diffDays}일 전`;

        return date.toLocaleDateString('ko-KR');
    }

    // 토스트 메시지 표시 - 간단한 버전으로 수정
    showToast(message, type = 'success') {
        // 기존 토스트 제거
        $('.custom-toast').remove();

        const backgroundColor = this.getToastBackgroundColor(type);

        const toastHtml = `
            <div class="custom-toast" style="
                position: fixed;
                top: 20px;
                right: 20px;
                padding: 15px 20px;
                border-radius: 6px;
                color: white;
                font-weight: 500;
                z-index: 10000;
                opacity: 0;
                transition: all 0.3s ease;
                max-width: 350px;
                box-shadow: 0 4px 12px rgba(0,0,0,0.3);
                ${backgroundColor}
            ">
                ${message}
            </div>
        `;

        $('body').append(toastHtml);

        const $toast = $('.custom-toast');

        // 페이드 인
        setTimeout(() => {
            $toast.css({
                'opacity': '1',
                'transform': 'translateY(0)'
            });
        }, 10);

        // 자동 제거
        setTimeout(() => {
            $toast.css({
                'opacity': '0',
                'transform': 'translateY(-20px)'
            });
            setTimeout(() => $toast.remove(), 300);
        }, 3000);

        console.log('토스트 메시지 표시:', message); // 디버깅용
    }

    // 토스트 배경색 반환
    getToastBackgroundColor(type) {
        switch(type) {
            case 'success': return 'background: linear-gradient(135deg, #28a745, #20c997);';
            case 'error': return 'background: linear-gradient(135deg, #dc3545, #e74c3c);';
            case 'warning': return 'background: linear-gradient(135deg, #ffc107, #fd7e14); color: #212529;';
            case 'info':
            default: return 'background: linear-gradient(135deg, #17a2b8, #20c997);';
        }
    }
}

// 페이지 로드 시 댓글 매니저 초기화
$(document).ready(function() {
    // 게시글 ID를 HTML에서 가져오기 (예: data 속성 사용)
    const postId = $('#post-detail').data('post-id') || 1; // 임시값
    window.commentManager = new CommentManager(postId);
});

// CSS 스타일 (별도 파일로 분리 권장)
const commentStyles = `
<style>
.popular-comments-section {
    background-color: #f8f9fa;
    border-radius: 8px;
    padding: 15px;
    margin-bottom: 20px;
}

.popular-comments-title {
    color: #ff6b35;
    font-size: 14px;
    font-weight: bold;
    margin-bottom: 10px;
}

.popular-badge {
    background-color: #ff6b35;
    color: white;
    font-size: 10px;
    padding: 2px 6px;
    border-radius: 10px;
    margin-left: 5px;
}

.reply-item {
    margin-left: 20px;
    border-left: 2px solid #e9ecef;
    padding-left: 15px;
}

.reply-form, .edit-form {
    margin-top: 10px;
    padding: 10px;
    background-color: #f8f9fa;
    border-radius: 6px;
}

.reply-input, .edit-input {
    width: 100%;
    border: 1px solid #ddd;
    border-radius: 4px;
    padding: 8px;
    resize: vertical;
    font-family: inherit;
}

.reply-actions, .edit-actions {
    display: flex;
    justify-content: flex-end;
    gap: 10px;
    margin-top: 10px;
}

.reply-cancel-btn, .edit-cancel-btn {
    background: #6c757d;
    color: white;
    border: none;
    padding: 5px 12px;
    border-radius: 4px;
    cursor: pointer;
}

.reply-submit-btn, .edit-submit-btn {
    background: #007bff;
    color: white;
    border: none;
    padding: 5px 12px;
    border-radius: 4px;
    cursor: pointer;
}

.comment-action.active {
    color: #e91e63;
}

.comment-action.active .fas {
    color: #e91e63;
}

.deleted-comment {
    opacity: 0.6;
}

.toast {
    position: fixed;
    top: 20px;
    right: 20px;
    padding: 12px 20px;
    border-radius: 6px;
    color: white;
    font-weight: 500;
    z-index: 9999;
    transform: translateX(100%);
    transition: transform 0.3s ease;
}

.toast.show {
    transform: translateX(0);
}

.toast-success { background-color: #28a745; }
.toast-error { background-color: #dc3545; }
.toast-warning { background-color: #ffc107; color: #212529; }
.toast-info { background-color: #17a2b8; }

.toggle-replies-btn {
    color: #007bff;
    cursor: pointer;
    font-size: 12px;
    margin-top: 5px;
}

.toggle-replies-btn:hover {
    text-decoration: underline;
}

.comment-options {
    margin-left: auto;
}

.comment-edit-btn, .comment-delete-btn {
    background: none;
    border: none;
    color: #6c757d;
    font-size: 12px;
    cursor: pointer;
    margin-left: 10px;
}

.comment-edit-btn:hover, .comment-delete-btn:hover {
    color: #495057;
}
</style>
`;

// 스타일 추가
$('head').append(commentStyles);