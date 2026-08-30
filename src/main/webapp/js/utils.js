/**
 * Global UI Utilities
 */

// Theme Manager
const ThemeManager = {
    init: () => {
        const savedTheme = localStorage.getItem('fixflow-theme') || 'light';
        document.documentElement.setAttribute('data-theme', savedTheme);
        ThemeManager.updateToggleIcons(savedTheme);
        
        // Setup toggle buttons
        document.querySelectorAll('.theme-toggle-btn').forEach(btn => {
            btn.addEventListener('click', () => {
                const currentTheme = document.documentElement.getAttribute('data-theme');
                const newTheme = currentTheme === 'dark' ? 'light' : 'dark';
                
                document.documentElement.setAttribute('data-theme', newTheme);
                localStorage.setItem('fixflow-theme', newTheme);
                ThemeManager.updateToggleIcons(newTheme);
            });
        });
    },
    
    updateToggleIcons: (theme) => {
        document.querySelectorAll('.theme-toggle-btn').forEach(btn => {
            if (theme === 'dark') {
                btn.innerHTML = '<i class="fas fa-sun"></i>';
                btn.title = "Switch to Light Mode";
            } else {
                btn.innerHTML = '<i class="fas fa-moon"></i>';
                btn.title = "Switch to Dark Mode";
            }
        });
    }
};

// Toast Notifications
const Toast = {
    container: null,
    
    init: () => {
        if (!document.getElementById('toast-container')) {
            const container = document.createElement('div');
            container.id = 'toast-container';
            document.body.appendChild(container);
            Toast.container = container;
        } else {
            Toast.container = document.getElementById('toast-container');
        }
    },
    
    show: (message, type = 'info', title = null) => {
        if (!Toast.container) Toast.init();
        
        const toast = document.createElement('div');
        toast.className = `toast toast-${type}`;
        
        let icon = 'info-circle';
        if (!title) {
            title = 'Notification';
            if (type === 'success') { icon = 'check-circle'; title = 'Success'; }
            if (type === 'error') { icon = 'exclamation-circle'; title = 'Error'; }
            if (type === 'warning') { icon = 'exclamation-triangle'; title = 'Warning'; }
        }
        
        toast.innerHTML = `
            <div class="toast-icon">
                <i class="fas fa-${icon}"></i>
            </div>
            <div class="toast-content">
                <h4 class="toast-title">${title}</h4>
                <p class="toast-message">${message}</p>
            </div>
            <div class="toast-progress"></div>
        `;
        
        Toast.container.appendChild(toast);
        
        // Remove after animation
        setTimeout(() => {
            toast.style.animation = 'slideOut 0.3s forwards';
            setTimeout(() => toast.remove(), 300);
        }, 4000);
    }
};

// Modal Handler
const Modal = {
    open: (modalId) => {
        const backdrop = document.getElementById(`${modalId}-backdrop`);
        if (backdrop) backdrop.classList.add('active');
    },
    
    close: (modalId) => {
        const backdrop = document.getElementById(`${modalId}-backdrop`);
        if (backdrop) backdrop.classList.remove('active');
    },
    
    setup: () => {
        document.querySelectorAll('.modal-close').forEach(btn => {
            btn.addEventListener('click', (e) => {
                const backdrop = e.target.closest('.modal-backdrop');
                if (backdrop) backdrop.classList.remove('active');
            });
        });
    }
};

// Formatters
const Format = {
    date: (dateStr) => {
        if (!dateStr) return '-';
        return new Date(dateStr).toLocaleDateString(undefined, { 
            year: 'numeric', month: 'short', day: 'numeric',
            hour: '2-digit', minute: '2-digit'
        });
    },
    
    badge: (status) => {
        const lower = status ? status.toLowerCase() : 'neutral';
        return `<span class="badge badge-${lower}">${status}</span>`;
    }
};

// Setup user profile dropdowns
const setupProfileDropdowns = () => {
    document.querySelectorAll('.user-profile').forEach(profile => {
        profile.addEventListener('click', (e) => {
            e.stopPropagation();
            profile.classList.toggle('active');
        });
    });
    
    document.addEventListener('click', () => {
        document.querySelectorAll('.user-profile').forEach(profile => {
            profile.classList.remove('active');
        });
    });
};

// View Switcher (for SPA-like feel in dashboards)
const setupViewSwitching = () => {
    const navLinks = document.querySelectorAll('.nav-link[data-target]');
    navLinks.forEach(link => {
        link.addEventListener('click', (e) => {
            e.preventDefault();
            const targetId = link.getAttribute('data-target');
            
            // Update active link
            document.querySelectorAll('.nav-link').forEach(n => n.classList.remove('active'));
            link.classList.add('active');
            
            // Switch views
            document.querySelectorAll('.view-section').forEach(v => v.classList.remove('active'));
            document.getElementById(targetId).classList.add('active');
            
            // Trigger custom event for the module to handle (e.g. refresh data)
            document.dispatchEvent(new CustomEvent('viewChanged', { detail: targetId }));
        });
    });
};

// Immediately apply theme before DOM is fully loaded to prevent flash
(function() {
    const savedTheme = localStorage.getItem('fixflow-theme') || 'light';
    document.documentElement.setAttribute('data-theme', savedTheme);
})();

document.addEventListener('DOMContentLoaded', () => {
    ThemeManager.init();
    Toast.init();
    Modal.setup();
    setupProfileDropdowns();
    setupViewSwitching();
});
