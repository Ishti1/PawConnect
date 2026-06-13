-- CatConnect MySQL Schema
CREATE DATABASE IF NOT EXISTS catconnect CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE catconnect;

CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    display_name VARCHAR(100) NOT NULL,
    avatar_url VARCHAR(500),
    city VARCHAR(100) DEFAULT 'Cairo',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE vets (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    address VARCHAR(500),
    phone VARCHAR(50),
    latitude DECIMAL(10, 7),
    longitude DECIMAL(10, 7),
    rating DECIMAL(2, 1) DEFAULT 4.0,
    is_emergency BOOLEAN DEFAULT FALSE,
    open_hours VARCHAR(200),
    city VARCHAR(100) DEFAULT 'Cairo'
);

CREATE TABLE cat_shops (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    address VARCHAR(500),
    phone VARCHAR(50),
    latitude DECIMAL(10, 7),
    longitude DECIMAL(10, 7),
    rating DECIMAL(2, 1) DEFAULT 4.0,
    city VARCHAR(100) DEFAULT 'Cairo'
);

CREATE TABLE shelters (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    address VARCHAR(500),
    phone VARCHAR(50),
    email VARCHAR(255),
    website VARCHAR(500),
    description TEXT,
    capacity INT DEFAULT 50,
    city VARCHAR(100) DEFAULT 'Cairo'
);

CREATE TABLE care_knowledge (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(300) NOT NULL,
    category VARCHAR(100) NOT NULL,
    content TEXT NOT NULL,
    author VARCHAR(100) DEFAULT 'CatConnect Team',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE food_recommendations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    brand VARCHAR(150) NOT NULL,
    product_name VARCHAR(200) NOT NULL,
    age_group VARCHAR(50) NOT NULL,
    health_condition VARCHAR(100) DEFAULT 'General',
    description TEXT,
    rating DECIMAL(2, 1) DEFAULT 4.5
);

CREATE TABLE cat_memes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(200),
    image_url VARCHAR(500) NOT NULL,
    likes INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE adoption_listings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT,
    cat_name VARCHAR(100) NOT NULL,
    breed VARCHAR(100),
    age_months INT,
    gender VARCHAR(20),
    description TEXT,
    image_url VARCHAR(500),
    shelter_id BIGINT,
    status VARCHAR(30) DEFAULT 'AVAILABLE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL,
    FOREIGN KEY (shelter_id) REFERENCES shelters(id) ON DELETE SET NULL
);

CREATE TABLE lost_found_posts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT,
    post_type ENUM('LOST', 'FOUND') NOT NULL,
    cat_description TEXT NOT NULL,
    last_seen_location VARCHAR(500),
    contact_phone VARCHAR(50),
    image_url VARCHAR(500),
    status VARCHAR(30) DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL
);

CREATE TABLE cat_moments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    caption VARCHAR(500),
    image_url VARCHAR(500) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE donation_campaigns (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(300) NOT NULL,
    description TEXT,
    shelter_id BIGINT,
    goal_amount DECIMAL(12, 2) NOT NULL,
    raised_amount DECIMAL(12, 2) DEFAULT 0,
    image_url VARCHAR(500),
    status VARCHAR(30) DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (shelter_id) REFERENCES shelters(id) ON DELETE SET NULL
);

CREATE TABLE donations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    campaign_id BIGINT NOT NULL,
    user_id BIGINT,
    amount DECIMAL(12, 2) NOT NULL,
    message VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (campaign_id) REFERENCES donation_campaigns(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL
);

CREATE INDEX idx_vets_emergency ON vets(is_emergency);
CREATE INDEX idx_adoption_status ON adoption_listings(status);
CREATE INDEX idx_lost_found_type ON lost_found_posts(post_type);
