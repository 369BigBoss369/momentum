// Landing Page JavaScript

document.addEventListener('DOMContentLoaded', function() {
    // Initialize AOS (Animate On Scroll) - but skip on create-food page
    const isCreateFoodPage = window.location.pathname.includes('/nutrition/create-food');
    if (!isCreateFoodPage && typeof AOS !== 'undefined') {
        AOS.init({
            duration: 1000,
            once: true,
            offset: 100
        });
    }

    // Initialize loading screen
    initLoadingScreen();
    
    // Initialize global navigation handler
    initGlobalNavigation();
    
    // Initialize navigation
    initNavigation();
    
    // Initialize counter animations
    initCounters();
    
    // Initialize scroll effects
    initScrollEffects();
    
    // Initialize interactive elements
    initInteractiveElements();
});

// Get gradient class based on URL path
function getGradientClass(path) {
    const lowerPath = path.toLowerCase();
    if (lowerPath.includes('nutrition')) {
        return 'nutrition-gradient';
    } else if (lowerPath.includes('fitness')) {
        return 'fitness-gradient';
    }
    return 'default-gradient';
}

// Get gradient background value based on class name
// Uses the same CSS variables as the hub navigation cards for perfect color matching
function getGradientBackground(gradientClass) {
    // Read from CSS custom properties to ensure exact match with card icons
    const root = document.documentElement;
    let gradient;
    
    if (gradientClass === 'nutrition-gradient') {
        // Use the same CSS variable as nutrition-hub-card icon
        gradient = getComputedStyle(root).getPropertyValue('--gradient-success').trim();
        if (!gradient) {
            gradient = 'linear-gradient(135deg, #11998e 0%, #38ef7d 100%)';
        }
    } else if (gradientClass === 'fitness-gradient') {
        // Use the same explicit gradient as fitness-hub-card icon
        gradient = 'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)';
    } else {
        // Default gradient
        gradient = getComputedStyle(root).getPropertyValue('--gradient-primary').trim();
        if (!gradient) {
            gradient = 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)';
        }
    }
    
    return gradient;
}

// Show loading screen with appropriate gradient
function showLoadingScreen(targetPath) {
    const gradientClass = getGradientClass(targetPath);
    const gradientBackground = getGradientBackground(gradientClass);
    
    // Get or create loading element
    let loading = document.getElementById('loadingScreen');
    
    if (!loading) {
        loading = document.getElementById('pageLoadingScreen');
    }
    
    if (!loading) {
        // Create loading div for pages that don't have it
        loading = document.createElement('div');
        loading.className = 'loading-screen';
        loading.id = 'pageLoadingScreen';
        loading.innerHTML = '<div class="loading-spinner"></div>';
        document.body.appendChild(loading);
    }
    
    // Remove all gradient classes and add the appropriate one
    loading.classList.remove('nutrition-gradient', 'fitness-gradient', 'default-gradient');
    loading.classList.add(gradientClass);
    
    // Set background gradient inline to ensure it's visible immediately
    loading.style.background = gradientBackground;
    
    // Show loading screen INSTANTLY - no transition delay
    loading.classList.remove('hidden');
    // Force immediate display with inline styles (no CSS transition when showing)
    loading.style.transition = 'none';
    loading.style.opacity = '1';
    loading.style.pointerEvents = 'all';
    loading.classList.add('active');
    
    // Re-enable transition after a tiny delay for future fade-out
    setTimeout(() => {
        loading.style.transition = '';
    }, 50);
}

// Hide loading screen
function hideLoadingScreen() {
    const loading = document.getElementById('loadingScreen') || document.getElementById('pageLoadingScreen');
    if (loading) {
        // Clear inline opacity and pointer-events to allow CSS transition to work
        // Keep background inline style to maintain gradient during fade-out
        loading.style.opacity = '';
        loading.style.pointerEvents = '';
        // Remove active class to trigger fade-out transition
        loading.classList.remove('active');
        // Mark as hidden after transition completes (400ms transition + small buffer)
        setTimeout(() => {
            loading.classList.add('hidden');
            // Clear inline background after hiding so CSS classes take over next time
            loading.style.background = '';
        }, 450);
    }
}

// Loading Screen
function initLoadingScreen() {
    // Check if page was restored from cache - if so, don't show loading screen
    // (pageshow event will handle hiding it if it's already visible)
    const loading = document.getElementById('loadingScreen') || document.getElementById('pageLoadingScreen');
    
    // If page is already complete and loading screen exists but is hidden, don't show it
    if (document.readyState === 'complete' && loading && !loading.classList.contains('active')) {
        // Page was likely restored from cache - pageshow handler will manage it
        return;
    }
    
    // Check URL to determine which gradient to use on initial load
    const currentPath = window.location.pathname;
    const gradientClass = getGradientClass(currentPath);
    
    // Check if loading-screen div exists (from layout.html)
    let loadingEl = loading;
    
    if (!loadingEl) {
        // Create loading div for pages that don't use layout.html (like index page)
        loadingEl = document.createElement('div');
        loadingEl.className = 'loading-screen';
        loadingEl.id = 'pageLoadingScreen';
        loadingEl.innerHTML = '<div class="loading-spinner"></div>';
        document.body.appendChild(loadingEl);
    }
    
    // Apply the appropriate gradient class
    loadingEl.classList.remove('nutrition-gradient', 'fitness-gradient', 'default-gradient');
    loadingEl.classList.add(gradientClass);
    
    // Set background gradient inline to ensure it's visible
    const gradientBackground = getGradientBackground(gradientClass);
    loadingEl.style.background = gradientBackground;
    
    // Only show loading screen if page is not already fully loaded
    // (if it's complete, it was likely restored from cache)
    if (document.readyState !== 'complete') {
        // Always show loading screen on page load INSTANTLY - no transition delay
        loadingEl.style.transition = 'none';
        loadingEl.style.opacity = '1';
        loadingEl.style.pointerEvents = 'all';
        loadingEl.classList.remove('hidden');
        loadingEl.classList.add('active');
        
        // Re-enable transition after a tiny delay for future fade-out
        setTimeout(() => {
            loadingEl.style.transition = '';
        }, 50);
    }
    
    // Hide loading screen when page is ready
    // Don't wait for all images to load - hide after DOM and critical resources are ready
    function hideWhenReady() {
        // If DOM is already complete, hide immediately
        if (document.readyState === 'complete') {
            setTimeout(() => {
                hideLoadingScreen();
            }, 300);
        } else if (document.readyState === 'interactive') {
            // DOM is ready, hide after a brief delay
            setTimeout(() => {
                hideLoadingScreen();
            }, 300);
        } else {
            // Wait for DOMContentLoaded, then hide
            document.addEventListener('DOMContentLoaded', function() {
                setTimeout(() => {
                    hideLoadingScreen();
                }, 300);
            }, { once: true });
            
            // Fallback: Also listen for load event with a timeout
            // But set a maximum timeout so it doesn't wait forever for slow images
            let loadTimeout;
            const loadHandler = function() {
                clearTimeout(loadTimeout);
                setTimeout(() => {
                    hideLoadingScreen();
                }, 100);
            };
            
            window.addEventListener('load', loadHandler, { once: true });
            
            // Maximum timeout: hide loading screen after 2 seconds even if images aren't loaded
            loadTimeout = setTimeout(function() {
                window.removeEventListener('load', loadHandler);
                hideLoadingScreen();
            }, 2000);
        }
    }
    
    hideWhenReady();
}

// Handle browser back/forward navigation (pageshow event fires for cached pages)
window.addEventListener('pageshow', function(event) {
    // If page was loaded from cache (bfcache), ensure loading screen is hidden
    if (event.persisted) {
        // Page was restored from cache - hide loading screen immediately
        const loading = document.getElementById('loadingScreen') || document.getElementById('pageLoadingScreen');
        if (loading) {
            // Hide immediately without transition
            loading.style.transition = 'none';
            loading.style.opacity = '0';
            loading.style.pointerEvents = 'none';
            loading.style.background = '';
            loading.classList.remove('active', 'nutrition-gradient', 'fitness-gradient', 'default-gradient');
            loading.classList.add('hidden');
            
            // Re-enable transition after hiding
            setTimeout(() => {
                loading.style.transition = '';
            }, 50);
        }
    } else {
        // Normal page load - initLoadingScreen will handle it
        // But ensure it runs if DOMContentLoaded already fired
        if (document.readyState === 'complete' || document.readyState === 'interactive') {
            // Already loaded, ensure loading screen hides
            setTimeout(() => {
                hideLoadingScreen();
            }, 100);
        }
    }
});

// Global navigation handler
function initGlobalNavigation() {
    // Intercept all link clicks
    document.addEventListener('click', function(e) {
        const link = e.target.closest('a');
        
        if (!link || !link.href) return;
        
        // Skip if it's not a same-origin link, or has special attributes
        try {
            const url = new URL(link.href, window.location.origin);
            
            // Only handle same-origin navigation
            if (url.origin !== window.location.origin) return;
            
            // Skip if it has target="_blank" or download attribute
            if (link.target === '_blank' || link.hasAttribute('download')) return;
            
            // Skip if it's a hash-only link (e.g., dashboard#, #section) - same pathname, only hash changes
            if (url.pathname === window.location.pathname && url.hash) {
                // Allow default behavior for hash links (anchor links, collapse menus, etc.)
                return;
            }
            
            // Skip if the href is just a hash (e.g., href="#")
            if (link.getAttribute('href') === '#' || link.getAttribute('href') === '#!') {
                return;
            }
            
            // Skip if it's a javascript: or mailto: link
            if (link.protocol === 'javascript:' || link.protocol === 'mailto:') return;
            
            // Skip if it's a data-toggle collapse button (Bootstrap navbar)
            if (link.hasAttribute('data-bs-toggle') && link.getAttribute('data-bs-toggle') === 'collapse') {
                return;
            }
            
            // Skip if it's a dropdown toggle
            if (link.hasAttribute('data-bs-toggle') && link.getAttribute('data-bs-toggle') === 'dropdown') {
                return;
            }
            
            // Only show loading if pathname actually changes
            if (url.pathname === window.location.pathname) {
                return;
            }
            
            // Prevent default navigation
            e.preventDefault();
            
            // Show loading screen with appropriate gradient IMMEDIATELY (synchronously)
            showLoadingScreen(url.pathname);
            
            // Force a synchronous reflow to ensure loading screen is rendered before navigation
            const loadingElement = document.getElementById('loadingScreen') || document.getElementById('pageLoadingScreen');
            if (loadingElement) {
                void loadingElement.offsetWidth; // Force layout calculation
            }
            
            // Navigate immediately - loading screen is already visible
            window.location.href = link.href;
            
        } catch (err) {
            // If URL parsing fails, allow default behavior
            return;
        }
    });
    
    // Handle form submissions
    document.addEventListener('submit', function(e) {
        const form = e.target;
        const action = form.action || window.location.href;
        
        if (action) {
            showLoadingScreen(action);
        }
    });
    
    // Note: popstate is removed - we don't show loading screen on back/forward
    // The pageshow event handler will ensure loading screen is hidden for cached pages
}

// Navigation
function initNavigation() {
    const navbar = document.getElementById('mainNav');
    const navLinks = document.querySelectorAll('a.nav-link');
    
    // Navbar scroll effect
    window.addEventListener('scroll', function() {
        if (window.scrollY > 100) {
            navbar.classList.add('scrolled');
        } else {
            navbar.classList.remove('scrolled');
        }
    });
    
    // Smooth scroll for navigation links
    navLinks.forEach(link => {
        link.addEventListener('click', function(e) {
            const href = this.getAttribute('href') || '';
            if (href.startsWith('#') && href.length > 1) {
                e.preventDefault();
                const target = document.querySelector(href);
                if (target) {
                    target.scrollIntoView({
                        behavior: 'smooth',
                        block: 'start'
                    });
                }
            }
        });
    });
}

// Counter Animation
function initCounters() {
    const counters = document.querySelectorAll('.stat-number[data-count]');
    
    const animateCounter = (counter) => {
        const target = parseInt(counter.getAttribute('data-count'));
        const duration = 2000;
        const increment = target / (duration / 16);
        let current = 0;
        
        const timer = setInterval(() => {
            current += increment;
            if (current >= target) {
                current = target;
                clearInterval(timer);
            }
            counter.textContent = Math.floor(current).toLocaleString();
        }, 16);
    };
    
    // Intersection Observer for counters
    const counterObserver = new IntersectionObserver((entries) => {
        entries.forEach(entry => {
            if (entry.isIntersecting) {
                animateCounter(entry.target);
                counterObserver.unobserve(entry.target);
            }
        });
    }, { threshold: 0.5 });
    
    counters.forEach(counter => {
        counterObserver.observe(counter);
    });
}

// Scroll Effects
function initScrollEffects() {
    // Parallax effect for hero shapes
    window.addEventListener('scroll', function() {
        const scrolled = window.pageYOffset;
        const shapes = document.querySelectorAll('.shape');
        
        shapes.forEach((shape, index) => {
            const speed = 0.5 + (index * 0.1);
            shape.style.transform = `translateY(${scrolled * speed}px) rotate(${scrolled * 0.1}deg)`;
        });
    });
    
    // Reveal animations on scroll
    const revealElements = document.querySelectorAll('.feature-card, .step-card');
    const revealObserver = new IntersectionObserver((entries) => {
        entries.forEach(entry => {
            if (entry.isIntersecting) {
                entry.target.style.opacity = '1';
                entry.target.style.transform = 'translateY(0)';
            }
        });
    }, { threshold: 0.1 });
    
    revealElements.forEach(element => {
        element.style.opacity = '0';
        element.style.transform = 'translateY(30px)';
        element.style.transition = 'opacity 0.6s ease, transform 0.6s ease';
        revealObserver.observe(element);
    });
}

// Interactive Elements
function initInteractiveElements() {
    // Feature card hover effects
    const featureCards = document.querySelectorAll('.feature-card');
    featureCards.forEach(card => {
        card.addEventListener('mouseenter', function() {
            this.style.transform = 'translateY(-10px) scale(1.02)';
        });
        
        card.addEventListener('mouseleave', function() {
            this.style.transform = 'translateY(0) scale(1)';
        });
    });
    
    // Step card hover effects
    const stepCards = document.querySelectorAll('.step-card');
    stepCards.forEach(card => {
        card.addEventListener('mouseenter', function() {
            this.style.transform = 'translateY(-5px) scale(1.02)';
        });
        
        card.addEventListener('mouseleave', function() {
            this.style.transform = 'translateY(0) scale(1)';
        });
    });
    
    // Button click effects
    const buttons = document.querySelectorAll('.btn');
    buttons.forEach(button => {
        button.addEventListener('click', function(e) {
            // Create ripple effect
            const ripple = document.createElement('span');
            const rect = this.getBoundingClientRect();
            const size = Math.max(rect.width, rect.height);
            const x = e.clientX - rect.left - size / 2;
            const y = e.clientY - rect.top - size / 2;
            
            ripple.style.width = ripple.style.height = size + 'px';
            ripple.style.left = x + 'px';
            ripple.style.top = y + 'px';
            ripple.classList.add('ripple');
            
            this.appendChild(ripple);
            
            setTimeout(() => {
                ripple.remove();
            }, 600);
        });
    });
    
    // Phone mockup interaction
    const phoneMockup = document.querySelector('.phone-mockup');
    if (phoneMockup) {
        phoneMockup.addEventListener('mouseenter', function() {
            this.style.transform = 'perspective(1000px) rotateY(-10deg) rotateX(2deg) scale(1.05)';
        });
        
        phoneMockup.addEventListener('mouseleave', function() {
            this.style.transform = 'perspective(1000px) rotateY(-15deg) rotateX(5deg) scale(1)';
        });
    }
}

// Utility Functions
const LandingUtils = {
    // Debounce function
    debounce: function(func, wait) {
        let timeout;
        return function executedFunction(...args) {
            const later = () => {
                clearTimeout(timeout);
                func(...args);
            };
            clearTimeout(timeout);
            timeout = setTimeout(later, wait);
        };
    },
    
    // Throttle function
    throttle: function(func, limit) {
        let inThrottle;
        return function() {
            const args = arguments;
            const context = this;
            if (!inThrottle) {
                func.apply(context, args);
                inThrottle = true;
                setTimeout(() => inThrottle = false, limit);
            }
        };
    },
    
    // Check if element is in viewport
    isInViewport: function(element) {
        const rect = element.getBoundingClientRect();
        return (
            rect.top >= 0 &&
            rect.left >= 0 &&
            rect.bottom <= (window.innerHeight || document.documentElement.clientHeight) &&
            rect.right <= (window.innerWidth || document.documentElement.clientWidth)
        );
    },
    
    // Smooth scroll to element
    scrollToElement: function(element, offset = 0) {
        const elementPosition = element.getBoundingClientRect().top;
        const offsetPosition = elementPosition + window.pageYOffset - offset;
        
        window.scrollTo({
            top: offsetPosition,
            behavior: 'smooth'
        });
    }
};

// Add CSS for ripple effect
const style = document.createElement('style');
style.textContent = `
    .btn {
        position: relative;
        overflow: hidden;
    }
    
    .ripple {
        position: absolute;
        border-radius: 50%;
        background: rgba(255, 255, 255, 0.3);
        transform: scale(0);
        animation: ripple-animation 0.6s linear;
        pointer-events: none;
    }
    
    @keyframes ripple-animation {
        to {
            transform: scale(4);
            opacity: 0;
        }
    }
    
    .feature-card:hover .pulse-ring {
        animation-play-state: running;
    }
    
    .pulse-ring {
        animation-play-state: paused;
    }
`;
document.head.appendChild(style);

// Export for global access
window.LandingUtils = LandingUtils;


(function() {
    const toastContainerId = 'momentumToastContainer';
    const defaultDuration = 3500;
    const variantClassMap = {
        primary: 'bg-primary text-white',
        secondary: 'bg-secondary text-white',
        success: 'bg-success text-white',
        danger: 'bg-danger text-white',
        warning: 'bg-warning text-dark',
        info: 'bg-info text-dark',
        light: 'bg-light text-dark',
        dark: 'bg-dark text-white'

    };

    function ensureToastContainer() {
        let container = document.getElementById(toastContainerId);
        if (!container) {
            container = document.createElement('div');
            container.id = toastContainerId;
            container.className = 'toast-container position-fixed top-0 end-0 p-3 pe-3';
            container.style.zIndex = '2000';
            container.style.maxWidth = '360px';
            document.body.appendChild(container);
        }
        return container;
    }

    function normalizeVariant(variant) {
        const key = (typeof variant === 'string' ? variant : 'info').trim().toLowerCase();
        if (key === 'error') {
            return 'danger';
        }
        return variantClassMap[key] ? key : 'info';
    }

    function getCloseButtonClass(variantKey) {
        const whiteCloseVariants = new Set(['primary', 'secondary', 'success', 'danger', 'dark']);
        return whiteCloseVariants.has(variantKey) ? 'btn-close btn-close-white ms-2' : 'btn-close ms-2';
    }

    function showToast(message, variant = 'info', options = {}) {
        return null;
    }

    window.Momentum = window.Momentum || {};
    window.Momentum.showToast = showToast;
})();






