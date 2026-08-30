/**
 * catalog.js — Shared Service Catalog utilities
 * Used by user.js, technician.js, and admin.js
 */

// Map category names → icon class and color class
const CATEGORY_META = {
    'Electrical':               { icon: 'fa-bolt',           cls: 'electrical' },
    'Plumbing':                 { icon: 'fa-faucet',         cls: 'plumbing'   },
    'HVAC / AC':                { icon: 'fa-snowflake',      cls: 'hvac'       },
    'Furniture & Carpentry':    { icon: 'fa-couch',          cls: 'furniture'  },
    'IT Support':               { icon: 'fa-laptop',         cls: 'it'         },
    'Network & Internet':       { icon: 'fa-wifi',           cls: 'network'    },
    'Cleaning & Housekeeping':  { icon: 'fa-broom',          cls: 'cleaning'   },
    'Lift / Elevator':          { icon: 'fa-elevator',       cls: 'lift'       },
    'Security':                 { icon: 'fa-shield-halved',  cls: 'security'   },
    'Building Maintenance':     { icon: 'fa-building',       cls: 'building'   },
    'Civil / Infrastructure':   { icon: 'fa-road',           cls: 'civil'      },
    'Fire & Safety':            { icon: 'fa-fire-extinguisher', cls: 'fire'    },
    'Lighting':                 { icon: 'fa-lightbulb',      cls: 'lighting'   },
    'Garden & Landscaping':     { icon: 'fa-leaf',           cls: 'garden'     },
    'Parking':                  { icon: 'fa-car',            cls: 'parking'    },
    'Logistics':                { icon: 'fa-truck',          cls: 'logistics'  },
    'Classroom & Lab Maintenance': { icon: 'fa-chalkboard',  cls: 'classroom'  },
    'General Maintenance':      { icon: 'fa-screwdriver-wrench', cls: 'general'},
};

// Contextual description hints per category (used in request creation form)
const CATEGORY_HINTS = {
    'Electrical':               'e.g. Tripped switch, faulty socket, flickering lights, power failure in Room 204',
    'Plumbing':                 'e.g. Leaking tap in Lab B, blocked drain near hostel, water supply interruption',
    'HVAC / AC':                'e.g. AC not cooling in Room 204, ventilation fan broken, temperature uncontrollable',
    'Furniture & Carpentry':    'e.g. Broken chair in Seminar Hall 3, stuck door in Admin Block, damaged shelf',
    'IT Support':               'e.g. Laptop not starting in Computer Lab 2, projector not displaying, printer offline',
    'Network & Internet':       'e.g. Wi-Fi not working on Floor 2, LAN port dead, internet slow in Library',
    'Cleaning & Housekeeping':  'e.g. Washroom requires deep clean, spillage in corridor, dustbins not emptied',
    'Lift / Elevator':          'e.g. Elevator stuck between floors, lift door not closing, unusual grinding noise',
    'Security':                 'e.g. CCTV camera offline at Gate 2, broken access card reader, alarm malfunction',
    'Building Maintenance':     'e.g. Ceiling plaster falling in Lecture Hall 1, wall crack in B Block, roof leaking',
    'Civil / Infrastructure':   'e.g. Pothole near main entrance, broken pathway tiles, boundary wall damaged',
    'Fire & Safety':            'e.g. Fire extinguisher expired in Lab 3, smoke detector beeping, emergency exit blocked',
    'Lighting':                 'e.g. Streetlight not working near parking, fluorescent tube flickering in office, corridor dark',
    'Garden & Landscaping':     'e.g. Overgrown grass near hostel, fallen tree branch blocking path, irrigation pipe broken',
    'Parking':                  'e.g. Barrier gate not opening, parking lines faded, pothole in parking area',
    'Logistics':                'e.g. Need furniture moved to New Block, equipment relocation from Lab 4 to Lab 6',
    'Classroom & Lab Maintenance': 'e.g. Whiteboard damaged in Room 301, lab bench broken in Chemistry Lab, AV system faulty',
    'General Maintenance':      'e.g. Describe the issue, location, and any relevant details about what needs fixing',
};

// Title placeholder hints per category
const CATEGORY_TITLE_HINTS = {
    'Electrical':               'e.g. Power socket not working in Room 204',
    'Plumbing':                 'e.g. Leaking pipe in Gents washroom, Ground Floor',
    'HVAC / AC':                'e.g. AC not cooling in Seminar Hall 2',
    'Furniture & Carpentry':    'e.g. Broken chair in Lecture Hall 3',
    'IT Support':               'e.g. Laptop unable to boot in Computer Lab 1',
    'Network & Internet':       'e.g. Wi-Fi not connecting on Library Floor 2',
    'Cleaning & Housekeeping':  'e.g. Washroom cleaning required at Hostel Block A',
    'Lift / Elevator':          'e.g. Elevator door not closing on 3rd Floor',
    'Security':                 'e.g. CCTV camera offline at North Gate',
    'Building Maintenance':     'e.g. Ceiling water leakage in Admin Block Room 5',
    'Civil / Infrastructure':   'e.g. Cracked pathway near Main Building entrance',
    'Fire & Safety':            'e.g. Fire extinguisher expired in Chemistry Lab',
    'Lighting':                 'e.g. Streetlight not working near Hostel parking',
    'Garden & Landscaping':     'e.g. Overgrown grass blocking walkway near Block B',
    'Parking':                  'e.g. Barrier gate stuck open at Staff Parking',
    'Logistics':                'e.g. Equipment to be shifted from Lab 3 to Lab 5',
    'Classroom & Lab Maintenance': 'e.g. Projector not working in Lecture Hall 4',
    'General Maintenance':      'e.g. Brief one-line summary of the issue',
};

/**
 * Returns the icon HTML string for a given category name.
 * Falls back to a generic wrench icon.
 */
function getCategoryIconHtml(catName, extraClass = '') {
    const meta = CATEGORY_META[catName] || { icon: 'fa-screwdriver-wrench', cls: 'general' };
    return `<div class="catalog-icon ${meta.cls} ${extraClass}"><i class="fas ${meta.icon}"></i></div>`;
}

/**
 * Renders the visual Service Catalog grid into a container element.
 * @param {HTMLElement} container - Element to render into
 * @param {Array} categories - List of category objects from API
 * @param {Function} onSelect - Callback(categoryId, categoryName) when user clicks a card
 * @param {Object} countMap - Optional map of { categoryId: requestCount }
 */
function renderCatalogGrid(container, categories, onSelect, countMap = {}) {
    if (!container) return;
    if (categories.length === 0) {
        container.innerHTML = `<p class="empty-state">No service categories available.</p>`;
        return;
    }

    container.innerHTML = categories.map(cat => {
        const meta = CATEGORY_META[cat.name] || { icon: 'fa-screwdriver-wrench', cls: 'general' };
        const count = countMap[cat.id] !== undefined ? countMap[cat.id] : null;
        const countHtml = count !== null
            ? `<div class="catalog-card-count"><i class="fas fa-file-alt"></i> ${count} request${count !== 1 ? 's' : ''}</div>`
            : '';
        return `
        <div class="catalog-card" data-cat-id="${cat.id}" data-cat-name="${cat.name}" tabindex="0" role="button" aria-label="Select ${cat.name}">
            <div class="catalog-icon ${meta.cls}"><i class="fas ${meta.icon}"></i></div>
            <div class="catalog-card-name">${cat.name}</div>
            <div class="catalog-card-desc">${cat.description || ''}</div>
            ${countHtml}
        </div>`;
    }).join('');

    // Attach click events
    container.querySelectorAll('.catalog-card').forEach(card => {
        card.addEventListener('click', () => {
            container.querySelectorAll('.catalog-card').forEach(c => c.classList.remove('selected'));
            card.classList.add('selected');
            onSelect(parseInt(card.dataset.catId), card.dataset.catName);
        });
        card.addEventListener('keydown', (e) => {
            if (e.key === 'Enter' || e.key === ' ') {
                e.preventDefault();
                card.click();
            }
        });
    });
}

/**
 * Updates placeholder text and contextual hints in the request creation form
 * when a category is selected.
 * @param {string} catName - Selected category name
 */
function updateFormHintsForCategory(catName) {
    const titleInput = document.getElementById('req-title');
    const descInput  = document.getElementById('req-description');
    const hintEl     = document.getElementById('req-description-hint');

    if (titleInput) {
        titleInput.placeholder = CATEGORY_TITLE_HINTS[catName] || 'Brief summary of the issue';
    }
    if (descInput) {
        descInput.placeholder = CATEGORY_HINTS[catName] || 'Describe the issue in detail...';
    }
    if (hintEl) {
        hintEl.textContent = CATEGORY_HINTS[catName] ? `Tip: ${CATEGORY_HINTS[catName]}` : '';
    }
}
