/**
 * Authentication Utilities
 */
const Auth = {
    getToken: () => localStorage.getItem('fixflow_token'),
    
    getUser: () => {
        const userStr = localStorage.getItem('fixflow_user');
        return userStr ? JSON.parse(userStr) : null;
    },
    
    setAuth: (token, user) => {
        localStorage.setItem('fixflow_token', token);
        localStorage.setItem('fixflow_user', JSON.stringify(user));
    },
    
    logout: () => {
        localStorage.removeItem('fixflow_token');
        localStorage.removeItem('fixflow_user');
        window.location.href = '/FixFlow/login.html';
    },
    
    isAuthenticated: () => !!localStorage.getItem('fixflow_token'),
    
    hasRole: (role) => {
        const user = Auth.getUser();
        return user && user.role === role;
    },

    // Used in dashboard scripts to redirect if unauthorized
    requireAuth: (requiredRole = null) => {
        if (!Auth.isAuthenticated()) {
            window.location.href = '/FixFlow/login.html';
            return false;
        }
        
        if (requiredRole && !Auth.hasRole(requiredRole)) {
            // Redirect to appropriate dashboard based on actual role
            const user = Auth.getUser();
            if (user.role === 'ADMIN') window.location.href = '/FixFlow/admin/dashboard.html';
            else if (user.role === 'TECHNICIAN') window.location.href = '/FixFlow/technician/dashboard.html';
            else window.location.href = '/FixFlow/user/dashboard.html';
            return false;
        }
        
        return true;
    }
};

// Common header UI initialization
document.addEventListener('DOMContentLoaded', () => {
    const userDisplay = document.getElementById('current-user-name');
    const roleDisplay = document.getElementById('current-user-role');
    const logoutBtn = document.getElementById('logout-btn');
    
    if (userDisplay && Auth.getUser()) {
        userDisplay.textContent = Auth.getUser().name;
    }
    
    if (roleDisplay && Auth.getUser()) {
        roleDisplay.textContent = Auth.getUser().role;
    }

    if (logoutBtn) {
        logoutBtn.addEventListener('click', (e) => {
            e.preventDefault();
            Auth.logout();
        });
    }
});
