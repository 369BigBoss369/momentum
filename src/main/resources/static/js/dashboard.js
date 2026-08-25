// Dashboard-specific JavaScript

// Reset loading screen on page load
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

// Hub Navigation with Circle Transition
function initHubNavigation() {
    // Reset any existing loading states first
    resetLoadingScreen();
    
    // Wait for everything to be loaded
    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', initHubNavigation);
        return;
    }
    
    const nutritionLink = document.getElementById('nutritionHubLink');
    const fitnessLink = document.getElementById('fitnessHubLink');
    const transitionOverlay = document.getElementById('transitionOverlay');
    const loadingScreen = document.getElementById('loadingScreen');
    
    // Check if elements exist
    if (!nutritionLink || !fitnessLink || !transitionOverlay || !loadingScreen) {
        console.warn('Hub navigation elements not found');
        return;
    }
    
    function handleNavigation(event, url, gradientClass) {
        event.preventDefault();
        event.stopPropagation();
        
        // Remove any existing gradient classes
        transitionOverlay.classList.remove('nutrition-gradient', 'fitness-gradient');
        loadingScreen.classList.remove('nutrition-gradient', 'fitness-gradient');
        
        // Reset overlay state
        transitionOverlay.classList.remove('active', 'expanding');
        loadingScreen.classList.remove('active');
        
        // Get click position relative to viewport
        const x = event.clientX;
        const y = event.clientY;
        
        // Set the circle center position
        transitionOverlay.style.left = x + 'px';
        transitionOverlay.style.top = y + 'px';
        
        // Calculate the maximum radius needed to cover entire screen
        // We need the farthest distance from click point to any corner
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
        
        // Add some padding to ensure full coverage
        maxRadius += 100;
        
        // Reset overlay completely first
        transitionOverlay.style.width = '0px';
        transitionOverlay.style.height = '0px';
        transitionOverlay.classList.remove('nutrition-gradient', 'fitness-gradient', 'active', 'expanding');
        
        // Force reflow
        void transitionOverlay.offsetWidth;
        
        // Set position first
        transitionOverlay.style.left = x + 'px';
        transitionOverlay.style.top = y + 'px';
        
        // Add gradient class
        transitionOverlay.classList.add(gradientClass);
        
        // Set the radius as CSS variable (diameter will be radius * 2)
        const diameter = maxRadius * 2;
        transitionOverlay.style.setProperty('--max-radius', maxRadius + 'px');
        
        // Force reflow again
        void transitionOverlay.offsetWidth;
        
        // Make visible first (with opacity transition)
        transitionOverlay.classList.add('active');
        
        // Wait a tiny bit, then trigger expansion
        requestAnimationFrame(() => {
            requestAnimationFrame(() => {
                transitionOverlay.classList.add('expanding');
            });
        });
        
        // Show loading screen after circle expands
        setTimeout(() => {
            // Remove any existing gradient classes
            loadingScreen.classList.remove('nutrition-gradient', 'fitness-gradient', 'default-gradient');
            loadingScreen.classList.add(gradientClass);
            loadingScreen.classList.add('active');
        }, 900);
        
        // Navigate after transition completes
        setTimeout(() => {
            window.location.href = url;
        }, 1200);
    }
    
    nutritionLink.addEventListener('click', function(e) {
        // Stop event from bubbling to global navigation handler
        e.stopImmediatePropagation();
        handleNavigation(e, '/nutrition/dashboard', 'nutrition-gradient');
    });
    
    fitnessLink.addEventListener('click', function(e) {
        // Stop event from bubbling to global navigation handler
        e.stopImmediatePropagation();
        handleNavigation(e, '/fitness/dashboard', 'fitness-gradient');
    });
}

// Initialize loading screen reset when page loads
if (document.readyState === 'complete' || document.readyState === 'interactive') {
    resetLoadingScreen();
} else {
    window.addEventListener('load', function() {
        resetLoadingScreen();
    });
}

// Initialize dashboard-specific features only if on dashboard page
document.addEventListener('DOMContentLoaded', function() {
    // Only initialize hub navigation if we're on the dashboard page
    if (document.querySelector('.dashboard-page') ||
        document.querySelector('[data-page="dashboard"]') ||
        window.location.pathname === '/dashboard') {
        console.log('Dashboard page detected, initializing hub navigation');
        setTimeout(initHubNavigation, 100);
    } else {
        console.log('Not on dashboard page, skipping hub navigation initialization');
    }
});

// Also reset on page visibility change (when user navigates back)
document.addEventListener('visibilitychange', function() {
    if (!document.hidden) {
        resetLoadingScreen();
    }
});

// Reset on pageshow (handles back button navigation)
window.addEventListener('pageshow', function(event) {
    if (event.persisted) {
        resetLoadingScreen();
    }
});

