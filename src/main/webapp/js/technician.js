// Technician Dashboard Logic
let categoriesCacheTech = [];

document.addEventListener('DOMContentLoaded', () => {
    Auth.requireAuth('TECHNICIAN');

    const user = Auth.getUser();
    if (user && user.name) {
        const avatarEl = document.getElementById('avatar-initial');
        if (avatarEl) avatarEl.textContent = user.name.charAt(0).toUpperCase();
        const nameEl = document.getElementById('current-user-name');
        if (nameEl) nameEl.textContent = user.name;
    }

    document.addEventListener('viewChanged', (e) => {
        if (e.detail === 'view-assigned')    loadAssignedRequests();
        if (e.detail === 'view-in-progress') loadInProgressRequests();
    });

    loadCategories().then(() => loadAssignedRequests());
});

async function loadCategories() {
    if (categoriesCacheTech.length > 0) return;
    try {
        const res = await apiGet('/categories');
        categoriesCacheTech = res.data;
    } catch (err) {
        console.error('Could not load categories', err);
    }
}

function getCatForId(catId) {
    return categoriesCacheTech.find(c => c.id === catId) || null;
}

function buildCatCell(catId) {
    const cat = getCatForId(catId);
    if (!cat) return '<span style="color:var(--text-secondary);font-size:0.8rem;">—</span>';
    const meta = CATEGORY_META[cat.name] || { icon: 'fa-screwdriver-wrench', cls: 'general' };
    return `<div style="display:flex;align-items:center;gap:0.4rem;">
        <div class="catalog-icon ${meta.cls}" style="width:26px;height:26px;border-radius:7px;font-size:0.7rem;flex-shrink:0;">
            <i class="fas ${meta.icon}"></i>
        </div>
        <span style="font-size:0.8rem;color:var(--text-secondary);line-height:1.2;">${cat.name}</span>
    </div>`;
}

async function loadAssignedRequests() {
    try {
        const tbody = document.querySelector('#tech-requests-table tbody');
        tbody.innerHTML = '<tr><td colspan="8" class="empty-state"><div class="spinner"></div> Loading...</td></tr>';

        const res  = await apiGet('/requests?limit=50&sortBy=createdAt&sortOrder=desc');
        const data = res.data;
        const active = data.filter(r => r.status === 'ASSIGNED' || r.status === 'IN_PROGRESS');

        // Stats
        document.getElementById('stat-assigned').textContent = active.filter(r => r.status === 'ASSIGNED').length;
        document.getElementById('stat-wip').textContent      = active.filter(r => r.status === 'IN_PROGRESS').length;
        document.getElementById('stat-done').textContent     = data.filter(r => r.status === 'RESOLVED').length;

        if (active.length === 0) {
            tbody.innerHTML = `<tr><td colspan="8" class="empty-state"><i class="fas fa-check-circle" style="font-size:2rem;color:var(--success-color);margin-bottom:0.75rem;"></i><br>No active assignments. You're all caught up!</td></tr>`;
            return;
        }

        tbody.innerHTML = active.map(req => `
            <tr>
                <td>#${req.id}</td>
                <td>${buildCatCell(req.categoryId)}</td>
                <td><strong>${req.title}</strong></td>
                <td style="max-width:140px;overflow:hidden;text-overflow:ellipsis;white-space:nowrap;" title="${req.location}">${req.location}</td>
                <td>${Format.badge(req.priority)}</td>
                <td>${Format.badge(req.status)}</td>
                <td style="font-size:0.78rem;color:var(--text-secondary);">${Format.date(req.createdAt)}</td>
                <td>
                    <button class="btn btn-primary" onclick="viewTechRequestDetails(${req.id})">
                        <i class="fas fa-wrench"></i> Manage
                    </button>
                </td>
            </tr>
        `).join('');

        // Also refresh WIP table if it's visible
        loadInProgressTable(data.filter(r => r.status === 'IN_PROGRESS'));

    } catch (err) {
        Toast.show('Failed to load assigned requests', 'error');
    }
}

async function loadInProgressRequests() {
    try {
        const res  = await apiGet('/requests?status=IN_PROGRESS&limit=50');
        loadInProgressTable(res.data);
    } catch (err) {
        Toast.show('Failed to load in-progress requests', 'error');
    }
}

function loadInProgressTable(requests) {
    const tbody = document.querySelector('#tech-wip-table tbody');
    if (!tbody) return;
    if (requests.length === 0) {
        tbody.innerHTML = `<tr><td colspan="6" class="empty-state">No requests currently in progress.</td></tr>`;
        return;
    }
    tbody.innerHTML = requests.map(req => `
        <tr>
            <td>#${req.id}</td>
            <td>${buildCatCell(req.categoryId)}</td>
            <td><strong>${req.title}</strong></td>
            <td>${req.location}</td>
            <td>${Format.badge(req.priority)}</td>
            <td>
                <button class="btn btn-primary" onclick="viewTechRequestDetails(${req.id})">
                    <i class="fas fa-check"></i> Resolve
                </button>
            </td>
        </tr>
    `).join('');
}

window.viewTechRequestDetails = async (id) => {
    Modal.open('request-details');
    const content = document.getElementById('request-details-content');
    const footer  = document.getElementById('request-details-actions');

    content.innerHTML = '<div class="empty-state"><div class="spinner"></div></div>';
    footer.innerHTML  = '<button class="btn btn-outline modal-close">Close</button>';

    try {
        const res = await apiGet(`/requests/${id}`);
        const req = res.data;
        const cat = getCatForId(req.categoryId);
        const catIconHtml = cat ? getCategoryIconHtml(cat.name) : '';

        content.innerHTML = `
            <div style="display:flex;align-items:center;gap:0.75rem;margin-bottom:1rem;padding-bottom:1rem;border-bottom:1px solid var(--border-color);">
                ${catIconHtml}
                <div>
                    <p style="margin:0;font-size:0.75rem;color:var(--text-secondary);font-weight:600;text-transform:uppercase;letter-spacing:0.05em;">${cat ? cat.name : 'Service Request'}</p>
                    <h4 style="margin:0.2rem 0;font-size:1rem;">${req.title}</h4>
                    <div style="display:flex;gap:0.4rem;">${Format.badge(req.status)} ${Format.badge(req.priority)}</div>
                </div>
            </div>
            <div style="display:grid;grid-template-columns:1fr 1fr;gap:0.75rem;margin-bottom:1rem;">
                <div style="background:var(--bg-main);padding:0.75rem;border-radius:var(--radius-md);border:1px solid var(--border-color);">
                    <p style="margin:0;font-size:0.75rem;color:var(--text-secondary);font-weight:600;">LOCATION</p>
                    <p style="margin:0.2rem 0 0;font-size:0.88rem;">${req.location}</p>
                </div>
                <div style="background:var(--bg-main);padding:0.75rem;border-radius:var(--radius-md);border:1px solid var(--border-color);">
                    <p style="margin:0;font-size:0.75rem;color:var(--text-secondary);font-weight:600;">CREATED</p>
                    <p style="margin:0.2rem 0 0;font-size:0.88rem;">${Format.date(req.createdAt)}</p>
                </div>
            </div>
            <h5 style="margin-bottom:0.5rem;color:var(--text-secondary);font-size:0.8rem;text-transform:uppercase;letter-spacing:0.05em;">Description</h5>
            <div style="white-space:pre-wrap;color:var(--text-primary);font-size:0.875rem;background:var(--bg-surface);padding:1rem;border:1px solid var(--border-color);border-radius:var(--radius-md);">${req.description}</div>
        `;

        // Action buttons based on state
        if (req.status === 'ASSIGNED') {
            footer.innerHTML = `
                <button class="btn btn-outline modal-close">Cancel</button>
                <button class="btn btn-primary" onclick="updateRequestStatus(${req.id}, 'IN_PROGRESS')">
                    <i class="fas fa-play"></i> Start Work
                </button>`;
        } else if (req.status === 'IN_PROGRESS') {
            footer.innerHTML = `
                <button class="btn btn-outline modal-close">Cancel</button>
                <button class="btn btn-primary" onclick="updateRequestStatus(${req.id}, 'RESOLVED')" style="background-color: var(--status-resolved, #22c55e);">
                    <i class="fas fa-check"></i> Mark as Resolved
                </button>`;
        } else {
            footer.innerHTML = '<button class="btn btn-outline modal-close">Close</button>';
        }

    } catch (err) {
        content.innerHTML = `<div class="empty-state"><i class="fas fa-exclamation-circle" style="font-size:2rem;color:var(--danger-color);margin-bottom:1rem;"></i><br>Failed to load details</div>`;
    }
};

window.updateRequestStatus = async (id, status) => {
    try {
        await apiPatch(`/requests/${id}/status`, { status });
        Toast.show('Status updated successfully', 'success');
        Modal.close('request-details');
        loadAssignedRequests();
    } catch (err) {
        Toast.show(err.message, 'error');
    }
};
