-- Reference schema. Hibernate (spring.jpa.hibernate.ddl-auto=update) will create/update
-- these tables automatically on startup. This file is here so you understand the shape
-- of the data, and as a base if you switch to Flyway/Liquibase migrations later.

CREATE DATABASE IF NOT EXISTS campus_app;
USE campus_app;

CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    register_no VARCHAR(100) UNIQUE,
    role ENUM('ADMIN', 'STAFF', 'STUDENT') NOT NULL,
    department VARCHAR(255),
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME NOT NULL
);

CREATE TABLE clubs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    description VARCHAR(1000),
    coordinator_id BIGINT NOT NULL,
    created_at DATETIME NOT NULL,
    FOREIGN KEY (coordinator_id) REFERENCES users(id)
);

CREATE TABLE club_members (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    club_id BIGINT NOT NULL,
    student_id BIGINT NOT NULL,
    joined_at DATETIME NOT NULL,
    UNIQUE KEY uq_club_student (club_id, student_id),
    FOREIGN KEY (club_id) REFERENCES clubs(id),
    FOREIGN KEY (student_id) REFERENCES users(id)
);

CREATE TABLE budget_requests (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    club_id BIGINT NOT NULL,
    requested_by BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    purpose VARCHAR(2000),
    amount DECIMAL(10,2) NOT NULL,
    status ENUM('PENDING','APPROVED','REJECTED') NOT NULL DEFAULT 'PENDING',
    reviewed_by BIGINT,
    review_comment VARCHAR(1000),
    reviewed_at DATETIME,
    created_at DATETIME NOT NULL,
    FOREIGN KEY (club_id) REFERENCES clubs(id),
    FOREIGN KEY (requested_by) REFERENCES users(id),
    FOREIGN KEY (reviewed_by) REFERENCES users(id)
);

CREATE TABLE attendance_sessions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    club_id BIGINT NOT NULL,
    created_by BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    otp VARCHAR(10) NOT NULL,
    created_at DATETIME NOT NULL,
    expires_at DATETIME NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    FOREIGN KEY (club_id) REFERENCES clubs(id),
    FOREIGN KEY (created_by) REFERENCES users(id)
);

CREATE TABLE attendance_records (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    session_id BIGINT NOT NULL,
    student_id BIGINT NOT NULL,
    marked_at DATETIME NOT NULL,
    UNIQUE KEY uq_session_student (session_id, student_id),
    FOREIGN KEY (session_id) REFERENCES attendance_sessions(id),
    FOREIGN KEY (student_id) REFERENCES users(id)
);
