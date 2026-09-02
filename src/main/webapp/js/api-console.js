// Admin API Console Logic

document.addEventListener('DOMContentLoaded', () => {
    Auth.requireAuth('ADMIN');

    // Populate user info
    const user = Auth.getUser();
    if (user && user.name) {
        const avatarEl = document.getElementById('avatar-initial');
        if (avatarEl) avatarEl.textContent = user.name.charAt(0).toUpperCase();
        const nameEl = document.getElementById('current-user-name');
        if (nameEl) nameEl.textContent = user.name;
    }

    // Setup Event Listeners
    document.getElementById('api-method').addEventListener('change', handleMethodChange);
    document.getElementById('btn-send-request').addEventListener('click', sendApiRequest);
    document.getElementById('btn-copy-res').addEventListener('click', copyResponse);
    document.getElementById('btn-clear-res').addEventListener('click', clearResponse);
    document.getElementById('logout-btn').addEventListener('click', () => { Auth.logout(); });

    // Initial check for health
    checkHealth();
    
    // Load history
    renderHistory();
});

function handleMethodChange(e) {
    const method = e.target.value;
    const methodBadge = document.querySelector('.method-badge'); // not the one in the examples, wait, we need to update the color of something?
    // Actually, in the UI we just have the select dropdown. We can change the border color or something, but let's just toggle body visibility.
    
    const bodySection = document.getElementById('body-section');
    if (method === 'POST' || method === 'PUT' || method === 'PATCH') {
        bodySection.style.display = 'block';
    } else {
        bodySection.style.display = 'none';
    }
}

async function checkHealth() {
    const statusText = document.getElementById('api-status-text');
    const statusBtn = document.getElementById('api-status-btn');
    try {
        // Quick fetch to /api/health to see if online
        const url = `${API_BASE}/health`;
        const res = await fetch(url);
        if (res.ok) {
            statusText.textContent = 'API ONLINE';
        } else {
            throw new Error('Not OK');
        }
    } catch (err) {
        statusText.textContent = 'API OFFLINE';
        statusBtn.style.color = '#ef4444';
        statusBtn.style.background = 'color-mix(in srgb, #ef4444 15%, transparent)';
        statusBtn.style.borderColor = 'color-mix(in srgb, #ef4444 30%, transparent)';
        statusBtn.querySelector('.status-dot').style.background = '#ef4444';
        statusBtn.querySelector('.status-dot').style.boxShadow = '0 0 8px #ef4444';
    }
}

// Attach to window so onclick in HTML can use it
window.loadExample = function(method, endpoint, body = '') {
    const methodSelect = document.getElementById('api-method');
    methodSelect.value = method;
    // Dispatch change event to toggle body visibility
    methodSelect.dispatchEvent(new Event('change'));
    
    document.getElementById('api-endpoint').value = endpoint;
    document.getElementById('api-body').value = body;
    
    // Scroll to top
    window.scrollTo({ top: 0, behavior: 'smooth' });
};

async function sendApiRequest() {
    const method = document.getElementById('api-method').value;
    let endpoint = document.getElementById('api-endpoint').value.trim();
    const bodyText = document.getElementById('api-body').value.trim();
    
    if (!endpoint) {
        Toast.show('Endpoint is required', 'warning');
        return;
    }
    
    if (!endpoint.startsWith('/')) {
        endpoint = '/' + endpoint;
    }
    
    // Validate JSON if POST/PUT/PATCH
    let bodyData = null;
    if ((method === 'POST' || method === 'PUT' || method === 'PATCH') && bodyText) {
        try {
            bodyData = JSON.parse(bodyText);
        } catch (e) {
            Toast.show('Invalid JSON body', 'warning');
            return;
        }
    }

    const btn = document.getElementById('btn-send-request');
    btn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Sending...';
    btn.disabled = true;
    
    document.getElementById('api-response').textContent = 'Loading...';
    document.getElementById('api-response').style.color = '#d4d4d4';
    document.getElementById('res-meta-panel').style.opacity = '1';

    const url = endpoint.startsWith('/api') ? endpoint.replace('/api', API_BASE) : `${API_BASE}${endpoint}`;
    
    const headers = {
        'Accept': 'application/json'
    };
    
    const token = localStorage.getItem('fixflow_token');
    if (token) {
        headers['Authorization'] = `Bearer ${token}`;
    }
    
    const config = {
        method,
        headers
    };
    
    if (bodyData) {
        headers['Content-Type'] = 'application/json';
        config.body = JSON.stringify(bodyData);
    }
    
    const startTime = performance.now();
    let status = 0;
    let statusText = '';
    let responseData = null;
    let isError = false;

    try {
        const response = await fetch(url, config);
        const endTime = performance.now();
        const duration = Math.round(endTime - startTime);
        
        status = response.status;
        statusText = response.statusText;
        
        // Render Meta
        document.getElementById('res-time').textContent = `${duration} ms`;
        
        const statusEl = document.getElementById('res-status');
        statusEl.textContent = `${status} ${statusText}`;
        if (status >= 200 && status < 300) {
            statusEl.style.color = '#10b981'; // Green
        } else if (status >= 400 && status < 500) {
            statusEl.style.color = '#f59e0b'; // Warning
            isError = true;
        } else {
            statusEl.style.color = '#ef4444'; // Error
            isError = true;
        }

        // Add to history
        addHistory(method, endpoint, status, duration);

        if (status === 204) {
            responseData = 'No Content (204)';
            document.getElementById('api-response').textContent = responseData;
        } else {
            const text = await response.text();
            try {
                // Try format JSON
                const json = JSON.parse(text);
                document.getElementById('api-response').textContent = JSON.stringify(json, null, 2);
            } catch (e) {
                // Fallback to text
                document.getElementById('api-response').textContent = text;
            }
        }
        
        if (isError) {
            document.getElementById('api-response').style.color = '#ef4444';
        }

        Toast.show(`Request completed (${status})`, isError ? 'error' : 'success');

    } catch (err) {
        console.error('Fetch Error:', err);
        document.getElementById('res-status').textContent = 'ERROR';
        document.getElementById('res-status').style.color = '#ef4444';
        document.getElementById('res-time').textContent = '-';
        document.getElementById('api-response').textContent = `Connection Error: ${err.message}`;
        document.getElementById('api-response').style.color = '#ef4444';
        Toast.show('Request failed', 'error');
        addHistory(method, endpoint, 'ERR', '-');
    } finally {
        btn.innerHTML = '<i class="fas fa-paper-plane"></i> Send Request';
        btn.disabled = false;
    }
}

function copyResponse() {
    const text = document.getElementById('api-response').textContent;
    if (text && text !== 'Awaiting request...' && text !== 'Loading...') {
        navigator.clipboard.writeText(text).then(() => {
            Toast.show('Response copied to clipboard', 'success');
        });
    }
}

function clearResponse() {
    document.getElementById('api-response').textContent = 'Awaiting request...';
    document.getElementById('api-response').style.color = '#d4d4d4';
    document.getElementById('res-status').textContent = '-';
    document.getElementById('res-status').style.color = 'inherit';
    document.getElementById('res-time').textContent = '-';
    document.getElementById('res-meta-panel').style.opacity = '0.5';
}

function addHistory(method, endpoint, status, time) {
    let history = [];
    try {
        history = JSON.parse(sessionStorage.getItem('api_history') || '[]');
    } catch (e) {}
    
    history.unshift({ method, endpoint, status, time, timestamp: new Date().toLocaleTimeString() });
    if (history.length > 10) history = history.slice(0, 10);
    
    sessionStorage.setItem('api_history', JSON.stringify(history));
    renderHistory();
}

function renderHistory() {
    let history = [];
    try {
        history = JSON.parse(sessionStorage.getItem('api_history') || '[]');
    } catch (e) {}
    
    const list = document.getElementById('api-history-list');
    if (history.length === 0) {
        list.innerHTML = '<li class="history-item" style="color: var(--text-muted); justify-content: center;">No recent requests</li>';
        return;
    }
    
    list.innerHTML = history.map(h => {
        let statusColor = '#d4d4d4';
        if (h.status >= 200 && h.status < 300) statusColor = '#10b981';
        else if (h.status >= 400 && h.status < 500) statusColor = '#f59e0b';
        else if (h.status >= 500) statusColor = '#ef4444';
        
        let methodClass = `method-${h.method}`;
        return `
            <li class="history-item" style="display:flex; justify-content: space-between; align-items:center;">
                <div style="display:flex; gap: 1rem; align-items: center; flex:1;">
                    <span class="method-badge ${methodClass}" style="min-width: 50px; font-size: 0.7rem; padding: 0.2rem 0.5rem;">${h.method}</span>
                    <span style="font-weight: 600; color: var(--text-primary); text-overflow: ellipsis; overflow: hidden; white-space: nowrap; max-width: 250px;" title="${h.endpoint}">${h.endpoint}</span>
                </div>
                <div style="display:flex; gap: 1rem; align-items: center; text-align: right;">
                    <span style="color: ${statusColor}; font-weight:bold;">${h.status}</span>
                    <span style="color: var(--text-secondary); min-width: 60px;">${h.time}ms</span>
                </div>
            </li>
        `;
    }).join('');
}

window.clearHistory = function() {
    sessionStorage.removeItem('api_history');
    renderHistory();
    Toast.show('History cleared', 'success');
};
