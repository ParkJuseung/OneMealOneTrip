// navbar-toggle.js
document.addEventListener('DOMContentLoaded', function() {
    // 1. 필요한 요소 찾기
    const addButtonContainer = document.querySelector('.nav-item:nth-child(3)');
    /*const addButton = addButtonContainer.querySelector('.add-button');*/

    // 2. 오버레이 요소 생성
    const overlay = document.createElement('div');
    overlay.className = 'overlay';
    document.body.appendChild(overlay);

    // 3. 토글 메뉴 컨테이너 생성
    const toggleMenu = document.createElement('div');
    toggleMenu.className = 'toggle-menu';

    // 4. 메뉴 아이템 생성
    const menuItem1 = document.createElement('div');
    menuItem1.className = 'menu-item';
    menuItem1.innerHTML = '<i class="fas fa-camera"></i>';


    const menuItem2 = document.createElement('div');
    menuItem2.className = 'menu-item';
    menuItem2.innerHTML = '<i class="fas fa-edit"></i>';
    menuItem2.addEventListener('click', () => {
        location.href = '/post/create';
    });

    // 5. 메뉴 아이템 추가
    toggleMenu.appendChild(menuItem1);
    toggleMenu.appendChild(menuItem2);

    // 6. 토글 메뉴를 .add-button 앞에 추가 (DOM에서 형제 요소로)
    addButtonContainer.insertBefore(toggleMenu, addButton);

    // 7. CSS 스타일 추가
    const style = document.createElement('style');
    style.textContent = `
    .nav-item {
      position: relative;
    }
    
    .add-button {
      width: 56px;
      height: 56px;
      border-radius: 50%;
      background-color: #2DD4BF;
      display: flex;
      justify-content: center;
      align-items: center;
      box-shadow: 0 2px 10px rgba(0, 0, 0, 0.15);
      cursor: pointer;
      transition: background-color 0.3s;
      position: relative;
      z-index: 11;
      margin-bottom: 16px;
    }
    
    .add-button.active {
      background-color: #5ED3C8;
    }
    
    .add-button.active i.fas.fa-plus {
      display: none;
    }
    
    .add-button.active::before,
    .add-button.active::after {
      content: '';
      position: absolute;
      width: 16px;
      height: 2px;
      background-color: white;
      top: 50%;
      left: 50%;
    }
    
    .add-button.active::before {
      transform: translate(-50%, -50%) rotate(45deg);
    }
    
    .add-button.active::after {
      transform: translate(-50%, -50%) rotate(-45deg);
    }
    
    .toggle-menu {
      position: absolute;
      bottom: 80px;
      left: 50%;
      transform: translateX(-50%) scale(0);
      display: flex;
      flex-direction: column;
      align-items: center;
      opacity: 0;
      transition: transform 0.3s, opacity 0.3s;
      z-index: 10;
      pointer-events: none;
    }
    
    .toggle-menu.active {
      transform: translateX(-50%) scale(1);
      opacity: 1;
      pointer-events: auto;
    }
    
    .menu-item {
      width: 52px;
      height: 52px;
      border-radius: 50%;
      background-color: #E6FFFA;
      color: #2DD4BF;
      display: flex;
      justify-content: center;
      align-items: center;
      box-shadow: none;
      margin-bottom: 20px;
      cursor: pointer;
      font-size: 22px;
      transition: background-color 0.2s;
    }
    
    .menu-item:hover {
      background-color: #B2F5EA;
    }
    
    .overlay {
      position: fixed;
      top: 0;
      left: 0;
      right: 0;
      bottom: 0;
      background-color: rgba(0, 0, 0, 0.3);
      opacity: 0;
      visibility: hidden;
      transition: opacity 0.3s;
      z-index: 9;
    }
    
    .overlay.active {
      opacity: 1;
      visibility: visible;
    }
  `;

    document.head.appendChild(style);

    // 8. 토글 기능 구현
    let isOpen = false;

    function toggleMenuState() {
        isOpen = !isOpen;

        if (isOpen) {
            addButton.classList.add('active');
            toggleMenu.classList.add('active');
            overlay.classList.add('active');
        } else {
            addButton.classList.remove('active');
            toggleMenu.classList.remove('active');
            overlay.classList.remove('active');
        }
    }

    // 9. 이벤트 리스너 등록
    addButton.addEventListener('click', toggleMenuState);

    overlay.addEventListener('click', () => {
        if (isOpen) toggleMenuState();
    });

    // 10. 메뉴 아이템 이벤트 처리
    menuItem1.addEventListener('click', () => {
        console.log('카메라 메뉴 클릭됨');
        toggleMenuState();
        // 여기에 카메라 기능 구현
    });

    menuItem2.addEventListener('click', () => {
        console.log('편집 메뉴 클릭됨');
        toggleMenuState();
        // 여기에 편집 기능 구현
    });
});