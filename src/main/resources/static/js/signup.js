document.addEventListener('DOMContentLoaded', () => {
    const nicknameInput = document.getElementById('nickname');
    const submitBtn     = document.getElementById('complete-btn');
    const nicknameError = document.getElementById('nickname-error');
    const nicknameRegex = /^[가-힣]{2,10}$/;
    const phoneInput = document.getElementById('phone');


    phoneInput.addEventListener('input', function(e) {
        let digits = e.target.value.replace(/\D/g, '');

        if (digits.length <= 3) {
            e.target.value = digits;
        }
        else if (digits.length <= 7) {
            e.target.value = digits.slice(0, 3)
                + '-'
                + digits.slice(3);
        }
        else {
            e.target.value = digits.slice(0, 3)
                + '-'
                + digits.slice(3, 7)
                + '-'
                + digits.slice(7, 11);
        }
    });



    async function checkDuplicate(value) {
        try {
            const res = await fetch(`/api/users/check-nickname?nickname=${encodeURIComponent(value)}`);
            const json = await res.json();
            return json.exists;
        } catch (e) {
            console.error('닉네임 중복 확인 실패', e);
            return false;
        }
    }

    async function updateButtonState() {
        const v = nicknameInput.value.trim();

        if (!nicknameRegex.test(v)) {
            nicknameError.textContent = '닉네임은 한글 2~10자만 사용 가능합니다.';
            nicknameError.classList.add('visible');
            submitBtn.disabled = true;
            return;
        }

        const isDup = await checkDuplicate(v);
        if (isDup) {
            nicknameError.textContent = '이미 사용 중인 닉네임입니다.';
            nicknameError.classList.add('visible');
            submitBtn.disabled = true;
        } else {
            nicknameError.textContent = '';
            nicknameError.classList.remove('visible');
            submitBtn.disabled = false;
        }
    }

    submitBtn.disabled = true;
    nicknameInput.addEventListener('input', updateButtonState);

    // 로드 확인용 디버그 로그
    console.log('▶ signup.js loaded');

    // 프로필 미리보기 로직
    const input = document.getElementById('profile-input');
    const preview = document.getElementById('profile-preview');

    if (!input || !preview) {
        console.warn('signup.js: profile-input 또는 profile-preview를 찾을 수 없습니다.');
    } else {
        input.addEventListener('change', (e) => {
            const file = e.target.files[0];
            if (!file) return;

            // 기존 내용 제거
            preview.innerHTML = '';

            // 이미지 태그 생성 및 삽입
            const img = document.createElement('img');
            img.src = URL.createObjectURL(file);
            img.onload = () => URL.revokeObjectURL(img.src);
            img.style.maxWidth = '100%';
            img.style.maxHeight = '100%';
            img.alt = '프로필 미리보기';
            preview.appendChild(img);
        });
    }

});
