document.addEventListener('DOMContentLoaded', () => {
    const nicknameInput = document.getElementById('nickname');
    const submitBtn = document.getElementById('complete-btn');
    const nicknameError = document.getElementById('nickname-error');
    const phoneInput = document.getElementById('phone');

    if (!nicknameInput || !submitBtn || !nicknameError || !phoneInput) {
        console.warn('⚠ signup.js: 회원가입 폼이 아니므로 스크립트를 종료합니다.');
        return;
    }

    const nicknameRegex = /^[가-힣]{2,10}$/;

    phoneInput.addEventListener('input', function (e) {
        let digits = e.target.value.replace(/\D/g, '');
        if (digits.length <= 3) {
            e.target.value = digits;
        } else if (digits.length <= 7) {
            e.target.value = digits.slice(0, 3) + '-' + digits.slice(3);
        } else {
            e.target.value = digits.slice(0, 3) + '-' + digits.slice(3, 7) + '-' + digits.slice(7, 11);
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

    console.log('▶ signup.js loaded');

    // 프로필 미리보기
    const input = document.getElementById('profile-input');
    const preview = document.getElementById('profile-preview');

    if (input && preview) {
        input.addEventListener('change', (e) => {
            const file = e.target.files[0];
            if (!file) return;
            preview.innerHTML = '';
            const img = document.createElement('img');
            img.src = URL.createObjectURL(file);
            img.onload = () => URL.revokeObjectURL(img.src);
            preview.appendChild(img);
        });
    }
});
