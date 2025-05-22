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
    const textarea = document.getElementById('intro-content');
    const charCount = document.getElementById('intro-char-count');
    const tagInput = document.getElementById('intro-tag-input');
    const tagList = document.getElementById('intro-new-tags');

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
        deleteBtn.addEventListener('click', () => {
            if (confirm('정말로 나의 소개를 삭제하시겠습니까?')) {
                viewMode.classList.add('hidden');
                viewEmpty.classList.remove('hidden');
                alert('나의 소개가 삭제되었습니다.');
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
            if (e.key === 'Enter') {
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

    // 저장 버튼 클릭
    if (saveBtn) {
        saveBtn.addEventListener('click', () => {
            const content = textarea.value.trim();
            if (!content) {
                alert('소개글을 입력해주세요.');
                return;
            }

            // 데모용 저장 처리
            document.querySelector('#intro-view .introduce-content').textContent = content;

            // 태그 업데이트
            const tagContainer = document.querySelector('#intro-view .tag-list');
            tagContainer.innerHTML = '';
            tags.forEach(tag => {
                const span = document.createElement('span');
                span.className = 'interest-tag';
                span.textContent = `#${tag}`;
                tagContainer.appendChild(span);
            });

            editMode.classList.add('hidden');
            viewMode.classList.remove('hidden');
            alert('나의 소개가 저장되었습니다!');
        });
    }
});


