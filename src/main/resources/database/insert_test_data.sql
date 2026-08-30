USE fixflow_db;

-- Ignore duplicates
INSERT IGNORE INTO users (name, email, password, role) VALUES 
('Test User', 'user@fixflow.local', '$2b$12$02Dk1jjXm9iLd7NdhS6djufE6qlgRHVJn6H6fHySiMWAFJd50cJ0G', 'USER'),
('Test Technician', 'technician@fixflow.local', '$2b$12$02Dk1jjXm9iLd7NdhS6djufE6qlgRHVJn6H6fHySiMWAFJd50cJ0G', 'TECHNICIAN'),
('Test Admin', 'admin@fixflow.local', '$2b$12$02Dk1jjXm9iLd7NdhS6djufE6qlgRHVJn6H6fHySiMWAFJd50cJ0G', 'ADMIN');

INSERT IGNORE INTO service_categories (name, description) VALUES 
('Electrical', 'Electrical issues and maintenance'),
('Plumbing', 'Plumbing issues and maintenance');
