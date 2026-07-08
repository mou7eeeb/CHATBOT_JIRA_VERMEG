-- PostgreSQL Initialization Script for AI Jira Chatbot
-- This script creates the database and initial admin user

-- Create database (run this separately as postgres user)
-- CREATE DATABASE jirachatbot;

-- Connect to the database
-- \c jirachatbot

-- Create tables (these will be auto-created by Hibernate, but here for reference)

-- Users table
CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    first_name VARCHAR(255) NOT NULL,
    last_name VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL DEFAULT 'USER',
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    account_non_expired BOOLEAN NOT NULL DEFAULT TRUE,
    account_non_locked BOOLEAN NOT NULL DEFAULT TRUE,
    credentials_non_expired BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_login_at TIMESTAMP
);

-- Jira Connections table
CREATE TABLE IF NOT EXISTS jira_connections (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    connection_name VARCHAR(255) NOT NULL,
    jira_base_url VARCHAR(500) NOT NULL,
    jira_email VARCHAR(255) NOT NULL,
    jira_api_token VARCHAR(500) NOT NULL,
    is_default BOOLEAN NOT NULL DEFAULT FALSE,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    last_tested_at TIMESTAMP,
    last_test_success BOOLEAN,
    last_test_message TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Chat Sessions table
CREATE TABLE IF NOT EXISTS chat_sessions (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    session_name VARCHAR(255),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Chat Messages table
CREATE TABLE IF NOT EXISTS chat_messages (
    id BIGSERIAL PRIMARY KEY,
    session_id BIGINT NOT NULL,
    message_type VARCHAR(50) NOT NULL,
    content TEXT NOT NULL,
    jql_query TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (session_id) REFERENCES chat_sessions(id) ON DELETE CASCADE
);

-- Saved Searches table
CREATE TABLE IF NOT EXISTS saved_searches (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    search_name VARCHAR(255) NOT NULL,
    natural_query TEXT NOT NULL,
    jql_query TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Create indexes for better performance
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_jira_connections_user_id ON jira_connections(user_id);
CREATE INDEX IF NOT EXISTS idx_chat_sessions_user_id ON chat_sessions(user_id);
CREATE INDEX IF NOT EXISTS idx_chat_messages_session_id ON chat_messages(session_id);
CREATE INDEX IF NOT EXISTS idx_saved_searches_user_id ON saved_searches(user_id);

-- Insert default admin user
-- Password: admin123 (BCrypt encoded)
-- Note: This is for development only. Change in production!
INSERT INTO users (email, password, first_name, last_name, role, enabled, account_non_expired, account_non_locked, credentials_non_expired)
VALUES (
    'admin@jirachatbot.com',
    '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.AQubh4a',
    'Admin',
    'User',
    'ADMIN',
    TRUE,
    TRUE,
    TRUE,
    TRUE
)
ON CONFLICT (email) DO NOTHING;

-- Insert test user
-- Password: user123 (BCrypt encoded)
INSERT INTO users (email, password, first_name, last_name, role, enabled, account_non_expired, account_non_locked, credentials_non_expired)
VALUES (
    'user@jirachatbot.com',
    '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lKTKKMT8.5Ld6Ov5i',
    'Test',
    'User',
    'USER',
    TRUE,
    TRUE,
    TRUE,
    TRUE
)
ON CONFLICT (email) DO NOTHING;

-- Verification queries
-- SELECT * FROM users;
-- SELECT COUNT(*) FROM users;
