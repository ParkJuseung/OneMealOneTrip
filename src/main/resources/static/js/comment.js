/**
 * 댓글 관련 JavaScript 기능 - 신고 기능 추가
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

        // 댓글 신고 (새로 추가)
        $(document).on('click', '.report-btn', (e) => {
            this.showReportModal(e);
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

            if (result.success) {
                let comments = [];
                if (result.data && result.data.dtoList) {
                    comments = result.data.dtoList;
                    this.hasMoreComments = result.data.next;
                    this.updateCommentCount(result.data.totalCount);
                } else if (Array.isArray(result.data)) {
                    comments = result.data;
                } else {
                    comments = [];
                }

                if (comments.length > 0) {
                    await this.renderComments(comments);
                } else {
                    if (this.currentPage === 0) {
                        $('.comments-list').append('<p class="no-comments">아직 댓글이 없습니다.</p>');
                    }
                }
            } else {
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
                    this.addReplyToComment(parentId, result.data);
                    $(`.reply-form[data-parent-id="${parentId}"]`).hide();
                    $(`.reply-form[data-parent-id="${parentId}"] .reply-input`).val('');
                } else {
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

                // 싫어요가 10개 이상이 되면 블라인드 처리 알림
                if (reactionType === 'DISLIKE' && result.data.dislikeCount >= 10) {
                    this.showToast('댓글이 블라인드 처리되었습니다.', 'info');
                    // 페이지 새로고침
                    setTimeout(() => {
                        location.reload();
                    }, 1500);
                }
            } else {
                this.showToast(result.message || '반응 처리에 실패했습니다.', 'error');
            }
        } catch (error) {
            console.error('반응 처리 실패:', error);
            this.showToast('반응 처리에 실패했습니다.', 'error');
        }
    }

    // 신고 모달 표시 (새로 추가)
    async showReportModal(event) {
        const commentId = $(event.target).closest('.comment-item, .reply-item').data('comment-id');

        // 먼저 신고 가능 여부 확인
        try {
            const statusResponse = await fetch(`/api/reports/comment/${commentId}/status`);
            const statusResult = await statusResponse.json();

            if (!statusResult.success) {
                this.showToast('신고 상태를 확인할 수 없습니다.', 'error');
                return;
            }

            const status = statusResult.data;

            if (!status.canReport) {
                this.showToast(status.cannotReportReason, 'warning');
                return;
            }

            // 신고 모달 HTML 생성
            const modalHtml = `
                <div class="report-modal-overlay" style="
                    position: fixed;
                    top: 0;
                    left: 0;
                    width: 100%;
                    height: 100%;
                    background: rgba(0, 0, 0, 0.6);
                    z-index: 10000;
                    display: flex;
                    align-items: center;
                    justify-content: center;
                ">
                    <div class="report-modal" style="
                        background: white;
                        border-radius: 12px;
                        padding: 24px;
                        max-width: 500px;
                        width: 90%;
                        max-height: 90%;
                        overflow-y: auto;
                        box-shadow: 0 8px 32px rgba(0, 0, 0, 0.3);
                        position: relative;
                    ">
                        <div class="report-modal-header" style="
                            display: flex;
                            justify-content: space-between;
                            align-items: center;
                            margin-bottom: 20px;
                            padding-bottom: 12px;
                            border-bottom: 1px solid #e9ecef;
                        ">
                            <h3 style="margin: 0; color: #333; font-size: 18px;">댓글 신고</h3>
                            <button class="report-modal-close" style="
                                background: none;
                                border: none;
                                font-size: 24px;
                                cursor: pointer;
                                color: #666;
                                padding: 0;
                                width: 30px;
                                height: 30px;
                                display: flex;
                                align-items: center;
                                justify-content: center;
                            ">&times;</button>
                        </div>
                        
                        <div class="report-modal-body">
                            <div class="report-info" style="
                                background: #f8f9fa;
                                padding: 12px;
                                border-radius: 8px;
                                margin-bottom: 20px;
                                border-left: 4px solid #dc3545;
                            ">
                                <p style="margin: 0; font-size: 14px; color: #666;">
                                    <i class="fas fa-info-circle" style="margin-right: 8px; color: #dc3545;"></i>
                                    부적절한 댓글을 신고해주세요. 허위 신고는 제재를 받을 수 있습니다.
                                </p>
                            </div>
                            
                            <div class="report-reason-section" style="margin-bottom: 20px;">
                                <label style="
                                    display: block;
                                    margin-bottom: 8px;
                                    font-weight: 600;
                                    color: #333;
                                ">신고 사유 <span style="color: #dc3545;">*</span></label>
                                <div class="report-reason-options">
                                    <label style="
                                        display: block;
                                        margin-bottom: 8px;
                                        cursor: pointer;
                                        padding: 8px;
                                        border-radius: 6px;
                                        transition: background-color 0.2s;
                                    " onmouseover="this.style.backgroundColor='#f8f9fa'" 
                                       onmouseout="this.style.backgroundColor='transparent'">
                                        <input type="radio" name="reportReason" value="SPAM" style="margin-right: 8px;">
                                        스팸 및 광고
                                    </label>
                                    <label style="
                                        display: block;
                                        margin-bottom: 8px;
                                        cursor: pointer;
                                        padding: 8px;
                                        border-radius: 6px;
                                        transition: background-color 0.2s;
                                    " onmouseover="this.style.backgroundColor='#f8f9fa'" 
                                       onmouseout="this.style.backgroundColor='transparent'">
                                        <input type="radio" name="reportReason" value="INAPPROPRIATE" style="margin-right: 8px;">
                                        부적절한 내용
                                    </label>
                                    <label style="
                                        display: block;
                                        margin-bottom: 8px;
                                        cursor: pointer;
                                        padding: 8px;
                                        border-radius: 6px;
                                        transition: background-color 0.2s;
                                    " onmouseover="this.style.backgroundColor='#f8f9fa'" 
                                       onmouseout="this.style.backgroundColor='transparent'">
                                        <input type="radio" name="reportReason" value="HARASSMENT" style="margin-right: 8px;">
                                        괴롭힘 및 욕설
                                    </label>
                                    <label style="
                                        display: block;
                                        margin-bottom: 8px;
                                        cursor: pointer;
                                        padding: 8px;
                                        border-radius: 6px;
                                        transition: background-color 0.2s;
                                    " onmouseover="this.style.backgroundColor='#f8f9fa'" 
                                       onmouseout="this.style.backgroundColor='transparent'">
                                        <input type="radio" name="reportReason" value="HATE_SPEECH" style="margin-right: 8px;">
                                        혐오 발언
                                    </label>
                                    <label style="
                                        display: block;
                                        margin-bottom: 8px;
                                        cursor: pointer;
                                        padding: 8px;
                                        border-radius: 6px;
                                        transition: background-color 0.2s;
                                    " onmouseover="this.style.backgroundColor='#f8f9fa'" 
                                       onmouseout="this.style.backgroundColor='transparent'">
                                        <input type="radio" name="reportReason" value="OTHER" style="margin-right: 8px;">
                                        기타
                                    </label>
                                </div>
                            </div>
                            
                            <div class="report-detail-section" style="margin-bottom: 20px;">
                                <label style="
                                    display: block;
                                    margin-bottom: 8px;
                                    font-weight: 600;
                                    color: #333;
                                ">상세 내용</label>
                                <textarea class="report-detail-input" placeholder="신고 사유를 자세히 설명해주세요 (선택사항)" style="
                                    width: 100%;
                                    min-height: 80px;
                                    padding: 12px;
                                    border: 1px solid #ddd;
                                    border-radius: 8px;
                                    resize: vertical;
                                    font-family: inherit;
                                    font-size: 14px;
                                    box-sizing: border-box;
                                " maxlength="1000"></textarea>
                                <div style="
                                    text-align: right;
                                    margin-top: 4px;
                                    font-size: 12px;
                                    color: #666;
                                ">
                                    <span class="char-count">0</span>/1000
                                </div>
                            </div>
                        </div>
                        
                        <div class="report-modal-footer" style="
                            display: flex;
                            gap: 12px;
                            justify-content: flex-end;
                            padding-top: 20px;
                            border-top: 1px solid #e9ecef;
                        ">
                            <button class="report-cancel-btn" style="
                                padding: 10px 20px;
                                border: 1px solid #ddd;
                                background: white;
                                color: #666;
                                border-radius: 6px;
                                cursor: pointer;
                                font-size: 14px;
                                transition: all 0.2s;
                            " onmouseover="this.style.backgroundColor='#f8f9fa'"
                               onmouseout="this.style.backgroundColor='white'">취소</button>
                            <button class="report-submit-btn" style="
                                padding: 10px 20px;
                                border: none;
                                background: #dc3545;
                                color: white;
                                border-radius: 6px;
                                cursor: pointer;
                                font-size: 14px;
                                transition: all 0.2s;
                            " onmouseover="this.style.backgroundColor='#c82333'"
                               onmouseout="this.style.backgroundColor='#dc3545'"
                               data-comment-id="${commentId}">신고하기</button>
                        </div>
                    </div>
                </div>
            `;

            // 모달을 body에 추가
            $('body').append(modalHtml);

            // 문자 수 카운터 업데이트
            $('.report-detail-input').on('input', function() {
                const count = $(this).val().length;
                $('.char-count').text(count);
            });

            // 모달 닫기 이벤트
            $('.report-modal-close, .report-cancel-btn').on('click', () => {
                $('.report-modal-overlay').remove();
            });

            // 배경 클릭으로 모달 닫기
            $('.report-modal-overlay').on('click', function(e) {
                if (e.target === this) {
                    $(this).remove();
                }
            });

            // 신고 제출 이벤트
            $('.report-submit-btn').on('click', () => {
                this.submitReport(commentId);
            });

            // ESC 키로 모달 닫기
            $(document).on('keydown.reportModal', function(e) {
                if (e.keyCode === 27) {
                    $('.report-modal-overlay').remove();
                    $(document).off('keydown.reportModal');
                }
            });

        } catch (error) {
            console.error('신고 상태 확인 실패:', error);
            this.showToast('신고 처리 중 오류가 발생했습니다.', 'error');
        }
    }

    // 신고 제출 (새로 추가)
    async submitReport(commentId) {
        const selectedReason = $('input[name="reportReason"]:checked').val();
        const detail = $('.report-detail-input').val().trim();

        if (!selectedReason) {
            this.showToast('신고 사유를 선택해주세요.', 'warning');
            return;
        }

        try {
            const response = await fetch('/api/reports', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({
                    reportType: 'COMMENT',
                    targetId: commentId,
                    reason: selectedReason,
                    detail: detail
                })
            });

            const result = await response.json();

            if (result.success) {
                $('.report-modal-overlay').remove();
                this.showToast('신고가 접수되었습니다. 검토 후 조치하겠습니다.', 'success');

                // 신고 버튼을 비활성화하고 텍스트 변경
                const commentItem = $(`.comment-item[data-comment-id="${commentId}"], .reply-item[data-comment-id="${commentId}"]`);
                const reportBtn = commentItem.find('.report-btn');
                reportBtn.removeClass('report-btn').addClass('reported-btn');
                reportBtn.find('span').text('신고됨');
                reportBtn.css({
                    'color': '#6c757d',
                    'cursor': 'not-allowed',
                    'opacity': '0.6'
                });
                reportBtn.off('click');

            } else {
                this.showToast(result.message || '신고 접수에 실패했습니다.', 'error');
            }

        } catch (error) {
            console.error('신고 제출 실패:', error);
            this.showToast('신고 접수 중 오류가 발생했습니다.', 'error');
        }
    }

    // 댓글 렌더링 - 개선된 버전
    async renderComments(comments) {
        const commentsContainer = $('.comments-list');

        if (commentsContainer.length === 0) {
            console.error('댓글 컨테이너(.comments-list)를 찾을 수 없습니다!');
            return;
        }

        for (const comment of comments) {
            try {
                const commentHtml = await this.createCommentHTML(comment);
                commentsContainer.append(commentHtml);
            } catch (error) {
                console.error(`댓글 렌더링 실패:`, error);
            }
        }

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

    // 댓글 HTML 생성 - 신고 기능 추가
    async createCommentHTML(comment, isPopular = false) {
        const isBlinded = comment.isBlinded;
        const isDeleted = comment.isDeleted === 'Y';
        const displayContent = isDeleted ? '삭제된 댓글입니다' :
            (isBlinded ? '블라인드 처리된 댓글입니다' : comment.content);
        const popularBadge = isPopular ? '<span class="popular-badge">인기</span>' : '';

        // 신고 상태 조회
        let canReport = false;
        let isReported = false;
        try {
            const statusResponse = await fetch(`/api/reports/comment/${comment.id}/status`);
            const statusResult = await statusResponse.json();
            if (statusResult.success) {
                canReport = statusResult.data.canReport;
                isReported = statusResult.data.isReportedByCurrentUser;
            }
        } catch (error) {
            console.warn('신고 상태 조회 실패:', error);
        }

        return `
            <div class="comment-item ${isPopular ? 'popular-comment' : ''} ${isDeleted ? 'comment-deleted' : ''} ${isBlinded ? 'comment-blinded' : ''}" data-comment-id="${comment.id}">
                <img src="${comment.userProfileImage || '/api/placeholder/40/40'}" 
                     alt="${comment.userNickname}" class="comment-avatar">
                <div class="comment-content">
                    <div class="comment-header">
                        <span class="comment-author">${comment.userNickname}</span>
                        ${popularBadge}
                        <span class="comment-date">${this.formatDate(comment.createdAt)}</span>
                    </div>
                    <p class="comment-text">${displayContent}</p>
                    ${this.createCommentActionsHTML(comment, isBlinded, isDeleted, canReport, isReported)}
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

    // 댓글 액션 버튼 HTML 생성 - 신고 기능 추가
    createCommentActionsHTML(comment, isBlinded, isDeleted, canReport, isReported) {
        if (isDeleted) {
            return ''; // 삭제된 댓글은 액션 버튼 없음
        }

        let actionsHtml = '<div class="comment-actions">';

        if (!isBlinded) {
            // 일반 댓글: 좋아요, 싫어요, 답글, 신고 버튼
            actionsHtml += `
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
            `;

            // 신고 버튼 추가
            if (canReport && !isReported) {
                actionsHtml += `
                    <div class="comment-action report-btn">
                        <i class="fas fa-flag"></i>
                        <span>신고</span>
                    </div>
                `;
            } else if (isReported) {
                actionsHtml += `
                    <div class="comment-action reported-btn" style="color: #6c757d; cursor: not-allowed; opacity: 0.6;">
                        <i class="fas fa-flag"></i>
                        <span>신고됨</span>
                    </div>
                `;
            }

            // 수정/삭제 버튼 (본인 댓글인 경우)
            if (comment.userId === 1) { // 임시로 사용자 ID 1로 비교
                actionsHtml += `
                    <div class="comment-options">
                        <button class="comment-edit-btn">수정</button>
                        <button class="comment-delete-btn">삭제</button>
                    </div>
                `;
            }
        } else {
            // 블라인드된 댓글: 신고 버튼만 표시
            if (canReport && !isReported) {
                actionsHtml += `
                    <div class="comment-action report-btn">
                        <i class="fas fa-flag"></i>
                        <span>신고</span>
                    </div>
                `;
            } else if (isReported) {
                actionsHtml += `
                    <div class="comment-action reported-btn" style="color: #6c757d; cursor: not-allowed; opacity: 0.6;">
                        <i class="fas fa-flag"></i>
                        <span>신고됨</span>
                    </div>
                `;
            }
        }

        actionsHtml += '</div>';
        return actionsHtml;
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
                const repliesHtml = await Promise.all(
                    result.data.map(reply => this.createReplyHTML(reply))
                );
                repliesContainer.html(repliesHtml.join(''));
            }
        } catch (error) {
            console.error('대댓글 로드 실패:', error);
        }
    }

    // 대댓글 HTML 생성 - 신고 기능 추가
    async createReplyHTML(reply) {
        const isBlinded = reply.isBlinded;
        const isDeleted = reply.isDeleted === 'Y';
        const displayContent = isDeleted ? '삭제된 댓글입니다' :
            (isBlinded ? '블라인드 처리된 댓글입니다' : reply.content);

        // 신고 상태 조회
        let canReport = false;
        let isReported = false;
        try {
            const statusResponse = await fetch(`/api/reports/comment/${reply.id}/status`);
            const statusResult = await statusResponse.json();
            if (statusResult.success) {
                canReport = statusResult.data.canReport;
                isReported = statusResult.data.isReportedByCurrentUser;
            }
        } catch (error) {
            console.warn('신고 상태 조회 실패:', error);
        }

        return `
            <div class="comment-item reply-item ${isDeleted ? 'comment-deleted' : ''} ${isBlinded ? 'comment-blinded' : ''}" data-comment-id="${reply.id}">
                <img src="${reply.userProfileImage || '/api/placeholder/40/40'}" 
                     alt="${reply.userNickname}" class="comment-avatar">
                <div class="comment-content">
                    <div class="comment-header">
                        <span class="comment-author">${reply.userNickname}</span>
                        <span class="comment-date">${this.formatDate(reply.createdAt)}</span>
                    </div>
                    <p class="comment-text">${displayContent}</p>
                    ${this.createReplyActionsHTML(reply, isBlinded, isDeleted, canReport, isReported)}
                </div>
            </div>
        `;
    }

    // 대댓글 액션 버튼 HTML 생성
    createReplyActionsHTML(reply, isBlinded, isDeleted, canReport, isReported) {
        if (isDeleted) {
            return ''; // 삭제된 댓글은 액션 버튼 없음
        }

        let actionsHtml = '<div class="comment-actions">';

        if (!isBlinded) {
            // 일반 대댓글: 좋아요, 싫어요, 신고 버튼
            actionsHtml += `
                <div class="comment-action comment-like-btn ${reply.isLikedByCurrentUser ? 'active' : ''}">
                    <i class="fas fa-heart"></i>
                    <span class="like-count">${reply.likeCount || 0}</span>
                </div>
                <div class="comment-action comment-dislike-btn ${reply.isDislikedByCurrentUser ? 'active' : ''}">
                    <i class="fas fa-heart-broken"></i>
                    <span class="dislike-count">${reply.dislikeCount || 0}</span>
                </div>
            `;

            // 신고 버튼 추가
            if (canReport && !isReported) {
                actionsHtml += `
                    <div class="comment-action report-btn">
                        <i class="fas fa-flag"></i>
                        <span>신고</span>
                    </div>
                `;
            } else if (isReported) {
                actionsHtml += `
                    <div class="comment-action reported-btn" style="color: #6c757d; cursor: not-allowed; opacity: 0.6;">
                        <i class="fas fa-flag"></i>
                        <span>신고됨</span>
                    </div>
                `;
            }

            // 수정/삭제 버튼 (본인 대댓글인 경우)
            if (reply.userId === 1) { // 임시로 사용자 ID 1로 비교
                actionsHtml += `
                    <div class="comment-options">
                        <button class="comment-edit-btn">수정</button>
                        <button class="comment-delete-btn">삭제</button>
                    </div>
                `;
            }
        } else {
            // 블라인드된 대댓글: 신고 버튼만 표시
            if (canReport && !isReported) {
                actionsHtml += `
                    <div class="comment-action report-btn">
                        <i class="fas fa-flag"></i>
                        <span>신고</span>
                    </div>
                `;
            } else if (isReported) {
                actionsHtml += `
                    <div class="comment-action reported-btn" style="color: #6c757d; cursor: not-allowed; opacity: 0.6;">
                        <i class="fas fa-flag"></i>
                        <span>신고됨</span>
                    </div>
                `;
            }
        }

        actionsHtml += '</div>';
        return actionsHtml;
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

    // 토스트 메시지 표시
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

// CSS 스타일 추가 (신고 관련 스타일 포함)
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

.comment-blinded {
    background-color: #f5f5f5;
    opacity: 0.8;
}

.comment-blinded .comment-text {
    color: #999;
    font-style: italic;
}

/* 신고 버튼 스타일 */
.report-btn {
    color: #dc3545 !important;
    transition: all 0.2s ease;
}

.report-btn:hover {
    background-color: #ffebee !important;
    color: #c62828 !important;
}

.reported-btn {
    color: #6c757d !important;
    cursor: not-allowed !important;
    opacity: 0.6 !important;
}

/* 신고 모달 반응형 */
@media (max-width: 768px) {
    .report-modal {
        margin: 20px !important;
        max-width: none !important;
        width: calc(100% - 40px) !important;
    }
}

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