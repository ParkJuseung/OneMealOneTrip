document.addEventListener('DOMContentLoaded', function() {
    // 뒤로가기 버튼
    const backButton = document.getElementById('back-button');
    if (backButton) {
        backButton.addEventListener('click', function() {
            window.history.back();
        });
    }

    // 프로필 편집 모달 관련
    const editProfileButton = document.getElementById('edit-profile-button');
    const profileEditModal = document.getElementById('profile-edit-modal');
    const modalClose = document.getElementById('modal-close');
    const cancelButton = document.getElementById('cancel-button');

    function closeModal() {
        profileEditModal.classList.remove('active');
        document.body.style.overflow = '';
    }

    if (editProfileButton && profileEditModal) {
        editProfileButton.addEventListener('click', function() {
            profileEditModal.classList.add('active');
            document.body.style.overflow = 'hidden';
        });
        modalClose.addEventListener('click', closeModal);
        cancelButton.addEventListener('click', closeModal);
        profileEditModal.addEventListener('click', function(e) {
            if (e.target === this) closeModal();
        });
    }

    //프로필 수정 폼 AJAX 제출
    const profileEditForm = document.getElementById('profile-edit-form');
    if (profileEditForm) {
        profileEditForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            const url = profileEditForm.action;            // th:action 에 설정된 /mypage/edit
            const formData = new FormData(profileEditForm);


            try {
                const resp = await fetch(url, {
                    method: 'POST',
                    body: formData,
                    credentials: 'include'
                });

                if (resp.ok) {
                    alert('프로필이 성공적으로 수정되었습니다!');
                    closeModal();
                    // 변경된 DB 값을 바로 가져오려면 페이지 새로고침
                    window.location.reload();
                } else if (resp.status === 401) {
                    alert('로그인이 필요합니다.');
                    window.location.href = '/login';
                } else {
                    alert('프로필 수정에 실패했습니다. 상태 코드: ' + resp.status);
                }
            } catch (err) {
                console.error(err);
                alert('서버와 연결에 실패했습니다.');
            }
        });
    }

    // 프로필 편집 미리보기 기능
    const editInput   = document.getElementById('edit-profile-input');   // 파일 input
    const editPreview = document.getElementById('edit-profile-preview'); // 미리보기 영역

    if (editInput && editPreview) {
        editInput.addEventListener('change', function(e) {
            const file = e.target.files[0];
            if (!file) return;

            // 이전 내용 삭제
            editPreview.innerHTML = '';

            // <img> 생성 후 삽입
            const img = document.createElement('img');
            img.src = URL.createObjectURL(file);
            img.onload = () => URL.revokeObjectURL(img.src);
            img.style.maxWidth  = '100%';
            img.style.maxHeight = '100%';
            img.alt = '프로필 미리보기';

            editPreview.appendChild(img);
        });
    }

    // ── 모달 전용: 프로필 추가·삭제 버튼 핸들링 ──
    const removeImageInput   = document.getElementById('remove-image');
    const modalDeleteButton  = document.getElementById('modal-delete-button');
    const modalUploadButton  = document.getElementById('modal-upload-button');

    // 카메라 아이콘(추가) 클릭 시 파일창 열기
    if (modalUploadButton && editInput) {
        modalUploadButton.addEventListener('click', () => {
            editInput.click();
        });
    }


    // 삭제 버튼 클릭 시: mypage.js
    if (modalDeleteButton && removeImageInput && editPreview && editInput) {
        modalDeleteButton.addEventListener('click', () => {
            // 1) 삭제 플래그 켜기
            removeImageInput.value = 'true';

            // 2) 파일 input 초기화
            editInput.value = '';

            // 3) preview 컨테이너 안의 모든 <img> 지우기
            editPreview.querySelectorAll('img').forEach(img => img.remove());

            // 4) preview 컨테이너 안의 모든 <i> (혹은 다른 잔여 노드) 지우기
            editPreview.querySelectorAll('i').forEach(i => i.remove());

            // 5) 기본 이미지(혹은 아이콘) + 카메라 버튼 HTML 을 아예 다시 그려 주기
            editPreview.innerHTML = `
      <img src="/images/default-profile.png"
           alt="기본 프로필"
           class="preview-img default-img"/>
      <div class="profile-upload-button" id="modal-upload-button">
        <i class="fas fa-camera"></i>
      </div>
    `;
        });
    }

    // 버킷리스트 토글
    const bucketCheckboxes = document.querySelectorAll('.bucket-checkbox');
    bucketCheckboxes.forEach(checkbox => {
        checkbox.addEventListener('change', function() {
            console.log(`버킷리스트 항목 "${this.nextElementSibling.textContent}" 상태 변경: ${this.checked}`);
        });
    });

    // 방명록 등록
    const leaveMessageButton = document.querySelector('.leave-message button');
    const leaveMessageTextarea = document.querySelector('.leave-message textarea');
    if (leaveMessageButton && leaveMessageTextarea) {
        leaveMessageButton.addEventListener('click', function() {
            const messageText = leaveMessageTextarea.value.trim();
            if (messageText) {
                alert('방명록이 등록되었습니다!');
                leaveMessageTextarea.value = '';

                const now = new Date();
                const year = now.getFullYear();
                const month = String(now.getMonth() + 1).padStart(2, '0');
                const day = String(now.getDate()).padStart(2, '0');
                const formattedDate = `${year}.${month}.${day}`;

                const newGuestbook = document.createElement('div');
                newGuestbook.className = 'guestbook-item';
                newGuestbook.innerHTML = `
          <img src="/api/placeholder/40/40" alt="방명록 작성자" class="guestbook-avatar">
          <div class="guestbook-content">
            <div class="guestbook-author">김여행</div>
            <div class="guestbook-text">${messageText}</div>
            <div class="guestbook-date">${formattedDate}</div>
          </div>
        `;
                const guestbookSection = document.querySelector('.guestbook-section');
                if (guestbookSection) {
                    guestbookSection.insertBefore(newGuestbook, document.querySelector('.leave-message'));
                }
            }
        });
    }

    // 나의 소개 관련 기능
    const createBtn = document.getElementById('intro-create-btn');
    const editBtn = document.getElementById('intro-edit-btn');
    const deleteBtn = document.getElementById('intro-delete-btn');
    const viewEmpty = document.getElementById('intro-view-empty');
    const viewMode = document.getElementById('intro-view');
    const editMode = document.getElementById('intro-edit');
    const cancelBtn = document.getElementById('intro-cancel-btn');
    const saveBtn = document.getElementById('intro-save-btn');
    console.log('▶ saveBtn element:', saveBtn);


    const textarea = document.getElementById('intro-content');
    const charCount = document.getElementById('intro-char-count');
    const tagInput = document.getElementById('intro-tag-input');
    const tagList = document.getElementById('intro-new-tags');
    const sectionActions = document.querySelector('#intro-section .section-actions');

    // CSRF 메타 태그가 있으면 꺼내고, 없으면 빈 문자열로
    const csrfMeta       = document.querySelector('meta[name="_csrf"]');
    const csrfHeaderMeta = document.querySelector('meta[name="_csrf_header"]');
    const csrfToken      = csrfMeta       ? csrfMeta.getAttribute('content')       : '';
    const csrfHeader     = csrfHeaderMeta ? csrfHeaderMeta.getAttribute('content') : '';

    let isComposing = false;
    tagInput.addEventListener('compositionstart', () => {
        isComposing = true;
    });
    tagInput.addEventListener('compositionend', () => {
        isComposing = false;
    });

    let tags = [];


    // 수정 버튼 클릭
    if (editBtn) {
        editBtn.addEventListener('click', () => {
            viewMode.classList.add('hidden');
            editMode.classList.remove('hidden');

            // 기존 데이터로 폼 채우기
            const currentContent = document.querySelector('#intro-view .introduce-content').textContent;
            textarea.value = currentContent;
            charCount.textContent = `${currentContent.length} / 500`;

            // 기존 태그들 가져오기
            tags = [];
            tagList.innerHTML = '';
            const existingTags = document.querySelectorAll('#intro-view .interest-tag');
            existingTags.forEach(tag => {
                const tagText = tag.textContent.replace('#', '');
                tags.push(tagText);
                const span = document.createElement('span');
                span.className = 'interest-tag';
                span.textContent = `#${tagText}`;
                span.addEventListener('click', () => {
                    tags = tags.filter(x => x !== tagText);
                    tagList.removeChild(span);
                });
                tagList.appendChild(span);
            });
        });
    }

    // 삭제 버튼 클릭
    if (deleteBtn) {
        deleteBtn.addEventListener('click', async () => {
            if (!confirm('정말로 나의 소개를 삭제하시겠습니까?')) return;
            try {
                const resp = await fetch('/mypage/intro', {
                    method: 'DELETE',
                    credentials: 'include',
                    headers: csrfHeader ? { [csrfHeader]: csrfToken } : {}
                });
                if (!resp.ok) {
                    return alert(`삭제에 실패했습니다. (${resp.status})`);
                }
                // ★ 성공하면 새로고침하고 바로 리턴 ★
                return window.location.reload();
            } catch (err) {
                console.error('소개 삭제 실패', err);
                alert('삭제 중 오류가 발생했습니다.');
            }
        });
    }

    // 작성하기 버튼 클릭
    if (createBtn) {
        createBtn.addEventListener('click', () => {
            viewEmpty.classList.add('hidden');
            editMode.classList.remove('hidden');
            textarea.value = '';
            tags = [];
            tagList.innerHTML = '';
            charCount.textContent = '0 / 500';
        });
    }

    // 취소 버튼 클릭
    if (cancelBtn) {
        cancelBtn.addEventListener('click', () => {
            editMode.classList.add('hidden');
            if (viewMode.querySelector('.introduce-content').textContent.trim()) {
                viewMode.classList.remove('hidden');
            } else {
                viewEmpty.classList.remove('hidden');
            }
        });
    }

    // 글자수 카운터
    if (textarea) {
        textarea.addEventListener('input', () => {
            const len = textarea.value.length;
            charCount.textContent = `${len} / 500`;
        });
    }

    // 태그 입력
    if (tagInput) {
        tagInput.addEventListener('keydown', (e) => {
            if (e.key === 'Enter' && !isComposing) {
                e.preventDefault();
                const raw = tagInput.value.trim();
                if (!raw) return;

                raw.split(',')
                    .map(t => t.trim())
                    .filter(t => t.length > 0)
                    .forEach(t => {
                        if (tags.includes(t)) return;
                        if (tags.length >= 10) return;
                        if (t.length > 20) return;

                        tags.push(t);
                        const span = document.createElement('span');
                        span.className = 'interest-tag';
                        span.textContent = `#${t}`;
                        span.addEventListener('click', () => {
                            tags = tags.filter(x => x !== t);
                            tagList.removeChild(span);
                        });
                        tagList.appendChild(span);
                    });

                tagInput.value = '';
            }
        });
    }

    if (saveBtn) {
        console.log('▶ 바인딩: saveBtn에 클릭 리스너 연결');
        saveBtn.addEventListener('click', async () => {
            const content = textarea.value.trim();
            if (!content) {
                return alert('소개글을 입력해주세요.');
            }
            // 수정 vs 신규 분기: 읽기 모드(뷰)가 숨겨져 있으면 작성
            const isCreate = viewMode.classList.contains('hidden');
            const method   = isCreate ? 'POST' : 'PUT';

            // 헤더 설정
            const headers = { 'Content-Type': 'application/json' };
            if (csrfHeader) headers[csrfHeader] = csrfToken;

            try {
                const resp = await fetch('/mypage/intro', {
                    method,
                    credentials: 'include',
                    headers,
                    body: JSON.stringify({ content, tags: tags.join(',') })
                });
                if (!resp.ok) {
                    return alert(`저장에 실패했습니다. (${resp.status})`);
                }
                // ★ 성공하면 새로고침하고 바로 리턴 ★
                return window.location.reload();
            } catch (err) {
                console.error('소개 저장 중 에러', err);
                alert('서버 오류가 발생했습니다.');
            }
        });
    }


    // 팔로우/언팔로우 버튼 클릭
    const followBtn = document.getElementById('follow-button');
    if (followBtn) {
        followBtn.addEventListener('click', () => {
            const followeeId = followBtn.dataset.userId;
            const currentlyFollowing = followBtn.textContent.trim() === '언팔로우';
            const method = currentlyFollowing ? 'DELETE' : 'POST';

            fetch(`/follow/${followeeId}`, {
                method,
                credentials: 'include'
            })
                .then(res => {
                    if (!res.ok) throw new Error(res.status);
                    return res.json();
                })
                .then(data => {
                    // 1) 버튼 텍스트 토글
                    followBtn.textContent = currentlyFollowing ? '팔로우' : '언팔로우';
                    // 2) 팔로워 수 갱신
                    document.getElementById('follower-count').textContent = data.followerCount;
                })
                .catch(err => {
                    console.error('팔로우 요청 실패', err);
                    alert('요청에 실패했습니다. 다시 시도해주세요.');
                });
        });
    }

    // ① 모달 요소 참조
    const listModal        = document.getElementById('list-modal');
    const listModalTitle   = document.getElementById('list-modal-title');
    const listModalClose   = document.getElementById('list-modal-close');
    const listModalContent = document.getElementById('list-modal-content');
// 로그인된 사용자 ID + 페이지 주인 여부
    const sessionUserId    = document.body.dataset.sessionUserId; // body 태그에 th:data-session-user-id 설정 권장
    const pageOwnerId      = document.body.dataset.pageOwnerId;
    const isOwner          = sessionUserId && sessionUserId === pageOwnerId;

// ② 모달 열기 함수
    function openListModal(type) {
        // type = 'followers' or 'followings'
        listModalTitle.textContent = (type === 'followers') ? '팔로워' : '팔로잉';
        listModalContent.innerHTML = ''; // 초기화

        fetch(`/users/${pageOwnerId}/${type}`)
            .then(res => res.json())
            .then(list => {
                if (!list.length) {
                    listModalContent.innerHTML =
                        '<p style="text-align:center; color:#888;">목록이 없습니다.</p>';
                    return;
                }
                list.forEach(u => {
                    const item = document.createElement('div');
                    item.className = 'list-item';
                    item.innerHTML = `<span>${u.nickname}</span>`;
                    // 팔로잉 모달이면서, 본인 페이지일 땐 “언팔로우” 버튼 추가
                    if (type === 'followings' && u.canUnfollow) {
                        const btn = document.createElement('button');
                        btn.className = 'cancel-button';
                        btn.textContent = '언팔로우';
                        btn.dataset.userId = u.id;
                        btn.addEventListener('click', () => {
                            // 언팔로우 API 호출
                            fetch(`/follow/${u.id}`, {
                                method: 'DELETE',
                                credentials: 'include'
                            })
                                .then(r => r.json())
                                .then(data => {
                                    item.remove(); // 리스트에서 제거
                                    // 팔로잉 카운트 업데이트
                                    document.getElementById('following-count').textContent =
                                        listModalContent.querySelectorAll('.list-item').length;
                                });
                        });
                        item.appendChild(btn);
                    }
                    listModalContent.appendChild(item);
                });
            });

        listModal.classList.add('active');
        listModal.classList.remove('hidden');

    }

// ③ 클릭 이벤트 바인딩
    const followersSpan  = document.getElementById('followers');
    const followingsSpan = document.getElementById('followings');
    if (followersSpan) {
        followersSpan.addEventListener('click', () => openListModal('followers'));
    }
    if (followingsSpan) {
        followingsSpan.addEventListener('click', () => openListModal('followings'));
    }

// ④ 모달 닫기
    listModalClose.addEventListener('click', () => listModal.classList.add('hidden'));
    listModal.addEventListener('click', e => {
        if (e.target === listModal)
            listModal.classList.remove('active');
            listModal.classList.add('hidden');
    });

    // --- (위에서 이미 sessionUserId, pageOwnerId 등이 정의되어 있어야 합니다) ---

// 1) 헤더 당일 방문자 수 초기 로드
    const visitorCountNum = document.getElementById('visitor-count-number');
    if (visitorCountNum) {
        fetch(`/users/${pageOwnerId}/visitor-stats`)
            .then(res => res.json())
            .then(dto => {
                visitorCountNum.textContent = dto.todayCount;
            })
            .catch(err => console.error('방문자 수 로드 실패', err));
    }

// 2) 모달 관련 요소 참조
    const visitorStatItem    = document.getElementById('visitor-count');
    const visitorStatsModal  = document.getElementById('visitor-stats-modal');
    const visitorStatsClose  = document.getElementById('visitor-stats-close');

    if (visitorStatItem && visitorStatsModal) {
        // 클릭 시 API 호출 → 통계 채우고 모달 오픈
        visitorStatItem.addEventListener('click', () => {
            fetch(`/users/${pageOwnerId}/visitor-stats`)
                .then(res => res.json())
                .then(dto => {
                    document.getElementById('stat-today').textContent  = dto.todayCount;
                    document.getElementById('stat-week').textContent   = dto.weekCount;
                    document.getElementById('stat-month').textContent  = dto.monthCount;
                    document.getElementById('stat-total').textContent  = dto.totalCount;
                    visitorStatsModal.classList.remove('hidden');
                    visitorStatsModal.classList.add('active');
                })
                .catch(err => console.error('방문자 통계 로드 실패', err));
        });

        // 닫기 버튼 클릭
        visitorStatsClose.addEventListener('click', () => {
            visitorStatsModal.classList.remove('active');
            visitorStatsModal.classList.add('hidden');
        });

        // 배경 클릭 시 모달 닫기
        visitorStatsModal.addEventListener('click', e => {
            if (e.target === visitorStatsModal) {
                visitorStatsModal.classList.remove('active');
                visitorStatsModal.classList.add('hidden');
            }
        });
    }


    // 뒤로가기 → /post 로 이동
    const backToPosts = document.getElementById('back-to-posts');
    if (backToPosts) {
        backToPosts.addEventListener('click', () => {
            window.location.href = '/post';
        });
    }


});


