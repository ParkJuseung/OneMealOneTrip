// 프로필 드롭다운 기능
function toggleDropdown() {
    const dropdown = document.getElementById('profileDropdown');
    const overlay = document.getElementById('dropdownOverlay');
    const profileArea = document.querySelector('.profile-area');

    if (!dropdown || !overlay || !profileArea) {
        return;
    }

    const isVisible = dropdown.classList.contains('show');

    if (isVisible) {
        closeDropdown();
    } else {
        dropdown.classList.add('show');
        overlay.classList.add('show');
        profileArea.classList.add('active');
    }
}

function closeDropdown() {
    const dropdown = document.getElementById('profileDropdown');
    const overlay = document.getElementById('dropdownOverlay');
    const profileArea = document.querySelector('.profile-area');

    if (dropdown) dropdown.classList.remove('show');
    if (overlay) overlay.classList.remove('show');
    if (profileArea) profileArea.classList.remove('active');
}

// DOM 로드 후 이벤트 리스너 등록
document.addEventListener('DOMContentLoaded', function() {
    // ESC 키로 드롭다운 닫기
    document.addEventListener('keydown', function(event) {
        if (event.key === 'Escape') {
            closeDropdown();
        }
    });

    // 프로필 드롭다운 영역 클릭 시 이벤트 버블링 방지
    const profileDropdown = document.querySelector('.profile-dropdown');
    if (profileDropdown) {
        profileDropdown.addEventListener('click', function(event) {
            event.stopPropagation();
        });
    }

    // 문서 전체 클릭 시 드롭다운 닫기
    document.addEventListener('click', function() {
        closeDropdown();
    });
});