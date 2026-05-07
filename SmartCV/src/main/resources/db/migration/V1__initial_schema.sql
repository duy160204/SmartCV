-- =============================================================================
-- Migration: V1__initial_schema.sql
-- Description: Consolidated Initial Schema based on JPA Entities (Source of Truth).
-- Strategy: Clean Baseline for Production Stability.
-- =============================================================================

-- 1. Roles
CREATE TABLE roles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO roles (id, name) VALUES (1, 'ROLE_USER'), (2, 'ROLE_ADMIN');

-- 2. Users
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    username VARCHAR(255),
    password VARCHAR(255),
    avatar_url VARCHAR(255),
    role_id BIGINT,
    is_verified BIT(1) NOT NULL DEFAULT 0,
    locked BIT(1) NOT NULL DEFAULT 1,
    verify_token VARCHAR(255),
    created_at DATE NOT NULL,
    updated_at DATE NOT NULL,
    CONSTRAINT fk_user_role FOREIGN KEY (role_id) REFERENCES roles(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. Plan Definitions
CREATE TABLE plan_definitions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    plan VARCHAR(255) NOT NULL,
    code VARCHAR(255) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    price DECIMAL(38, 2) NOT NULL,
    currency VARCHAR(255) NOT NULL DEFAULT 'VND',
    duration_months INT NOT NULL,
    is_active BIT(1) NOT NULL DEFAULT 1,
    description VARCHAR(255),
    max_share_per_month INT NOT NULL,
    public_link_expire_days INT NOT NULL,
    max_ai_requests_per_day INT NOT NULL DEFAULT 50
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 4. Plan Features
CREATE TABLE plan_features (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    plan VARCHAR(255) NOT NULL,
    feature_code VARCHAR(255) NOT NULL,
    enabled BIT(1) NOT NULL DEFAULT 1
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 5. Templates
CREATE TABLE templates (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(255) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    thumbnail_url VARCHAR(255),
    preview_content TEXT,
    full_content LONGTEXT,
    pdf_html LONGTEXT,
    pdf_css LONGTEXT,
    config_json LONGTEXT,
    plan_required VARCHAR(255) NOT NULL,
    is_active BIT(1) NOT NULL DEFAULT 1,
    created_at DATETIME(6),
    updated_at DATETIME(6),
    version INT DEFAULT 0,
    INDEX idx_template_active_plan (is_active, plan_required)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 6. CV
CREATE TABLE cv (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    template_id BIGINT,
    template_version INT,
    template_snapshot LONGTEXT,
    content LONGTEXT,
    data_json LONGTEXT,
    status VARCHAR(255) NOT NULL,
    is_public BIT(1) DEFAULT 0,
    is_locked BIT(1) DEFAULT 0,
    view_count BIGINT DEFAULT 0,
    created_at DATETIME(6),
    updated_at DATETIME(6),
    CONSTRAINT fk_cv_user FOREIGN KEY (user_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 7. Payment Transactions
CREATE TABLE payment_transactions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    plan VARCHAR(255) NOT NULL,
    months INT NOT NULL,
    amount BIGINT NOT NULL,
    provider VARCHAR(255) NOT NULL,
    status VARCHAR(255) NOT NULL,
    transaction_code VARCHAR(255) NOT NULL UNIQUE,
    external_id VARCHAR(255),
    paid_at DATETIME(6),
    ip_address VARCHAR(255),
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6),
    CONSTRAINT fk_payment_user FOREIGN KEY (user_id) REFERENCES users(id),
    INDEX idx_payment_user (user_id),
    INDEX idx_payment_status (status),
    INDEX idx_payment_txn_code (transaction_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 8. User Subscriptions
CREATE TABLE user_subscriptions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    plan VARCHAR(255) NOT NULL,
    status VARCHAR(255) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE,
    last_payment_id BIGINT,
    confirmed_by_admin_id BIGINT,
    confirmed_at DATETIME(6),
    version BIGINT DEFAULT 0,
    CONSTRAINT fk_sub_user FOREIGN KEY (user_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 9. Subscription History
CREATE TABLE subscription_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    old_plan VARCHAR(255),
    new_plan VARCHAR(255) NOT NULL,
    change_type VARCHAR(255) NOT NULL,
    reason VARCHAR(255) NOT NULL,
    payment_id BIGINT,
    confirmed_by_admin_id BIGINT,
    changed_at DATETIME(6) NOT NULL,
    CONSTRAINT fk_history_user FOREIGN KEY (user_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 10. Subscription Usage
CREATE TABLE subscription_usage (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    cv_id BIGINT NOT NULL,
    plan VARCHAR(255) NOT NULL,
    usage_type VARCHAR(255) NOT NULL,
    share_uuid VARCHAR(255) NOT NULL UNIQUE,
    period VARCHAR(7) NOT NULL,
    expire_at DATETIME(6) NOT NULL,
    notified_before_expire BIT(1) NOT NULL DEFAULT 0,
    created_at DATETIME(6) NOT NULL,
    INDEX idx_usage_user (user_id),
    INDEX idx_usage_cv (cv_id),
    INDEX idx_usage_expire (expire_at),
    INDEX idx_usage_period (period)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 11. Admin Subscription Requests
CREATE TABLE admin_subscription_requests (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    requested_plan VARCHAR(255) NOT NULL,
    months INT NOT NULL,
    payment_id BIGINT NOT NULL UNIQUE,
    status VARCHAR(255) NOT NULL,
    previewed_by_admin_id BIGINT,
    confirmed_by_admin_id BIGINT,
    previewed_at DATETIME(6),
    confirmed_at DATETIME(6),
    created_at DATETIME(6) NOT NULL,
    version BIGINT DEFAULT 0,
    CONSTRAINT fk_admin_req_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_admin_req_payment FOREIGN KEY (payment_id) REFERENCES payment_transactions(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 12. CV Favorites
CREATE TABLE cv_favorites (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    template_id BIGINT NOT NULL,
    created_at DATETIME(6),
    UNIQUE KEY uk_user_template (user_id, template_id),
    INDEX idx_cv_fav_user (user_id),
    INDEX idx_cv_fav_template (template_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 13. CV Shares
CREATE TABLE cv_shares (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    cv_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    share_uuid VARCHAR(100) NOT NULL UNIQUE,
    expires_at DATETIME(6),
    created_at DATETIME(6),
    INDEX idx_cv_share_uuid (share_uuid),
    INDEX idx_cv_share_cv_id (cv_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 14. Refresh Tokens
CREATE TABLE refresh_tokens (
    id VARCHAR(36) PRIMARY KEY,
    user_id BIGINT NOT NULL,
    token VARCHAR(255) NOT NULL UNIQUE,
    expires_at DATETIME(6) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    revoked BIT(1) NOT NULL DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 15. OAuth Accounts
CREATE TABLE oauth_accounts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT,
    provider VARCHAR(255) NOT NULL,
    provider_user_id VARCHAR(255) NOT NULL,
    created_at DATE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 16. AI Usage
CREATE TABLE ai_usage (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    usage_date DATE NOT NULL,
    request_count INT NOT NULL DEFAULT 0,
    last_request_at DATETIME(6),
    UNIQUE KEY uk_user_date (user_id, usage_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 17. AI Analysis Jobs
CREATE TABLE ai_analysis_jobs (
    job_id VARCHAR(255) PRIMARY KEY,
    user_id BIGINT NOT NULL,
    status VARCHAR(255) NOT NULL,
    result TEXT,
    created_at DATETIME(6),
    updated_at DATETIME(6)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
