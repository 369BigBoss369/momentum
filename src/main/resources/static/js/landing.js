document.addEventListener('DOMContentLoaded', function() {
    const isCreateFoodPage = window.location.pathname.includes('/nutrition/create-food');
    if (!isCreateFoodPage && typeof AOS !== 'undefined') {
        AOS.init({
            duration: 1000,
            once: true,
            offset: 100
        });
    }

    initLoadingScreen();
    initGlobalNavigation();
    initNavigation();
    initCounters();
    initScrollEffects();
    initInteractiveElements();
});

function getGradientClass(path) {
    const lowerPath = path.toLowerCase();
    if (lowerPath.includes('nutrition')) {
        return 'nutrition-gradient';
    } else if (lowerPath.includes('fitness')) {
        return 'fitness-gradient';
    }
    return 'default-gradient';
}

function getGradientBackground(gradientClass) {
    const root = document.documentElement;
    let gradient;
    
    if (gradientClass === 'nutrition-gradient') {
        gradient = getComputedStyle(root).getPropertyValue('--gradient-success').trim();
        if (!gradient) {
            gradient = 'linear-gradient(135deg, #11998e 0%, #38ef7d 100%)';
        }
    } else if (gradientClass === 'fitness-gradient') {
        gradient = 'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)';
    } else {
        gradient = getComputedStyle(root).getPropertyValue('--gradient-primary').trim();
        if (!gradient) {
            gradient = 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)';
        }
    }
    
    return gradient;
}

function showLoadingScreen(targetPath) {
    const gradientClass = getGradientClass(targetPath);
    const gradientBackground = getGradientBackground(gradientClass);
    
    let loading = document.getElementById('loadingScreen');
    
    if (!loading) {
        loading = document.getElementById('pageLoadingScreen');
    }
    
    if (!loading) {
        loading = document.createElement('div');
        loading.className = 'loading-screen';
        loading.id = 'pageLoadingScreen';
        loading.innerHTML = '<div class="loading-spinner"></div>';
        document.body.appendChild(loading);
    }
    
    loading.classList.remove('nutrition-gradient', 'fitness-gradient', 'default-gradient');
    loading.classList.add(gradientClass);
    loading.style.background = gradientBackground;
    loading.classList.remove('hidden');
    loading.style.transition = 'none';
    loading.style.opacity = '1';
    loading.style.pointerEvents = 'all';
    loading.classList.add('active');
    
    setTimeout(() => {
        loading.style.transition = '';
    }, 50);
}

function hideLoadingScreen() {
    const loading = document.getElementById('loadingScreen') || document.getElementById('pageLoadingScreen');
    if (loading) {
        loading.style.opacity = '';
        loading.style.pointerEvents = '';
        loading.classList.remove('active');
        setTimeout(() => {
            loading.classList.add('hidden');
            loading.style.background = '';
        }, 450);
    }
}

function initLoadingScreen() {
    const loading = document.getElementById('loadingScreen') || document.getElementById('pageLoadingScreen');
    
    if (document.readyState === 'complete' && loading && !loading.classList.contains('active')) {
        return;
    }
    
    const currentPath = window.location.pathname;
    const gradientClass = getGradientClass(currentPath);
    
    let loadingEl = loading;
    
    if (!loadingEl) {
        loadingEl = document.createElement('div');
        loadingEl.className = 'loading-screen';
        loadingEl.id = 'pageLoadingScreen';
        loadingEl.innerHTML = '<div class="loading-spinner"></div>';
        document.body.appendChild(loadingEl);
    }
    
    loadingEl.classList.remove('nutrition-gradient', 'fitness-gradient', 'default-gradient');
    loadingEl.classList.add(gradientClass);

    loadingEl.style.background = getGradientBackground(gradientClass);

    if (document.readyState !== 'complete') {
        loadingEl.style.transition = 'none';
        loadingEl.style.opacity = '1';
        loadingEl.style.pointerEvents = 'all';
        loadingEl.classList.remove('hidden');
        loadingEl.classList.add('active');
        
        setTimeout(() => {
            loadingEl.style.transition = '';
        }, 50);
    }

    function hideWhenReady() {
        if (document.readyState === 'complete') {
            setTimeout(() => {
                hideLoadingScreen();
            }, 300);
        } else if (document.readyState === 'interactive') {
            setTimeout(() => {
                hideLoadingScreen();
            }, 300);
        } else {
            document.addEventListener('DOMContentLoaded', function() {
                setTimeout(() => {
                    hideLoadingScreen();
                }, 300);
            }, { once: true });

            let loadTimeout;
            const loadHandler = function() {
                clearTimeout(loadTimeout);
                setTimeout(() => {
                    hideLoadingScreen();
                }, 100);
            };
            
            window.addEventListener('load', loadHandler, { once: true });
            
            loadTimeout = setTimeout(function() {
                window.removeEventListener('load', loadHandler);
                hideLoadingScreen();
            }, 2000);
        }
    }
    
    hideWhenReady();
}

window.addEventListener('pageshow', function(event) {
    if (event.persisted) {
        const loading = document.getElementById('loadingScreen') || document.getElementById('pageLoadingScreen');
        if (loading) {
            loading.style.transition = 'none';
            loading.style.opacity = '0';
            loading.style.pointerEvents = 'none';
            loading.style.background = '';
            loading.classList.remove('active', 'nutrition-gradient', 'fitness-gradient', 'default-gradient');
            loading.classList.add('hidden');
            
            setTimeout(() => {
                loading.style.transition = '';
            }, 50);
        }
    } else {
        if (document.readyState === 'complete' || document.readyState === 'interactive') {
            setTimeout(() => {
                hideLoadingScreen();
            }, 100);
        }
    }
});

function initGlobalNavigation() {
    document.addEventListener('click', function(e) {
        const link = e.target.closest('a');
        
        if (!link || !link.href) return;
        
        try {
            const url = new URL(link.href, window.location.origin);
            
            if (url.origin !== window.location.origin) return;
            
            if (link.target === '_blank' || link.hasAttribute('download')) return;
            
            if (url.pathname === window.location.pathname && url.hash) {
                return;
            }
            
            if (link.getAttribute('href') === '#' || link.getAttribute('href') === '#!') {
                return;
            }
            
            if (link.protocol === 'javascript:' || link.protocol === 'mailto:') return;
            
            if (link.hasAttribute('data-bs-toggle') && link.getAttribute('data-bs-toggle') === 'collapse') {
                return;
            }
            
            if (link.hasAttribute('data-bs-toggle') && link.getAttribute('data-bs-toggle') === 'dropdown') {
                return;
            }
            
            if (url.pathname === window.location.pathname) {
                return;
            }
            
            e.preventDefault();
            
            showLoadingScreen(url.pathname);
            
            const loadingElement = document.getElementById('loadingScreen') || document.getElementById('pageLoadingScreen');
            if (loadingElement) {
                void loadingElement.offsetWidth;
            }
            
            window.location.href = link.href;
            
        } catch (err) {}
    });
    
    document.addEventListener('submit', function(e) {
        const form = e.target;
        const action = form.action || window.location.href;
        
        if (action) {
            showLoadingScreen(action);
        }
    });
}

function initNavigation() {
    const navbar = document.getElementById('mainNav');
    const navLinks = document.querySelectorAll('a.nav-link');
    
    window.addEventListener('scroll', function() {
        if (window.scrollY > 100) {
            navbar.classList.add('scrolled');
        } else {
            navbar.classList.remove('scrolled');
        }
    });
    
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

function initScrollEffects() {
    window.addEventListener('scroll', function() {
        const scrolled = window.pageYOffset;
        const shapes = document.querySelectorAll('.shape');
        
        shapes.forEach((shape, index) => {
            const speed = 0.5 + (index * 0.1);
            shape.style.transform = `translateY(${scrolled * speed}px) rotate(${scrolled * 0.1}deg)`;
        });
    });
    
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

function initInteractiveElements() {
    const featureCards = document.querySelectorAll('.feature-card');
    featureCards.forEach(card => {
        card.addEventListener('mouseenter', function() {
            this.style.transform = 'translateY(-10px) scale(1.02)';
        });
        
        card.addEventListener('mouseleave', function() {
            this.style.transform = 'translateY(0) scale(1)';
        });
    });
    
    const stepCards = document.querySelectorAll('.step-card');
    stepCards.forEach(card => {
        card.addEventListener('mouseenter', function() {
            this.style.transform = 'translateY(-5px) scale(1.02)';
        });
        
        card.addEventListener('mouseleave', function() {
            this.style.transform = 'translateY(0) scale(1)';
        });
    });
    
    const buttons = document.querySelectorAll('.btn');
    buttons.forEach(button => {
        button.addEventListener('click', function(e) {
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

window.LandingUtils = LandingUtils;