// User Dashboard Logic
let currentPage = 1;
let categoriesCache = [];
let selectedCategoryId = null;

document.addEventListener('DOMContentLoaded', () => {
    Auth.requireAuth('USER');

    // Set avatar initial
    const user = Auth.getUser();
    if (user && user.name) {
        const initial = user.name.charAt(0).toUpperCase();
        const avatarEl = document.getElementById('avatar-initial');
        if (avatarEl) avatarEl.textContent = initial;
        const nameEl = document.getElementById('current-user-name');
        if (nameEl) nameEl.textContent = user.name;
    }

    // Listen for view changes
    document.addEventListener('viewChanged', (e) => {
        if (e.detail === 'view-overview') {
            loadRequests(1);
            loadOverviewCatalog();
        } else if (e.detail === 'view-my-requests') {
            loadRequests(1);
        } else if (e.detail === 'view-catalog') {
            loadFullCatalog();
        } else if (e.detail === 'view-create-request') {
            loadCreateCatalog();
        }
    });

    // Initial load
    loadRequests(1);
    loadOverviewCatalog();

    // Events
    document.getElementById('btn-apply-filters').addEventListener('click', () => {
        currentPage = 1;
        loadRequests(1);
    });

    document.getElementById('form-create-request').addEventListener('submit', handleCreateRequest);

    // "Change Category" button resets the 2-step form
    document.getElementById('btn-change-category').addEventListener('click', () => {
        document.getElementById('step-fill-form').style.display = 'none';
        document.getElementById('step-select-category').style.display = 'block';
        selectedCategoryId = null;
        document.querySelectorAll('#create-catalog-grid .catalog-card').forEach(c => c.classList.remove('selected'));
    });

    // Overview quick-select: clicking a catalog card goes to create form
    document.getElementById('overview-catalog-grid').addEventListener('click', (e) => {
        const card = e.target.closest('.catalog-card');
        if (card) {
            // Switch to create view and pre-select category
            document.querySelector('.nav-link[data-target="view-create-request"]').click();
        }
    });
});

// ─── Categories ────────────────────────────────────────────────
async function fetchCategories() {
    if (categoriesCache.length > 0) return categoriesCache;
    const res = await apiGet('/categories');
    categoriesCache = res.data;
    populateCategoryFilterDropdown(categoriesCache);
    return categoriesCache;
}

function populateCategoryFilterDropdown(cats) {
    const sel = document.getElementById('filter-category');
    if (!sel) return;
    sel.innerHTML = '<option value="">All Categories</option>' +
        cats.map(c => `<option value="${c.id}">${c.name}</option>`).join('');
}

// ─── Catalog Grids ─────────────────────────────────────────────
async function loadOverviewCatalog() {
    try {
        const cats = await fetchCategories();
        const container = document.getElementById('overview-catalog-grid');
        // Show only first 6 in overview for brevity
        renderCatalogGrid(container, cats.slice(0, 6), (catId, catName) => {
            // Clicking from overview navigates to create request with pre-selection
            document.querySelector('.nav-link[data-target="view-create-request"]').click();
            setTimeout(() => preselectCategory(catId, catName), 100);
        });
    } catch (err) {
        console.error('Overview catalog error:', err);
    }
}

async function loadFullCatalog() {
    try {
        const cats = await fetchCategories();
        const container = document.getElementById('full-catalog-grid');
        renderCatalogGrid(container, cats, (catId, catName) => {
            // Clicking from catalog navigates to create request
            document.querySelector('.nav-link[data-target="view-create-request"]').click();
            setTimeout(() => preselectCategory(catId, catName), 100);
        });
    } catch (err) {
        Toast.show('Failed to load service catalog', 'error');
    }
}

async function loadCreateCatalog() {
    try {
        const cats = await fetchCategories();
        const container = document.getElementById('create-catalog-grid');
        renderCatalogGrid(container, cats, (catId, catName) => {
            preselectCategory(catId, catName);
        });
        // If category already selected, restore visual state
        if (selectedCategoryId) {
            const card = container.querySelector(`[data-cat-id="${selectedCategoryId}"]`);
            if (card) card.classList.add('selected');
        }
    } catch (err) {
        Toast.show('Failed to load categories', 'error');
    }
}

function preselectCategory(catId, catName) {
    selectedCategoryId = catId;
    document.getElementById('req-category-id').value = catId;

    // Show form, hide catalog
    document.getElementById('step-select-category').style.display = 'none';
    document.getElementById('step-fill-form').style.display = 'block';

    // Update header
    document.getElementById('selected-cat-title').textContent = catName;
    document.getElementById('selected-cat-subtitle').textContent = 'Fill in the details below';
    document.getElementById('selected-cat-icon').innerHTML = getCategoryIconHtml(catName);

    // Update contextual hints
    updateFormHintsForCategory(catName);
}

// ─── Requests ──────────────────────────────────────────────────
async function loadRequests(page) {
    try {
        const tbody = document.querySelector('#requests-table tbody');
        if (page === 1 && !document.getElementById('filter-search').value) {
            tbody.innerHTML = '<tr><td colspan="8" class="empty-state"><div class="spinner"></div> Loading...</td></tr>';
        }

        let query = `?page=${page}&limit=10&sortBy=createdAt&sortOrder=desc`;

        const status   = document.getElementById('filter-status').value;
        const priority = document.getElementById('filter-priority').value;
        const search   = document.getElementById('filter-search').value;
        const catEl    = document.getElementById('filter-category');
        const catId    = catEl ? catEl.value : '';

        if (status)   query += `&status=${status}`;
        if (priority) query += `&priority=${priority}`;
        if (search)   query += `&search=${encodeURIComponent(search)}`;
        if (catId)    query += `&categoryId=${catId}`;

        const res  = await apiGet(`/requests${query}`);
        const data = res.data;
        const meta = res.pagination;

        // Update stat cards from first unfiltered page
        if (!status && !priority && !search && !catId) {
            updateStats(meta.totalItems, data);
        }

        // Ensure categories are loaded for display
        const cats = await fetchCategories();
        const catMap = {};
        cats.forEach(c => { catMap[c.id] = c; });

        if (data.length === 0) {
            tbody.innerHTML = `<tr><td colspan="8" class="empty-state">No requests found.</td></tr>`;
        } else {
            tbody.innerHTML = data.map(req => {
                const cat = catMap[req.categoryId];
                const catMeta = cat ? (CATEGORY_META[cat.name] || { icon: 'fa-screwdriver-wrench', cls: 'general' }) : null;
                const catHtml = cat
                    ? `<div style="display:flex;align-items:center;gap:0.4rem;">
                          <div class="catalog-icon ${catMeta.cls}" style="width:24px;height:24px;border-radius:6px;font-size:0.65rem;">
                              <i class="fas ${catMeta.icon}"></i>
                          </div>
                          <span style="font-size:0.8rem;color:var(--text-secondary);">${cat.name}</span>
                       </div>`
                    : '-';
                return `
                <tr>
                    <td>#${req.id}</td>
                    <td>${catHtml}</td>
                    <td><strong>${req.title}</strong></td>
                    <td>${req.location}</td>
                    <td>${Format.badge(req.priority)}</td>
                    <td>${Format.badge(req.status)}</td>
                    <td>${Format.date(req.createdAt)}</td>
                    <td>
                        <button class="btn btn-outline" onclick="viewRequestDetails(${req.id})" title="View Details"><i class="fas fa-eye"></i></button>
                    </td>
                </tr>`;
            }).join('');
        }

        renderPagination(meta);

    } catch (err) {
        Toast.show('Failed to load requests', 'error');
        document.querySelector('#requests-table tbody').innerHTML = `<tr><td colspan="8" class="empty-state">Error loading data.</td></tr>`;
    }
}

function updateStats(total, dataSubset) {
    document.getElementById('stat-total').textContent = total;
    const pending    = dataSubset.filter(r => r.status === 'PENDING').length;
    const inProgress = dataSubset.filter(r => r.status === 'IN_PROGRESS' || r.status === 'ASSIGNED').length;
    const resolved   = dataSubset.filter(r => r.status === 'RESOLVED' || r.status === 'CLOSED').length;

    const prefix = total > 10 ? '~' : '';
    document.getElementById('stat-pending').textContent    = `${prefix}${pending}`;
    document.getElementById('stat-inprogress').textContent = `${prefix}${inProgress}`;
    document.getElementById('stat-resolved').textContent   = `${prefix}${resolved}`;
}

function renderPagination(meta) {
    const container = document.getElementById('pagination-container');
    if (meta.totalPages <= 1) { container.innerHTML = ''; return; }

    let html = `<span style="font-size: 0.875rem; color: var(--text-secondary);">Page ${meta.page} of ${meta.totalPages}</span>`;
    html += `<div class="pagination-buttons">`;
    html += `<button class="btn btn-outline" onclick="changePage(${meta.page - 1})" ${meta.hasPrevious ? '' : 'disabled'}><i class="fas fa-chevron-left"></i></button>`;
    html += `<button class="btn btn-outline" onclick="changePage(${meta.page + 1})" ${meta.hasNext ? '' : 'disabled'}><i class="fas fa-chevron-right"></i></button>`;
    html += `</div>`;
    container.innerHTML = html;
}

window.changePage = (page) => { currentPage = page; loadRequests(page); };

// ─── Create Request ────────────────────────────────────────────
async function handleCreateRequest(e) {
    e.preventDefault();
    const catId = parseInt(document.getElementById('req-category-id').value);
    if (!catId) {
        Toast.show('Please select a service category first', 'warning');
        return;
    }

    const btn = document.getElementById('btn-submit-request');
    btn.disabled = true;
    btn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Submitting...';

    const payload = {
        title:       document.getElementById('req-title').value.trim(),
        categoryId:  catId,
        location:    document.getElementById('req-location').value.trim(),
        priority:    document.getElementById('req-priority').value,
        description: document.getElementById('req-description').value.trim()
    };

    try {
        await apiPost('/requests', payload);
        Toast.show('Service request submitted successfully!', 'success');
        e.target.reset();
        selectedCategoryId = null;
        document.getElementById('step-fill-form').style.display = 'none';
        document.getElementById('step-select-category').style.display = 'block';
        document.querySelector('.nav-link[data-target="view-my-requests"]').click();
    } catch (err) {
        Toast.show(err.message, 'error');
    } finally {
        btn.disabled = false;
        btn.innerHTML = '<i class="fas fa-paper-plane"></i> Submit Service Request';
    }
}

// ─── Request Details Modal ─────────────────────────────────────
window.viewRequestDetails = async (id) => {
    Modal.open('request-details');
    const content = document.getElementById('request-details-content');
    content.innerHTML = '<div class="empty-state"><div class="spinner"></div></div>';

    try {
        const [reqRes, cats] = await Promise.all([apiGet(`/requests/${id}`), fetchCategories()]);
        const req = reqRes.data;
        const cat = cats.find(c => c.id === req.categoryId);
        const catIconHtml = cat ? getCategoryIconHtml(cat.name) : '';

        const flow = ['PENDING', 'ASSIGNED', 'IN_PROGRESS', 'RESOLVED', 'CLOSED'];
        const getStepClass = (step) => {
            if (step === req.status) return 'active';
            if (flow.indexOf(step) < flow.indexOf(req.status)) return 'completed';
            return '';
        };

        const timeline = `
            <div class="workflow-timeline">
                ${[['PENDING','file-alt'],['ASSIGNED','user-check'],['IN_PROGRESS','tools'],['RESOLVED','check-double'],['CLOSED','archive']].map(([s, ico]) => `
                <div class="timeline-step ${getStepClass(s)}">
                    <div class="timeline-icon"><i class="fas fa-${ico}"></i></div>
                    <div class="timeline-label">${s.replace('_',' ')}</div>
                </div>`).join('')}
            </div>`;

        content.innerHTML = `
            <div style="display:flex; align-items:center; gap:0.75rem; margin-bottom:1rem;">
                ${catIconHtml}
                <div>
                    <h4 style="margin:0 0 0.3rem; font-size:1.1rem;">${req.title}</h4>
                    <div style="display:flex; gap:0.4rem;">${Format.badge(req.status)} ${Format.badge(req.priority)}</div>
                </div>
            </div>
            ${timeline}
            <div style="background:var(--bg-main);padding:1rem;border-radius:var(--radius-md);border:1px solid var(--border-color);margin:1rem 0;display:grid;grid-template-columns:1fr 1fr;gap:0.5rem;">
                <p style="margin:0;font-size:0.85rem;"><strong style="color:var(--text-secondary);">Service</strong><br>${cat ? cat.name : 'N/A'}</p>
                <p style="margin:0;font-size:0.85rem;"><strong style="color:var(--text-secondary);">Location</strong><br>${req.location}</p>
                <p style="margin:0;font-size:0.85rem;"><strong style="color:var(--text-secondary);">Created</strong><br>${Format.date(req.createdAt)}</p>
                <p style="margin:0;font-size:0.85rem;"><strong style="color:var(--text-secondary);">Updated</strong><br>${Format.date(req.updatedAt)}</p>
            </div>
            <h5 style="margin-bottom:0.5rem;color:var(--text-secondary);font-size:0.85rem;">Description</h5>
            <div style="white-space:pre-wrap;color:var(--text-primary);font-size:0.85rem;background:var(--bg-surface);padding:1rem;border:1px solid var(--border-color);border-radius:var(--radius-md);">${req.description}</div>
        `;
    } catch (err) {
        content.innerHTML = `<div class="empty-state"><i class="fas fa-exclamation-circle" style="font-size:2rem;margin-bottom:1rem;color:var(--danger-color);"></i><br>Failed to load details</div>`;
    }
};
