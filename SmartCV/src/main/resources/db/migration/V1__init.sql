-- =============================================================================
-- Migration: V1__init.sql
-- Description: Consolidated Base schema for SmartCV (MySQL 8 compatible).
-- =============================================================================

-- 1. Roles
CREATE TABLE IF NOT EXISTS roles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT IGNORE INTO roles (id, name) VALUES (1, 'ROLE_USER'), (2, 'ROLE_ADMIN');

-- 2. Users
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(100) UNIQUE NOT NULL,
    username VARCHAR(100),
    password VARCHAR(255) NULL,
    avatar_url VARCHAR(255),
    role_id BIGINT,
    is_verified BOOLEAN DEFAULT FALSE,
    locked BOOLEAN DEFAULT TRUE,
    verify_token VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_user_role FOREIGN KEY (role_id) REFERENCES roles(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. Plan Definitions
CREATE TABLE IF NOT EXISTS plan_definitions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    plan VARCHAR(50) NOT NULL, -- Unique constraint removed as per V2/V10 requirement
    code VARCHAR(100) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    price DECIMAL(19, 2) NOT NULL,
    currency VARCHAR(10) DEFAULT 'VND',
    duration_months INT NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    description TEXT,
    max_share_per_month INT NOT NULL,
    public_link_expire_days INT NOT NULL,
    max_ai_requests_per_day INT NOT NULL DEFAULT 50,
    INDEX idx_plan_isActive (plan, is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 4. Plan Features
CREATE TABLE IF NOT EXISTS plan_features (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    plan VARCHAR(50) NOT NULL,
    feature_code VARCHAR(100) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 5. Templates
CREATE TABLE IF NOT EXISTS templates (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(100) UNIQUE NOT NULL,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    thumbnail_url VARCHAR(255),
    preview_content TEXT,
    full_content LONGTEXT,
    pdf_html LONGTEXT COMMENT 'Table-based HTML for PDF export',
    pdf_css LONGTEXT COMMENT 'A4 CSS for PDF export',
    config_json LONGTEXT,
    plan_required VARCHAR(50) NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    version INT DEFAULT 0,
    INDEX idx_template_active_plan (is_active, plan_required)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 6. CV
CREATE TABLE IF NOT EXISTS cv (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    template_id BIGINT,
    template_version INT,
    content LONGTEXT,
    data_json LONGTEXT,
    status VARCHAR(50) NOT NULL,
    is_public BOOLEAN DEFAULT FALSE,
    is_locked BOOLEAN DEFAULT FALSE,
    view_count BIGINT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    version BIGINT DEFAULT 0,
    CONSTRAINT fk_cv_user FOREIGN KEY (user_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 7. Payment Transactions
CREATE TABLE IF NOT EXISTS payment_transactions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    plan VARCHAR(50) NOT NULL,
    months INT NOT NULL,
    amount BIGINT NOT NULL,
    provider VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL,
    transaction_code VARCHAR(100) UNIQUE NOT NULL,
    external_id VARCHAR(255),
    paid_at TIMESTAMP NULL,
    ip_address VARCHAR(45),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NULL ON UPDATE CURRENT_TIMESTAMP,
    version BIGINT DEFAULT 0,
    CONSTRAINT fk_payment_user FOREIGN KEY (user_id) REFERENCES users(id),
    INDEX idx_payment_user (user_id),
    INDEX idx_payment_status (status),
    INDEX idx_payment_txn_code (transaction_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 8. User Subscriptions
CREATE TABLE IF NOT EXISTS user_subscriptions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    plan VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE,
    last_payment_id BIGINT,
    confirmed_by_admin_id BIGINT,
    confirmed_at TIMESTAMP NULL,
    version BIGINT DEFAULT 0,
    CONSTRAINT fk_sub_user FOREIGN KEY (user_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 9. Subscription History
CREATE TABLE IF NOT EXISTS subscription_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    old_plan VARCHAR(50),
    new_plan VARCHAR(50) NOT NULL,
    change_type VARCHAR(50) NOT NULL,
    reason VARCHAR(50) NOT NULL,
    payment_id BIGINT UNIQUE, -- Unique for idempotency (V6)
    confirmed_by_admin_id BIGINT,
    changed_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_history_user FOREIGN KEY (user_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 10. Subscription Usage
CREATE TABLE IF NOT EXISTS subscription_usage (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    cv_id BIGINT NOT NULL,
    plan VARCHAR(50) NOT NULL,
    usage_type VARCHAR(50) NOT NULL,
    share_uuid VARCHAR(255) UNIQUE NOT NULL,
    period VARCHAR(7) NOT NULL,
    expire_at TIMESTAMP NOT NULL,
    notified_before_expire BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_usage_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_usage_cv FOREIGN KEY (cv_id) REFERENCES cv(id),
    INDEX idx_usage_user (user_id),
    INDEX idx_usage_cv (cv_id),
    INDEX idx_usage_expire (expire_at),
    INDEX idx_usage_period (period)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 11. Admin Subscription Requests
CREATE TABLE IF NOT EXISTS admin_subscription_requests (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    payment_id BIGINT NOT NULL UNIQUE, -- Unique for idempotency (V4)
    user_id BIGINT NOT NULL,
    plan VARCHAR(50) NOT NULL,
    months INT NOT NULL,
    status VARCHAR(50) NOT NULL,
    reason TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    processed_at TIMESTAMP NULL,
    processed_by BIGINT,
    version BIGINT DEFAULT 0,
    CONSTRAINT fk_admin_req_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_admin_req_payment FOREIGN KEY (payment_id) REFERENCES payment_transactions(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 12. CV Favorites
CREATE TABLE IF NOT EXISTS cv_favorites (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    cv_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_cv (user_id, cv_id),
    CONSTRAINT fk_fav_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_fav_cv FOREIGN KEY (cv_id) REFERENCES cv(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 13. CV Shares
CREATE TABLE IF NOT EXISTS cv_shares (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    cv_id BIGINT NOT NULL,
    share_token VARCHAR(255) UNIQUE NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    expire_at TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_share_cv FOREIGN KEY (cv_id) REFERENCES cv(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 14. Refresh Tokens
CREATE TABLE IF NOT EXISTS refresh_tokens (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    token VARCHAR(255) UNIQUE NOT NULL,
    expiry_date TIMESTAMP NOT NULL,
    CONSTRAINT fk_refresh_user FOREIGN KEY (user_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 15. OAuth Accounts
CREATE TABLE IF NOT EXISTS oauth_accounts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    provider VARCHAR(50) NOT NULL,
    provider_id VARCHAR(255) NOT NULL,
    email VARCHAR(100),
    UNIQUE KEY uk_provider_id (provider, provider_id),
    CONSTRAINT fk_oauth_user FOREIGN KEY (user_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 16. AI Usages
CREATE TABLE IF NOT EXISTS ai_usages (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    request_type VARCHAR(50) NOT NULL,
    model VARCHAR(50),
    tokens_used INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_ai_usage_user FOREIGN KEY (user_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 17. AI Analysis Jobs
CREATE TABLE IF NOT EXISTS ai_analysis_jobs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    cv_id BIGINT NOT NULL,
    status VARCHAR(50) NOT NULL,
    result_json LONGTEXT,
    error_message TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_ai_job_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_ai_job_cv FOREIGN KEY (cv_id) REFERENCES cv(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
