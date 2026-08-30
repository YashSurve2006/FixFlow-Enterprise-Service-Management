// Admin Dashboard Logic
let currentReqPage = 1;
let techniciansCache = [];
let categoriesCacheAdmin = [];
let currentManagingRequestId = null;

document.addEventListener('DOMContentLoaded', () => {
    Auth.requireAuth('ADMIN');

    const user = Auth.getUser();
    if (user && user.name) {
        const avatarEl = document.getElementById('avatar-initial');
        if (avatarEl) avatarEl.textContent = user.name.charAt(0).toUpperCase();
        const nameEl = document.getElementById('current-user-name');
        if (nameEl) nameEl.textContent = user.name;
    }

    document.addEventListener('viewChanged', (e) => {
        if (e.detail === 'view-stats')         { loadStatistics(); }
        else if (e.detail === 'view-all-requests') { loadRequests(1); }
        else if (e.detail === 'view-users')    { loadUsers(); }
        else if (e.detail === 'view-categories') { loadCategories(); }
    });

    // Initial
    loadCategories().then(() => {
        loadStatistics();
        loadRequests(1);
    });

    document.getElementById('btn-apply-filters').addEventListener('click', () => {
        currentReqPage = 1;
        loadRequests(1);
    });

    document.getElementById('form-add-category').addEventListener('submit', handleAddCategory);
    document.getElementById('form-edit-category').addEventListener('submit', handleEditCategory);
    document.getElementById('btn-assign-tech').addEventListener('click', handleAssignTechnician);
});

// ─── Categories ────────────────────────────────────────────────
async function loadCategories() {
    try {
        const res = await apiGet('/categories');
        categoriesCacheAdmin = res.data;

        // Populate request filter dropdown
        const sel = document.getElementById('filter-category-req');
        if (sel) {
            sel.innerHTML = '<option value="">All Services</option>' +
                categoriesCacheAdmin.map(c => `<option value="${c.id}">${c.name}</option>`).join('');
        }

        // Render the admin catalog cards
        renderAdminCatalog(categoriesCacheAdmin);
    } catch (err) {
        Toast.show('Failed to load categories', 'error');
    }
}

function renderAdminCatalog(cats) {
    const container = document.getElementById('admin-catalog-cards');
    if (!container) return;

    if (cats.length === 0) {
        container.innerHTML = `<p class="empty-state">No service categories found. Click "Add Service" to create one.</p>`;
        return;
    }

    container.innerHTML = cats.map(cat => {
        const meta = CATEGORY_META[cat.name] || { icon: 'fa-screwdriver-wrench', cls: 'general' };
        const createdStr = cat.createdAt ? new Date(cat.createdAt).toLocaleDateString() : '-';
        const updatedStr = cat.updatedAt ? new Date(cat.updatedAt).toLocaleDateString() : '-';
        return `
        <div class="admin-catalog-card" id="admin-cat-card-${cat.id}">
            <div class="catalog-icon ${meta.cls}">
                <i class="fas ${meta.icon}"></i>
            </div>
            <div class="catalog-info">
                <h4>${cat.name}</h4>
                <p title="${cat.description || ''}">${cat.description || '<em style="color:var(--text-muted)">No description</em>'}</p>
                <p style="margin-top:0.25rem;font-size:0.72rem;color:var(--text-muted);">
                    <i class="fas fa-calendar-plus"></i> ${createdStr}
                    &nbsp;&nbsp;
                    <i class="fas fa-calendar-check"></i> Updated ${updatedStr}
                </p>
            </div>
            <div class="catalog-actions">
                <button class="btn btn-outline" title="Edit" onclick="openEditModal(${cat.id}, '${escapeJs(cat.name)}', '${escapeJs(cat.description || '')}')">
                    <i class="fas fa-edit"></i>
                </button>
                <button class="btn btn-danger" title="Delete" onclick="deleteCategory(${cat.id}, '${escapeJs(cat.name)}')">
                    <i class="fas fa-trash"></i>
                </button>
            </div>
        </div>`;
    }).join('');
}

function escapeJs(str) {
    return String(str).replace(/\\/g, '\\\\').replace(/'/g, "\\'").replace(/\n/g, '\\n');
}

function getCatForIdAdmin(catId) {
    return categoriesCacheAdmin.find(c => c.id === catId) || null;
}

function buildAdminCatCell(catId) {
    const cat = getCatForIdAdmin(catId);
    if (!cat) return '<span style="color:var(--text-secondary);font-size:0.8rem;">—</span>';
    const meta = CATEGORY_META[cat.name] || { icon: 'fa-screwdriver-wrench', cls: 'general' };
    return `<div style="display:flex;align-items:center;gap:0.4rem;">
        <div class="catalog-icon ${meta.cls}" style="width:26px;height:26px;border-radius:7px;font-size:0.7rem;flex-shrink:0;">
            <i class="fas ${meta.icon}"></i>
        </div>
        <span style="font-size:0.8rem;color:var(--text-secondary);">${cat.name}</span>
    </div>`;
}

// ─── Add / Edit / Delete Category ─────────────────────────────
async function handleAddCategory(e) {
    e.preventDefault();
    const btn = e.target.querySelector('button[type="submit"]');
    btn.disabled = true;
    btn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Creating...';

    const name = document.getElementById('cat-name').value.trim();
    const description = document.getElementById('cat-desc').value.trim();
    try {
        await apiPost('/categories', { name, description });
        Toast.show(`Service "${name}" created successfully`, 'success');
        e.target.reset();
        Modal.close('add-category-modal');
        await loadCategories();
    } catch (err) {
        Toast.show(err.message, 'error');
    } finally {
        btn.disabled = false;
        btn.innerHTML = '<i class="fas fa-save"></i> Create Service';
    }
}

window.openEditModal = (id, name, description) => {
    document.getElementById('edit-cat-id').value   = id;
    document.getElementById('edit-cat-name').value = name;
    document.getElementById('edit-cat-desc').value = description;
    Modal.open('edit-category-modal');
};

async function handleEditCategory(e) {
    e.preventDefault();
    const btn = e.target.querySelector('button[type="submit"]');
    btn.disabled = true;
    btn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Saving...';

    const id   = document.getElementById('edit-cat-id').value;
    const name = document.getElementById('edit-cat-name').value.trim();
    const description = document.getElementById('edit-cat-desc').value.trim();

    try {
        await apiPut(`/categories/${id}`, { name, description });
        Toast.show(`Service "${name}" updated successfully`, 'success');
        Modal.close('edit-category-modal');
        await loadCategories();
    } catch (err) {
        Toast.show(err.message, 'error');
    } finally {
        btn.disabled = false;
        btn.innerHTML = '<i class="fas fa-save"></i> Save Changes';
    }
}

window.deleteCategory = async (id, name) => {
    if (!confirm(`Delete service "${name}"?\n\nNote: This will fail if there are service requests assigned to this category.`)) return;
    try {
        await apiDelete(`/categories/${id}`);
        Toast.show(`Service "${name}" deleted`, 'success');
        await loadCategories();
    } catch (err) {
        // User-friendly message for FK constraint
        const msg = err.message.toLowerCase().includes('cannot delete')
            ? `Cannot delete "${name}" — active service requests are using this category. Reassign or delete those requests first.`
            : err.message;
        Toast.show(msg, 'error');
    }
};

// ─── Statistics ────────────────────────────────────────────────
async function loadStatistics() {
    try {
        const res = await apiGet('/requests/statistics');
        const stats = res.data;

        document.getElementById('admin-stats-grid').innerHTML = `
            <div class="stat-card">
                <div class="stat-icon primary"><i class="fas fa-folder-open"></i></div>
                <div class="stat-details"><h3>${stats.totalRequests}</h3><p>Total Requests</p></div>
            </div>
            <div class="stat-card">
                <div class="stat-icon warning"><i class="fas fa-clock"></i></div>
                <div class="stat-details"><h3>${stats.pending}</h3><p>Pending</p></div>
            </div>
            <div class="stat-card">
                <div class="stat-icon primary"><i class="fas fa-user-tag"></i></div>
                <div class="stat-details"><h3>${stats.assigned}</h3><p>Assigned</p></div>
            </div>
            <div class="stat-card">
                <div class="stat-icon warning"><i class="fas fa-wrench"></i></div>
                <div class="stat-details"><h3>${stats.inProgress}</h3><p>In Progress</p></div>
            </div>
            <div class="stat-card">
                <div class="stat-icon success"><i class="fas fa-check-circle"></i></div>
                <div class="stat-details"><h3>${stats.resolved}</h3><p>Resolved</p></div>
            </div>
            <div class="stat-card">
                <div class="stat-icon success"><i class="fas fa-archive"></i></div>
                <div class="stat-details"><h3>${stats.closed}</h3><p>Closed</p></div>
            </div>
            <div class="stat-card">
                <div class="stat-icon danger"><i class="fas fa-exclamation-triangle"></i></div>
                <div class="stat-details"><h3>${stats.urgent}</h3><p>Urgent Priority</p></div>
            </div>
        `;

        // Category analytics bars
        renderCategoryAnalytics(stats.requestsByCategory);

    } catch (err) {
        Toast.show('Failed to load statistics', 'error');
    }
}

function renderCategoryAnalytics(byCategory) {
    const container = document.getElementById('category-analytics');
    if (!container || !byCategory || byCategory.length === 0) {
        if (container) container.innerHTML = '<p class="empty-state">No data available yet.</p>';
        return;
    }

    const max = Math.max(...byCategory.map(c => c.count), 1);
    container.innerHTML = byCategory.map(c => {
        const catMeta = CATEGORY_META[c.categoryName] || { icon: 'fa-screwdriver-wrench', cls: 'general' };
        const pct = Math.round((c.count / max) * 100);
        return `
        <div class="category-stat-bar">
            <div class="cat-label">
                <span><i class="fas ${catMeta.icon}" style="margin-right:0.3rem;"></i>${c.categoryName}</span>
                <span>${c.count}</span>
            </div>
            <div class="cat-bar-track">
                <div class="cat-bar-fill" style="width: ${pct}%;"></div>
            </div>
        </div>`;
    }).join('');
}

// ─── Requests ──────────────────────────────────────────────────
async function loadRequests(page) {
    try {
        const tbody = document.querySelector('#admin-requests-table tbody');
        if (page === 1) {
            tbody.innerHTML = '<tr><td colspan="7" class="empty-state"><div class="spinner"></div> Loading...</td></tr>';
        }

        let query = `?page=${page}&limit=10&sortBy=createdAt&sortOrder=desc`;
        const status   = document.getElementById('filter-status').value;
        const priority = document.getElementById('filter-priority').value;
        const search   = document.getElementById('filter-search').value;
        const catEl    = document.getElementById('filter-category-req');
        const catId    = catEl ? catEl.value : '';

        if (status)   query += `&status=${status}`;
        if (priority) query += `&priority=${priority}`;
        if (search)   query += `&search=${encodeURIComponent(search)}`;
        if (catId)    query += `&categoryId=${catId}`;

        const res  = await apiGet(`/requests${query}`);
        const data = res.data;

        if (data.length === 0) {
            tbody.innerHTML = `<tr><td colspan="7" class="empty-state">No requests found.</td></tr>`;
        } else {
            tbody.innerHTML = data.map(req => `
                <tr>
                    <td>#${req.id}</td>
                    <td>${buildAdminCatCell(req.categoryId)}</td>
                    <td><strong>${req.title}</strong></td>
                    <td>${Format.badge(req.priority)}</td>
                    <td>${Format.badge(req.status)}</td>
                    <td style="font-size:0.8rem;color:var(--text-secondary);">${Format.date(req.createdAt)}</td>
                    <td>
                        <button class="btn btn-primary" onclick="viewAdminRequestDetails(${req.id})">Manage</button>
                    </td>
                </tr>
            `).join('');
        }

        renderPagination(res.pagination);

    } catch (err) {
        Toast.show('Failed to load requests', 'error');
    }
}

function renderPagination(meta) {
    const container = document.getElementById('pagination-container');
    if (meta.totalPages <= 1) { container.innerHTML = ''; return; }
    let html = `<span>Page ${meta.page} of ${meta.totalPages}</span>`;
    if (meta.hasPrevious) html += `<button class="btn btn-outline" onclick="changePage(${meta.page - 1})">Previous</button>`;
    if (meta.hasNext)     html += `<button class="btn btn-outline" onclick="changePage(${meta.page + 1})">Next</button>`;
    container.innerHTML = html;
}

window.changePage = (page) => { currentReqPage = page; loadRequests(page); };

// ─── Users ─────────────────────────────────────────────────────
async function loadUsers() {
    try {
        const tbody = document.querySelector('#admin-users-table tbody');
        tbody.innerHTML = '<tr><td colspan="5" class="empty-state"><div class="spinner"></div></td></tr>';
        const res = await apiGet('/users');
        const users = res.data;

        techniciansCache = users.filter(u => u.role === 'TECHNICIAN');

        if (users.length === 0) {
            tbody.innerHTML = `<tr><td colspan="5" class="empty-state">No users found.</td></tr>`;
            return;
        }

        tbody.innerHTML = users.map(u => {
            const roleColor = u.role === 'ADMIN' ? 'var(--status-urgent)' : u.role === 'TECHNICIAN' ? 'var(--status-assigned)' : 'var(--status-closed)';
            return `<tr>
                <td>#${u.id}</td>
                <td>${u.name}</td>
                <td>${u.email}</td>
                <td>${u.phone || '-'}</td>
                <td><span class="user-role-badge" style="background:${roleColor}">${u.role}</span></td>
            </tr>`;
        }).join('');
    } catch (err) {
        Toast.show('Failed to load users', 'error');
    }
}

// ─── Request Detail Modal ──────────────────────────────────────
window.viewAdminRequestDetails = async (id) => {
    currentManagingRequestId = id;
    Modal.open('request-details');
    const content        = document.getElementById('request-details-content');
    const assignSection  = document.getElementById('assignment-section');
    const assignInfo     = document.getElementById('current-assignment-info');
    const actions        = document.getElementById('request-details-actions');

    content.innerHTML = '<div class="spinner"></div>';
    assignSection.style.display = 'none';

    try {
        const res = await apiGet(`/requests/${id}`);
        const req = res.data;
        const cat = getCatForIdAdmin(req.categoryId);
        const catIconHtml = cat ? getCategoryIconHtml(cat.name) : '';

        content.innerHTML = `
            <div style="display:flex;align-items:center;gap:0.75rem;margin-bottom:1rem;padding-bottom:1rem;border-bottom:1px solid var(--border-color);">
                ${catIconHtml}
                <div>
                    <p style="margin:0;font-size:0.75rem;color:var(--text-secondary);font-weight:600;text-transform:uppercase;">${cat ? cat.name : 'Service'}</p>
                    <h4 style="margin:0.2rem 0;font-size:1rem;">${req.title}</h4>
                    <div style="display:flex;gap:0.4rem;">${Format.badge(req.status)} ${Format.badge(req.priority)}</div>
                </div>
            </div>
            <div style="display:grid;grid-template-columns:1fr 1fr;gap:0.6rem;margin-bottom:1rem;">
                <div style="background:var(--bg-main);padding:0.75rem;border-radius:var(--radius-md);border:1px solid var(--border-color);">
                    <p style="margin:0;font-size:0.72rem;color:var(--text-secondary);font-weight:600;">LOCATION</p>
                    <p style="margin:0.2rem 0 0;font-size:0.875rem;">${req.location}</p>
                </div>
                <div style="background:var(--bg-main);padding:0.75rem;border-radius:var(--radius-md);border:1px solid var(--border-color);">
                    <p style="margin:0;font-size:0.72rem;color:var(--text-secondary);font-weight:600;">CREATED</p>
                    <p style="margin:0.2rem 0 0;font-size:0.875rem;">${Format.date(req.createdAt)}</p>
                </div>
            </div>
            <h5 style="margin-bottom:0.5rem;color:var(--text-secondary);font-size:0.78rem;text-transform:uppercase;letter-spacing:0.05em;">Description</h5>
            <div style="white-space:pre-wrap;color:var(--text-primary);font-size:0.85rem;background:var(--bg-surface);padding:0.85rem;border:1px solid var(--border-color);border-radius:var(--radius-md);">${req.description}</div>
        `;

        // Populate technicians dropdown
        if (techniciansCache.length === 0) await loadUsers();
        const select = document.getElementById('assign-technician-select');
        select.innerHTML = '<option value="">Choose a technician...</option>' +
            techniciansCache.map(t => `<option value="${t.id}">${t.name}</option>`).join('');

        // Load active assignment info
        const assignRes = await apiGet(`/requests/${id}/assignment`);
        const assignments = assignRes.data;
        const active = assignments.find(a => !a.completedAt);

        if (active) {
            const techName = active.technician ? active.technician.name : `Technician #${active.technicianId}`;
            assignInfo.innerHTML = `<div style="background:color-mix(in srgb,var(--primary-color) 8%,var(--bg-surface));padding:0.65rem 0.9rem;border-radius:var(--radius-md);border:1px solid color-mix(in srgb,var(--primary-color) 25%,transparent);font-size:0.85rem;">
                <i class="fas fa-user-check" style="color:var(--primary-color);margin-right:0.4rem;"></i>
                Currently assigned to <strong>${techName}</strong>
            </div>`;
        } else {
            assignInfo.innerHTML = `<p style="color:var(--text-secondary);font-size:0.85rem;margin:0;">No technician currently assigned.</p>`;
        }

        if (req.status === 'PENDING' || req.status === 'ASSIGNED') {
            assignSection.style.display = 'block';
        }

        // Footer actions
        actions.innerHTML = '<button class="btn btn-outline modal-close">Close</button>';
        if (req.status === 'RESOLVED') {
            actions.innerHTML += `<button class="btn btn-primary" onclick="adminUpdateStatus(${req.id}, 'CLOSED')"><i class="fas fa-archive"></i> Close Request</button>`;
        } else if (req.status === 'PENDING') {
            actions.innerHTML += `<button class="btn btn-danger" onclick="adminUpdateStatus(${req.id}, 'CANCELLED')"><i class="fas fa-ban"></i> Cancel</button>`;
        }

    } catch (err) {
        content.innerHTML = `<div class="empty-state">Failed to load request details</div>`;
    }
};

window.adminUpdateStatus = async (id, status) => {
    try {
        await apiPatch(`/requests/${id}/status`, { status });
        Toast.show('Status updated successfully', 'success');
        Modal.close('request-details');
        loadRequests(currentReqPage);
        loadStatistics();
    } catch (err) {
        Toast.show(err.message, 'error');
    }
};

async function handleAssignTechnician() {
    const select = document.getElementById('assign-technician-select');
    const techId = parseInt(select.value);

    if (!techId) {
        Toast.show('Please select a technician', 'warning');
        return;
    }

    const btn = document.getElementById('btn-assign-tech');
    btn.disabled = true;
    btn.innerHTML = '<i class="fas fa-spinner fa-spin"></i>';

    try {
        await apiPost(`/requests/${currentManagingRequestId}/assignment`, { technicianId: techId });
        Toast.show('Technician assigned successfully', 'success');
        viewAdminRequestDetails(currentManagingRequestId);
        loadRequests(currentReqPage);
        loadStatistics();
    } catch (err) {
        Toast.show(err.message, 'error');
    } finally {
        btn.disabled = false;
        btn.innerHTML = '<i class="fas fa-user-check"></i> Assign';
    }
}
