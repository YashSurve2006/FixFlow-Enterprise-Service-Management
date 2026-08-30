CREATE DATABASE IF NOT EXISTS fixflow_db;
USE fixflow_db;

CREATE TABLE IF NOT EXISTS users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    phone VARCHAR(20),
    role VARCHAR(20) NOT NULL DEFAULT 'USER',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS service_categories (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS service_requests (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    category_id INT NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    priority VARCHAR(20) NOT NULL DEFAULT 'LOW',
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    location VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (category_id) REFERENCES service_categories(id) ON DELETE RESTRICT,
    INDEX idx_status (status),
    INDEX idx_user_id (user_id),
    INDEX idx_category_id (category_id),
    INDEX idx_priority (priority),
    INDEX idx_created_at (created_at)
);

CREATE TABLE IF NOT EXISTS request_assignments (
    id INT AUTO_INCREMENT PRIMARY KEY,
    request_id INT NOT NULL,
    technician_id INT NOT NULL,
    assigned_by INT NOT NULL,
    assigned_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    accepted_at TIMESTAMP NULL,
    completed_at TIMESTAMP NULL,
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (request_id) REFERENCES service_requests(id) ON DELETE CASCADE,
    FOREIGN KEY (technician_id) REFERENCES users(id) ON DELETE RESTRICT,
    FOREIGN KEY (assigned_by) REFERENCES users(id) ON DELETE RESTRICT,
    INDEX idx_request_id (request_id),
    INDEX idx_technician_id (technician_id)
);

-- ================================================================
-- SEED: Service Categories (idempotent — safe to re-run)
-- ================================================================
INSERT INTO service_categories (name, description)
SELECT 'Electrical', 'Electrical maintenance including switches, sockets, fans, power supply, wiring, circuit breakers, and all electrical equipment and installations.'
WHERE NOT EXISTS (SELECT 1 FROM service_categories WHERE name = 'Electrical');

INSERT INTO service_categories (name, description)
SELECT 'Plumbing', 'Maintenance of water supply, drainage systems, taps, pipes, leakage control, blocked drains, water tanks, and all plumbing fixtures.'
WHERE NOT EXISTS (SELECT 1 FROM service_categories WHERE name = 'Plumbing');

INSERT INTO service_categories (name, description)
SELECT 'HVAC / AC', 'Air conditioning, heating, ventilation, and temperature-control maintenance including AC units, ducting, filters, and thermostat systems.'
WHERE NOT EXISTS (SELECT 1 FROM service_categories WHERE name = 'HVAC / AC');

INSERT INTO service_categories (name, description)
SELECT 'Furniture & Carpentry', 'Repair and maintenance of furniture, doors, windows, cabinets, shelving, wooden fixtures, and general carpentry work.'
WHERE NOT EXISTS (SELECT 1 FROM service_categories WHERE name = 'Furniture & Carpentry');

INSERT INTO service_categories (name, description)
SELECT 'IT Support', 'Technical support for computers, laptops, printers, projectors, scanners, software issues, and all IT hardware and peripherals.'
WHERE NOT EXISTS (SELECT 1 FROM service_categories WHERE name = 'IT Support');

INSERT INTO service_categories (name, description)
SELECT 'Network & Internet', 'Wi-Fi connectivity, LAN cabling, internet access issues, network switches, routers, access points, and campus network infrastructure support.'
WHERE NOT EXISTS (SELECT 1 FROM service_categories WHERE name = 'Network & Internet');

INSERT INTO service_categories (name, description)
SELECT 'Cleaning & Housekeeping', 'Deep cleaning services, routine housekeeping, waste disposal, sanitization of rooms, corridors, restrooms, and common areas.'
WHERE NOT EXISTS (SELECT 1 FROM service_categories WHERE name = 'Cleaning & Housekeeping');

INSERT INTO service_categories (name, description)
SELECT 'Lift / Elevator', 'Maintenance, breakdown response, and safety inspections for all passenger lifts, service elevators, and escalators on campus.'
WHERE NOT EXISTS (SELECT 1 FROM service_categories WHERE name = 'Lift / Elevator');

INSERT INTO service_categories (name, description)
SELECT 'Security', 'Security system maintenance including CCTV cameras, access control, door locks, alarm systems, intercom, and physical security concerns.'
WHERE NOT EXISTS (SELECT 1 FROM service_categories WHERE name = 'Security');

INSERT INTO service_categories (name, description)
SELECT 'Building Maintenance', 'General building upkeep including walls, ceilings, floors, roofing, waterproofing, painting, and structural integrity maintenance.'
WHERE NOT EXISTS (SELECT 1 FROM service_categories WHERE name = 'Building Maintenance');

INSERT INTO service_categories (name, description)
SELECT 'Civil / Infrastructure', 'Civil engineering concerns including roads, pathways, drainage channels, boundary walls, parking lots, and infrastructure repairs.'
WHERE NOT EXISTS (SELECT 1 FROM service_categories WHERE name = 'Civil / Infrastructure');

INSERT INTO service_categories (name, description)
SELECT 'Fire & Safety', 'Fire safety system maintenance including fire extinguishers, sprinklers, smoke detectors, fire alarm panels, emergency exits, and safety drills.'
WHERE NOT EXISTS (SELECT 1 FROM service_categories WHERE name = 'Fire & Safety');

INSERT INTO service_categories (name, description)
SELECT 'Lighting', 'Installation and repair of all indoor and outdoor lighting, streetlights, emergency lighting, fluorescent tubes, LED panels, and lighting controls.'
WHERE NOT EXISTS (SELECT 1 FROM service_categories WHERE name = 'Lighting');

INSERT INTO service_categories (name, description)
SELECT 'Garden & Landscaping', 'Maintenance of lawns, gardens, plants, trees, irrigation systems, outdoor spaces, landscaping, and groundskeeping services.'
WHERE NOT EXISTS (SELECT 1 FROM service_categories WHERE name = 'Garden & Landscaping');

INSERT INTO service_categories (name, description)
SELECT 'Parking', 'Parking area maintenance including line marking, barrier gates, signage, parking sensors, lighting, and general parking facility management.'
WHERE NOT EXISTS (SELECT 1 FROM service_categories WHERE name = 'Parking');

INSERT INTO service_categories (name, description)
SELECT 'Logistics', 'Internal logistics support including furniture moving, equipment shifting, material handling, delivery coordination, and storage management.'
WHERE NOT EXISTS (SELECT 1 FROM service_categories WHERE name = 'Logistics');

INSERT INTO service_categories (name, description)
SELECT 'Classroom & Lab Maintenance', 'Maintenance of classrooms, laboratories, lecture halls, and academic spaces including whiteboards, lab benches, AV equipment, and learning aids.'
WHERE NOT EXISTS (SELECT 1 FROM service_categories WHERE name = 'Classroom & Lab Maintenance');

INSERT INTO service_categories (name, description)
SELECT 'General Maintenance', 'All-purpose maintenance requests that do not fall into a specific category. Use this for miscellaneous facility and repair issues.'
WHERE NOT EXISTS (SELECT 1 FROM service_categories WHERE name = 'General Maintenance');
