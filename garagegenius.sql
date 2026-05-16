CREATE DATABASE IF NOT EXISTS garagegenius;
USE garagegenius;

-- ==========================================
-- 1. users
-- ==========================================
CREATE TABLE users (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role ENUM('admin', 'staff', 'customer') DEFAULT 'customer',
    phone VARCHAR(20),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status ENUM('active', 'inactive', 'pending') DEFAULT 'pending'
);

-- ==========================================
-- 2. customers
-- ==========================================
CREATE TABLE customers (
    customer_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    address VARCHAR(255),
    city VARCHAR(100),
    loyalty_points INT DEFAULT 0,
    registered_date DATE,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- ==========================================
-- 3. vehicles
-- ==========================================
CREATE TABLE vehicles (
    vehicle_id INT AUTO_INCREMENT PRIMARY KEY,
    customer_id INT NOT NULL,
    make VARCHAR(50) NOT NULL,
    model VARCHAR(50) NOT NULL,
    year YEAR NOT NULL,
    license_plate VARCHAR(20) UNIQUE NOT NULL,
    color VARCHAR(30),
    vin_number VARCHAR(50),
    mileage INT DEFAULT 0,
    fuel_type ENUM('Petrol', 'Diesel', 'Electric', 'Hybrid') NOT NULL,
    FOREIGN KEY (customer_id) REFERENCES customers(customer_id) ON DELETE CASCADE
);

-- ==========================================
-- 4. services
-- ==========================================
CREATE TABLE services (
    service_id INT AUTO_INCREMENT PRIMARY KEY,
    service_name VARCHAR(100) NOT NULL,
    description TEXT,
    base_price DECIMAL(10,2) NOT NULL,
    estimated_duration_hrs DECIMAL(4,2),
    category ENUM('repair', 'maintenance', 'inspection', 'bodywork') NOT NULL
);

-- ==========================================
-- 5. job_cards
-- ==========================================
CREATE TABLE job_cards (
    job_id INT AUTO_INCREMENT PRIMARY KEY,
    vehicle_id INT NOT NULL,
    customer_id INT NOT NULL,
    assigned_staff_id INT,
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    estimated_completion DATETIME,
    actual_completion DATETIME,
    status ENUM('pending', 'in_progress', 'completed', 'cancelled') DEFAULT 'pending',
    total_amount DECIMAL(10,2) DEFAULT 0.00,
    notes TEXT,
    mileage_at_service INT,
    FOREIGN KEY (vehicle_id) REFERENCES vehicles(vehicle_id),
    FOREIGN KEY (customer_id) REFERENCES customers(customer_id),
    FOREIGN KEY (assigned_staff_id) REFERENCES users(user_id)
);

-- ==========================================
-- 6. job_services
-- ==========================================
CREATE TABLE job_services (
    job_service_id INT AUTO_INCREMENT PRIMARY KEY,
    job_id INT NOT NULL,
    service_id INT NOT NULL,
    quantity INT DEFAULT 1,
    unit_price DECIMAL(10,2) NOT NULL,
    subtotal DECIMAL(10,2) NOT NULL,
    FOREIGN KEY (job_id) REFERENCES job_cards(job_id) ON DELETE CASCADE,
    FOREIGN KEY (service_id) REFERENCES services(service_id)
);

-- ==========================================
-- 7. spare_parts
-- ==========================================
CREATE TABLE spare_parts (
    part_id INT AUTO_INCREMENT PRIMARY KEY,
    part_name VARCHAR(100) NOT NULL,
    part_number VARCHAR(50) UNIQUE NOT NULL,
    category VARCHAR(50),
    quantity_in_stock INT DEFAULT 0,
    unit_price DECIMAL(10,2) NOT NULL,
    supplier_name VARCHAR(100),
    reorder_level INT DEFAULT 5,
    last_restocked TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- ==========================================
-- 8. job_parts
-- ==========================================
CREATE TABLE job_parts (
    job_part_id INT AUTO_INCREMENT PRIMARY KEY,
    job_id INT NOT NULL,
    part_id INT NOT NULL,
    quantity_used INT NOT NULL,
    unit_price DECIMAL(10,2) NOT NULL,
    subtotal DECIMAL(10,2) NOT NULL,
    FOREIGN KEY (job_id) REFERENCES job_cards(job_id) ON DELETE CASCADE,
    FOREIGN KEY (part_id) REFERENCES spare_parts(part_id)
);

-- ==========================================
-- 9. invoices
-- ==========================================
CREATE TABLE invoices (
    invoice_id INT AUTO_INCREMENT PRIMARY KEY,
    job_id INT NOT NULL,
    customer_id INT NOT NULL,
    invoice_date DATE NOT NULL,
    due_date DATE NOT NULL,
    subtotal DECIMAL(10,2) NOT NULL,
    tax_rate DECIMAL(5,2) DEFAULT 13.00,
    tax_amount DECIMAL(10,2) NOT NULL,
    discount DECIMAL(10,2) DEFAULT 0.00,
    total_amount DECIMAL(10,2) NOT NULL,
    payment_status ENUM('unpaid', 'paid', 'partial') DEFAULT 'unpaid',
    amount_paid DECIMAL(10,2) DEFAULT 0.00,
    payment_method ENUM('cash', 'card', 'online', '') DEFAULT '',
    payment_date TIMESTAMP NULL,
    FOREIGN KEY (job_id) REFERENCES job_cards(job_id),
    FOREIGN KEY (customer_id) REFERENCES customers(customer_id)
);

-- ==========================================
-- 10. service_history
-- ==========================================
CREATE TABLE service_history (
    history_id INT AUTO_INCREMENT PRIMARY KEY,
    vehicle_id INT NOT NULL,
    job_id INT NOT NULL,
    service_date DATE NOT NULL,
    description TEXT,
    mileage INT,
    next_service_due DATE,
    FOREIGN KEY (vehicle_id) REFERENCES vehicles(vehicle_id),
    FOREIGN KEY (job_id) REFERENCES job_cards(job_id)
);

-- ==========================================
-- 11. inventory_log
-- ==========================================
CREATE TABLE inventory_log (
    log_id INT AUTO_INCREMENT PRIMARY KEY,
    part_id INT NOT NULL,
    action ENUM('restock', 'used', 'adjustment') NOT NULL,
    quantity_change INT NOT NULL,
    previous_stock INT NOT NULL,
    new_stock INT NOT NULL,
    action_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    performed_by INT NOT NULL,
    FOREIGN KEY (part_id) REFERENCES spare_parts(part_id),
    FOREIGN KEY (performed_by) REFERENCES users(user_id)
);

-- ==========================================
-- 12. contacts
-- ==========================================
CREATE TABLE contacts (
    contact_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL,
    phone VARCHAR(20),
    subject VARCHAR(200) NOT NULL,
    message TEXT NOT NULL,
    submitted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status ENUM('new', 'read', 'replied') DEFAULT 'new'
);

-- ==========================================
-- 13. appointments
-- ==========================================
CREATE TABLE appointments (
    appointment_id INT AUTO_INCREMENT PRIMARY KEY,
    customer_id INT NOT NULL,
    vehicle_id INT NOT NULL,
    service_id INT NOT NULL,
    requested_date DATE NOT NULL,
    preferred_time TIME NOT NULL,
    status ENUM('pending', 'approved', 'rejected', 'completed', 'cancelled') DEFAULT 'pending',
    notes TEXT,
    admin_notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (customer_id) REFERENCES customers(customer_id) ON DELETE CASCADE,
    FOREIGN KEY (vehicle_id) REFERENCES vehicles(vehicle_id),
    FOREIGN KEY (service_id) REFERENCES services(service_id)
);

-- ==========================================
-- 14. messages (admin <-> staff internal)
-- ==========================================
CREATE TABLE messages (
    message_id INT AUTO_INCREMENT PRIMARY KEY,
    sender_id INT NOT NULL,
    receiver_id INT NOT NULL,
    job_id INT NULL,
    subject VARCHAR(200) NOT NULL,
    body TEXT NOT NULL,
    status ENUM('unread', 'read') DEFAULT 'unread',
    sent_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    read_at TIMESTAMP NULL,
    FOREIGN KEY (sender_id) REFERENCES users(user_id),
    FOREIGN KEY (receiver_id) REFERENCES users(user_id),
    FOREIGN KEY (job_id) REFERENCES job_cards(job_id) ON DELETE SET NULL
);

-- ==========================================
-- 15. orders (Spare Parts Shop)
-- ==========================================
CREATE TABLE IF NOT EXISTS orders (
    order_id INT AUTO_INCREMENT PRIMARY KEY,
    customer_id INT NOT NULL,
    total_amount DECIMAL(10,2) NOT NULL,
    status ENUM('pending', 'processing', 'completed', 'cancelled') DEFAULT 'pending',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (customer_id) REFERENCES customers(customer_id) ON DELETE CASCADE
);

-- ==========================================
-- 16. order_items
-- ==========================================
CREATE TABLE IF NOT EXISTS order_items (
    order_item_id INT AUTO_INCREMENT PRIMARY KEY,
    order_id INT NOT NULL,
    part_id INT NOT NULL,
    quantity INT NOT NULL,
    unit_price DECIMAL(10,2) NOT NULL,
    subtotal DECIMAL(10,2) NOT NULL,
    FOREIGN KEY (order_id) REFERENCES orders(order_id) ON DELETE CASCADE,
    FOREIGN KEY (part_id) REFERENCES spare_parts(part_id)
);

-- ==========================================
-- INSERT SAMPLE DATA
-- ==========================================

-- 1 Admin User (Admin@123 hashed)
INSERT INTO users (full_name, email, password_hash, role, phone, status) VALUES 
('System Admin', 'admin@garagegenius.com', '$2a$10$82VRo35GqTTEw8H9y3LRjeSQhfEczByXEowvJUaWeakshSlwNACw2', 'admin', '555-0000', 'active');

-- 2 Staff Users
INSERT INTO users (full_name, email, password_hash, role, phone, status) VALUES 
('Mechanic Mike', 'mike@garagegenius.com', '$2a$10$Y5HHDA7BdbFSaP1Q85BxeeVTZO/Y6aR3jjnEBJVGw0rt4/KNT11Ba', 'staff', '555-1111', 'active'),
('Service Sarah', 'sarah@garagegenius.com', '$2a$10$Y5HHDA7BdbFSaP1Q85BxeeVTZO/Y6aR3jjnEBJVGw0rt4/KNT11Ba', 'staff', '555-2222', 'active');

-- 5 Customers
INSERT INTO users (full_name, email, password_hash, role, phone, status) VALUES 
('Client One', 'c1@example.com', '$2a$10$ONjvwc4Q8mBkn9HzJV1OiO9MbuBkKG5MtFqjDvt56Y4N1WZ28Fziu', 'customer', '111', 'active'),
('Client Two', 'c2@example.com', '$2a$10$ONjvwc4Q8mBkn9HzJV1OiO9MbuBkKG5MtFqjDvt56Y4N1WZ28Fziu', 'customer', '222', 'active'),
('Client Three', 'c3@example.com', '$2a$10$ONjvwc4Q8mBkn9HzJV1OiO9MbuBkKG5MtFqjDvt56Y4N1WZ28Fziu', 'customer', '333', 'active'),
('Client Four', 'c4@example.com', '$2a$10$ONjvwc4Q8mBkn9HzJV1OiO9MbuBkKG5MtFqjDvt56Y4N1WZ28Fziu', 'customer', '444', 'active'),
('Client Five', 'c5@example.com', '$2a$10$ONjvwc4Q8mBkn9HzJV1OiO9MbuBkKG5MtFqjDvt56Y4N1WZ28Fziu', 'customer', '555', 'active');

-- Add Customers specific data (mapping to user_ids 4 to 8)
INSERT INTO customers (user_id, address, city, loyalty_points, registered_date) VALUES 
(4, '123 Main St', 'Auto City', 10, '2024-01-01'),
(5, '456 Oak Rd', 'Auto City', 50, '2024-02-15'),
(6, '789 Pine Ave', 'Mech Town', 0, '2024-03-10'),
(7, '321 Elm St', 'Carville', 100, '2024-04-05'),
(8, '654 Birch Blvd', 'Auto City', 20, '2024-05-20');

-- Add Vehicles
INSERT INTO vehicles (customer_id, make, model, year, license_plate, color, vin_number, mileage, fuel_type) VALUES 
(1, 'Toyota', 'Camry', 2018, 'ABC-123', 'Silver', 'VIN1234567890', 45000, 'Petrol'),
(1, 'Honda', 'Civic', 2020, 'XYZ-987', 'Black', 'VIN0987654321', 30000, 'Petrol'),
(2, 'Ford', 'F-150', 2015, 'TRK-555', 'Red', 'VIN5555555555', 85000, 'Diesel'),
(3, 'Tesla', 'Model 3', 2023, 'ELC-101', 'White', 'VINTESLA00001', 12000, 'Electric'),
(4, 'BMW', 'X5', 2019, 'BMW-404', 'Blue', 'VINBMWX540404', 60000, 'Diesel');

-- 10 Services in Catalog
INSERT INTO services (service_name, description, base_price, estimated_duration_hrs, category) VALUES 
('Basic Oil Change', 'Engine oil and filter replacement', 49.99, 1.0, 'maintenance'),
('Full Synthetic Oil Change', 'Premium synthetic oil replacement', 79.99, 1.0, 'maintenance'),
('Brake Pad Replacement', 'Front or rear brake pad replacement', 149.99, 2.0, 'repair'),
('Tire Rotation', 'Rotate tires for even wear', 29.99, 0.5, 'maintenance'),
('Wheel Alignment', 'Four-wheel alignment adjustment', 89.99, 1.5, 'maintenance'),
('Battery Replacement', 'Test and replace vehicle battery', 129.99, 0.5, 'repair'),
('AC Recharge', 'Air conditioning system evac and recharge', 99.99, 1.5, 'maintenance'),
('Engine Diagnostics', 'Computer scan and manual diagnostic', 69.99, 1.0, 'inspection'),
('Dent Removal', 'Paintless dent removal per panel', 199.99, 3.0, 'bodywork'),
('Full Detailing', 'Interior and exterior detailing', 149.99, 4.0, 'maintenance');

-- 5 Job Cards
INSERT INTO job_cards (vehicle_id, customer_id, assigned_staff_id, status, total_amount, mileage_at_service) VALUES
(1, 1, 2, 'completed', 49.99, 45100),
(2, 1, 3, 'completed', 149.99, 30200),
(3, 2, 2, 'in_progress', 0.00, 85300),
(4, 3, 3, 'pending', 0.00, 12050),
(5, 4, 2, 'cancelled', 0.00, 60100);

-- 5 Invoices
INSERT INTO invoices (job_id, customer_id, invoice_date, due_date, subtotal, tax_amount, total_amount, payment_status) VALUES
(1, 1, '2024-06-01', '2024-06-08', 49.99, 6.50, 56.49, 'paid'),
(2, 1, '2024-06-10', '2024-06-17', 149.99, 19.50, 169.49, 'paid'),
(3, 2, '2024-06-15', '2024-06-22', 89.99, 11.70, 101.69, 'unpaid');

-- Appointment Requests
INSERT INTO appointments (customer_id, vehicle_id, service_id, requested_date, preferred_time, status, notes, admin_notes) VALUES
(1, 1, 1, '2026-05-05', '09:00:00', 'approved', 'Oil warning light appears after starting.', 'Approved for 9 AM check-in.'),
(2, 3, 8, '2026-05-06', '14:00:00', 'pending', 'Engine noise during acceleration.', NULL);
