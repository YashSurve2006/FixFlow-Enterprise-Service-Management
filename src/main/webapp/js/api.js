// FixFlow API Base URL
// Assuming Tomcat deploys this war to /FixFlow context path. If deployed to root, change this to '/api'
const API_BASE = '/FixFlow/api';

/**
 * Core Fetch Wrapper
 */
async function apiRequest(endpoint, options = {}) {
    const url = `${API_BASE}${endpoint}`;
    
    // Setup Headers
    const headers = {
        'Accept': 'application/json',
        ...options.headers
    };
    
    // Add JWT Token if available
    const token = localStorage.getItem('fixflow_token');
    if (token) {
        headers['Authorization'] = `Bearer ${token}`;
    }

    // Auto set content-type for body if not FormData
    if (options.body && !(options.body instanceof FormData)) {
        headers['Content-Type'] = 'application/json';
        if (typeof options.body === 'object') {
            options.body = JSON.stringify(options.body);
        }
    }

    const config = {
        ...options,
        headers
    };

    try {
        const response = await fetch(url, config);
        
        // Handle 401 Unauthorized globally
        if (response.status === 401) {
            localStorage.removeItem('fixflow_token');
            localStorage.removeItem('fixflow_user');
            window.location.href = '/FixFlow/login.html'; // Adjust based on deployment path
            throw new Error('Unauthorized');
        }

        // 204 No Content has no body
        if (response.status === 204) {
            return null;
        }

        const data = await response.json();
        
        if (!response.ok) {
            throw new Error(data.message || data.error || 'API Request Failed');
        }
        
        return data;
    } catch (error) {
        console.error(`API Error [${options.method || 'GET'} ${endpoint}]:`, error);
        throw error;
    }
}

// Helpers
const apiGet = (endpoint) => apiRequest(endpoint, { method: 'GET' });
const apiPost = (endpoint, body) => apiRequest(endpoint, { method: 'POST', body });
const apiPut = (endpoint, body) => apiRequest(endpoint, { method: 'PUT', body });
const apiPatch = (endpoint, body) => apiRequest(endpoint, { method: 'PATCH', body });
const apiDelete = (endpoint) => apiRequest(endpoint, { method: 'DELETE' });
