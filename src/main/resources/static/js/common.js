let showAllMenus = false;
let menuIconToggled = false;
let hoveredMenu = null;
let showLoginModal = false;

function toggleMenu() {
    menuIconToggled = !menuIconToggled;
    const menuIcon = document.querySelector('.menuIcon');
    if (menuIcon) {
        menuIcon.textContent = menuIconToggled ? '🎣' : '☰';
    }

    showAllMenus = !showAllMenus;
    const fullMenuBox = document.querySelector('.fullMenuBox');
    if (fullMenuBox) {
        if (showAllMenus) {
            renderFullMenu();
            fullMenuBox.style.display = 'flex';
        } else {
            fullMenuBox.style.display = 'none';
        }
    }
}

function navigatePage(path) {
    window.location.href = path;
}

function showSubMenu(index) {
    const subMenu = document.getElementById(`subMenu${index}`);
    if (subMenu) {
        subMenu.style.display = 'block';
    }
}

function hideSubMenu(index) {
    const subMenu = document.getElementById(`subMenu${index}`);
    if (subMenu) {
        subMenu.style.display = 'none';
    }
}

function showLogin() {
    showLoginModal = true;
    const loginModalOverlay = document.getElementById('loginModalOverlay');
    if (loginModalOverlay) {
        loginModalOverlay.style.display = 'flex';
    }
}

function closeLogin() {
    showLoginModal = false;
    const loginModalOverlay = document.getElementById('loginModalOverlay');
    if (loginModalOverlay) {
        loginModalOverlay.style.display = 'none';
    }
}

function renderFullMenu() {
    const navigationItemsData = [
        { icon: '📅', label: '예약', path: '/reservation' },
        { icon: '🙋‍♂️', label: '낚시 파티원모집', path: '/fishing-party' },
        { icon: '👨‍👩‍👧‍👦', label: '지금 모집중!', path: '/recruiting' },
        { icon: '📋', label: '계획짜기', path: '/plan' },
        { icon: '💬', label: '커뮤니티', path: '/community' },
    ];

    const menuData = [
        { title: '예약하기', subMenus: ['예약 1', '예약 2', '예약 3'] },
        { title: '낚시파티모집', subMenus: ['모집 1', '모집 2'] },
        { title: '지금 모집중!', subMenus: ['모집 1', '모집 2'] },
        { title: '계획짜기!', subMenus: ['계획 1', '계획 2'] },
        {
            title: '날씨/풍속/조류!',
            subMenus: [
                { label: '날씨 1', path: '/weather' },
                { label: '풍속 1', path: '/wind' },
                { label: '조류 1', path: '/tide' }
            ]
        },
        { title: '커뮤니티', subMenus: ['커뮤니티 1', '커뮤니티 2'] },
    ];

    const fullMenuBox = document.querySelector('.fullMenuBox');
    if (!fullMenuBox) return;
    fullMenuBox.innerHTML = '';

    const navigationItemsContainer = document.createElement('div');
    navigationItemsContainer.className = 'navigationItemsContainer';

    navigationItemsData.forEach(item => {
        const button = document.createElement('button');
        button.className = 'navigationItem';
        button.onclick = () => navigatePage(item.path);
        button.innerHTML = `<span class="navigationItemIcon">${item.icon}</span><span class="navigationItemLabel">${item.label}</span>`;
        navigationItemsContainer.appendChild(button);
    });
    fullMenuBox.appendChild(navigationItemsContainer);

    menuData.forEach((menu, index) => {
        const menuSection = document.createElement('div');
        menuSection.className = 'fullMenuSection';
        menuSection.innerHTML = `<strong class="fullMenuTitle">${menu.title}</strong>`;
        const subMenuList = document.createElement('ul');
        subMenuList.className = 'fullMenuList';

        menu.subMenus.forEach(sub => {
            const listItem = document.createElement('li');
            listItem.className = 'fullMenuItem';
            const subButton = document.createElement('a');
            subButton.className = 'fullMenuButton';
            subButton.href = '#';
            subButton.style.textDecoration = 'none';

            if (typeof sub === 'string') {
                subButton.textContent = `- ${sub}`;
                subButton.onclick = (e) => {
                    e.preventDefault();
                    alert(`${sub} 페이지로 이동합니다.`);
                };
            } else {
                subButton.textContent = `- ${sub.label}`;
                subButton.onclick = (e) => {
                    e.preventDefault();
                    navigatePage(sub.path);
                };
            }

            listItem.appendChild(subButton);
            subMenuList.appendChild(listItem);
        });

        menuSection.appendChild(subMenuList);
        fullMenuBox.appendChild(menuSection);
    });
}


document.addEventListener('DOMContentLoaded', () => {
    renderFullMenu();

    const fullMenuBox = document.querySelector('.fullMenuBox');
    if (fullMenuBox) {
        fullMenuBox.style.display = 'none';
    }


    document.addEventListener('click', (event) => {
        const fullMenuBox = document.querySelector('.fullMenuBox');
        const menuIcon = document.querySelector('.menuIcon');

        if (!fullMenuBox || !showAllMenus) return;

        const isClickInsideMenu = fullMenuBox.contains(event.target);
        const isClickOnIcon = menuIcon && menuIcon.contains(event.target);

        if (!isClickInsideMenu && !isClickOnIcon) {
            fullMenuBox.style.display = 'none';
            showAllMenus = false;
            menuIconToggled = false;
            if (menuIcon) menuIcon.textContent = '☰';
        }
    });


    const videos = document.querySelectorAll('.videoSlider .video');
    let current = 0;

    function showVideo(index) {
        videos.forEach((video, i) => {
            video.classList.remove('active');
            if (i === index) {
                video.classList.add('active');
            }
        });
    }

    if (videos.length > 0) {
        showVideo(current);
        setInterval(() => {
            current = (current + 1) % videos.length;
            showVideo(current);
        }, 4000);
    }
});


function selectRegion(regionName) {
    alert(`선택된 해역: ${regionName}`);
}

function showSubMenu() {
  const submenu = event.currentTarget.querySelector(".submenu");
  if (submenu) submenu.style.display = "block";
}

function hideSubMenu() {
  const submenu = event.currentTarget.querySelector(".submenu");
  if (submenu) submenu.style.display = "none";
}

// ✅ 전역 바인딩해서 inline 이벤트에서 인식되도록
window.toggleMenu = toggleMenu;
window.navigatePage = navigatePage;
window.showSubMenu = showSubMenu;
window.hideSubMenu = hideSubMenu;
