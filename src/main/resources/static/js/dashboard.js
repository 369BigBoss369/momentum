function resetLoadingScreen() {
    const loadingScreen = document.getElementById('loadingScreen');
    if (loadingScreen) {
        loadingScreen.classList.remove('active', 'nutrition-gradient', 'fitness-gradient', 'default-gradient');
    }
    const transitionOverlay = document.getElementById('transitionOverlay');
    if (transitionOverlay) {
        transitionOverlay.classList.remove('active', 'expanding', 'nutrition-gradient', 'fitness-gradient');
        transitionOverlay.style.width = '0';
        transitionOverlay.style.height = '0';
        transitionOverlay.style.left = '';
        transitionOverlay.style.top = '';
    }
}

function initHubNavigation() {
    resetLoadingScreen();
    
    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', initHubNavigation);
        return;
    }
    
    const nutritionLink = document.getElementById('nutritionHubLink');
    const fitnessLink = document.getElementById('fitnessHubLink');
    const transitionOverlay = document.getElementById('transitionOverlay');
    const loadingScreen = document.getElementById('loadingScreen');
    
    if (!nutritionLink || !fitnessLink || !transitionOverlay || !loadingScreen) {
        console.warn('Hub navigation elements not found');
        return;
    }
    
    function handleNavigation(event, url, gradientClass) {
        event.preventDefault();
        event.stopPropagation();
        
        transitionOverlay.classList.remove('nutrition-gradient', 'fitness-gradient');
        loadingScreen.classList.remove('nutrition-gradient', 'fitness-gradient');
        
        transitionOverlay.classList.remove('active', 'expanding');
        loadingScreen.classList.remove('active');
        
        const x = event.clientX;
        const y = event.clientY;
        
        transitionOverlay.style.left = x + 'px';
        transitionOverlay.style.top = y + 'px';

        const corners = [
            {x: 0, y: 0},
            {x: window.innerWidth, y: 0},
            {x: 0, y: window.innerHeight},
            {x: window.innerWidth, y: window.innerHeight}
        ];
        
        let maxRadius = 0;
        corners.forEach(corner => {
            const distance = Math.sqrt(
                Math.pow(corner.x - x, 2) + 
                Math.pow(corner.y - y, 2)
            );
            if (distance > maxRadius) {
                maxRadius = distance;
            }
        });
        
        maxRadius += 100;
        
        transitionOverlay.style.width = '0px';
        transitionOverlay.style.height = '0px';
        transitionOverlay.classList.remove('nutrition-gradient', 'fitness-gradient', 'active', 'expanding');
        
        void transitionOverlay.offsetWidth;
        
        transitionOverlay.style.left = x + 'px';
        transitionOverlay.style.top = y + 'px';
        
        transitionOverlay.classList.add(gradientClass);

        transitionOverlay.style.setProperty('--max-radius', maxRadius + 'px');
        
        void transitionOverlay.offsetWidth;
        
        transitionOverlay.classList.add('active');
        
        requestAnimationFrame(() => {
            requestAnimationFrame(() => {
                transitionOverlay.classList.add('expanding');
            });
        });
        
        setTimeout(() => {
            loadingScreen.classList.remove('nutrition-gradient', 'fitness-gradient', 'default-gradient');
            loadingScreen.classList.add(gradientClass);
            loadingScreen.classList.add('active');
        }, 900);
        
        setTimeout(() => {
            window.location.href = url;
        }, 1200);
    }
    
    nutritionLink.addEventListener('click', function(e) {
        e.stopImmediatePropagation();
        handleNavigation(e, '/nutrition/dashboard', 'nutrition-gradient');
    });
    
    fitnessLink.addEventListener('click', function(e) {
        e.stopImmediatePropagation();
        handleNavigation(e, '/fitness/dashboard', 'fitness-gradient');
    });
}

if (document.readyState === 'complete' || document.readyState === 'interactive') {
    resetLoadingScreen();
} else {
    window.addEventListener('load', function() {
        resetLoadingScreen();
    });
}

document.addEventListener('DOMContentLoaded', function() {
    if (document.querySelector('.dashboard-page') ||
        document.querySelector('[data-page="dashboard"]') ||
        window.location.pathname === '/dashboard') {
        console.log('Dashboard page detected, initializing hub navigation');
        setTimeout(initHubNavigation, 100);
    } else {
        console.log('Not on dashboard page, skipping hub navigation initialization');
    }
});

document.addEventListener('visibilitychange', function() {
    if (!document.hidden) {
        resetLoadingScreen();
    }
});

window.addEventListener('pageshow', function(event) {
    if (event.persisted) {
        resetLoadingScreen();
    }
});