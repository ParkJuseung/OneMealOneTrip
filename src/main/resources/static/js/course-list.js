

//썸네일 출력
document.addEventListener('DOMContentLoaded', function () {
    const thumbnailContainers = document.querySelectorAll('.js-thumbnail-container');

    thumbnailContainers.forEach(container => {
        const routeId = container.dataset.id;

        // 썸네일 요청 → 실제 이미지 URL 받아오기
        fetch(`/api/travel/thumbnail?routeId=${routeId}`) //백틱 주의 없으면 이미지 로드 안됨!
            .then(res => res.json())
            .then(data => {
                // data.url은 다시 JSON을 주는 API임 → 다시 fetch
                return fetch(data.url);
            })
            .then(res => res.json())
            .then(photoData => {
                const img = document.createElement('img');
                img.src = photoData.url; // 실제 Google CDN 이미지 URL
                img.alt = '썸네일';
                img.className = 'course-image';

                // ✅ 이미지 로드 실패 시 fallback
                img.onerror = function () {
                    this.onerror = null;
                    this.src = '/images/default-thumbnail.jpg';
                };
                container.insertBefore(img, container.firstChild);
            })
            .catch(err => {
                console.error('썸네일 fetch 실패:', err);
                const fallback = document.createElement('img');
                fallback.src = '/images/default-thumbnail.jpg';
                fallback.alt = '기본 썸네일';
                fallback.className = 'course-image';
                container.insertBefore(fallback, container.firstChild);
            });
    });


    // 뒤로가기 버튼
    const backButton = document.getElementById('back-button');
    backButton.addEventListener('click', function() {
        window.history.back();
    });

    // 좋아요 버튼 토글
    const likeButtons = document.querySelectorAll('.course-like');
    likeButtons.forEach(button => {
        button.addEventListener('click', function(e) {
            e.stopPropagation(); // 카드 클릭 이벤트 중지

            const icon = this.querySelector('i');

            if (icon.classList.contains('far')) {
                icon.classList.remove('far');
                icon.classList.add('fas');
                this.classList.add('active');
            } else {
                icon.classList.remove('fas');
                icon.classList.add('far');
                this.classList.remove('active');
            }
        });
    });

    // 필터 태그 토글
    const filterTags = document.querySelectorAll('.filter-tag');
    filterTags.forEach(tag => {
        tag.addEventListener('click', function() {
            filterTags.forEach(t => t.classList.remove('active'));
            this.classList.add('active');

            // 여기에 필터링 로직 추가 가능
            // 예: 선택된 필터에 따라 코스 카드를 필터링
        });
    });

    // 코스 카드 클릭 이벤트 (상세 페이지로 이동)
    const courseCards = document.querySelectorAll('.course-card');
    courseCards.forEach(card => {
        card.addEventListener('click', function() {

            const courseId = this.getAttribute('data-id');

            fetch(`/api/travel/${courseId}/views`, {
                method: "PATCH"
            })
                .then(() => {
                    window.location.href = `/travel/${courseId}`;
                })
                .catch(err => {
                    // 실패하더라도 상세 페이지 이동
                    window.location.href = `/travel/${courseId}`;
                });

        });
    });

    // 페이지네이션 클릭 이벤트
    const pageItems = document.querySelectorAll('.page-item');
    pageItems.forEach(item => {
        item.addEventListener('click', function() {
            pageItems.forEach(p => p.classList.remove('active'));
            this.classList.add('active');

            // 실제로는 페이지 전환 로직 추가
            // 예: 페이지 번호에 따라 새로운 코스 데이터 로드
        });
    });

    // 정렬 옵션 클릭 이벤트 (실제로는 드롭다운 메뉴 구현 필요)
    const sortButton = document.querySelector('.sort-button');
    sortButton.addEventListener('click', function() {
        // 정렬 드롭다운 메뉴 토글 로직 (예시)
        alert('정렬 옵션: 최신순, 인기순, 조회순');

        // 여기에 실제 정렬 기능 구현 가능
    });

    // 채팅방 생성 버튼 클릭 이벤트
    const createChatBtn = document.getElementById('create-chat-btn');
    if (createChatBtn) {
        createChatBtn.addEventListener('click', function() {
            alert('새 채팅방 생성 기능이 곧 추가될 예정입니다!');

            // 여기에 채팅방 생성 모달 표시 로직 추가 가능
        });
    }

    // 검색 기능
    const searchInput = document.querySelector('.search-input');
    if (searchInput) {
        searchInput.addEventListener('input', function() {
            const searchTerm = this.value.toLowerCase();

            // 여기에 검색 로직 추가 가능
            // 예: 검색어와 일치하는 코스 카드만 표시

            // 간단한 프론트엔드 검색 구현 (실제로는 서버에서 처리하는 것이 좋음)
            if (searchTerm.length > 0) {
                courseCards.forEach(card => {
                    const title = card.querySelector('.course-title').textContent.toLowerCase();
                    const tags = Array.from(card.querySelectorAll('.course-tag')).map(tag => tag.textContent.toLowerCase());

                    // 제목이나 태그에 검색어가 포함되어 있으면 표시, 아니면 숨김
                    if (title.includes(searchTerm) || tags.some(tag => tag.includes(searchTerm))) {
                        card.style.display = 'block';
                    } else {
                        card.style.display = 'none';
                    }
                });
            } else {
                // 검색어가 없으면 모두 표시
                courseCards.forEach(card => {
                    card.style.display = 'block';
                });
            }
        });
    }

});