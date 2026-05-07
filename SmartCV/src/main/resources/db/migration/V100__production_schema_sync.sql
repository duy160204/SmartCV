-- =============================================================================
-- Migration: V100__production_schema_sync.sql
-- Description: Mega-Sync migration to ensure schema consistency with JPA entities.
-- Features: Idempotent, MySQL 8/9 compatible, handles missing tables/columns/indices.
-- =============================================================================

-- -----------------------------------------------------
-- 1. Helper Procedures (Internal)
-- -----------------------------------------------------
-- We use dynamic SQL blocks to check for column existence.

-- -----------------------------------------------------
-- 2. Ensure Core Tables & Columns
-- -----------------------------------------------------

-- Table: plan_definitions
CREATE TABLE IF NOT EXISTS plan_definitions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    plan VARCHAR(50) NOT NULL,
    code VARCHAR(100) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    price DECIMAL(19, 2) NOT NULL,
    currency VARCHAR(10) DEFAULT 'VND',
    duration_months INT NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    description TEXT,
    max_share_per_month INT NOT NULL DEFAULT 5,
    public_link_expire_days INT NOT NULL DEFAULT 30,
    max_ai_requests_per_day INT NOT NULL DEFAULT 50
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table: plan_features
CREATE TABLE IF NOT EXISTS plan_features (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    plan VARCHAR(50) NOT NULL,
    feature_code VARCHAR(100) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table: templates
CREATE TABLE IF NOT EXISTS templates (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(100) UNIQUE NOT NULL,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    thumbnail_url VARCHAR(255),
    preview_content TEXT,
    full_content LONGTEXT,
    pdf_html LONGTEXT,
    pdf_css LONGTEXT,
    config_json LONGTEXT,
    plan_required VARCHAR(50) NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    version INT DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table: cv
CREATE TABLE IF NOT EXISTS cv (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    template_id BIGINT,
    template_version INT,
    template_snapshot LONGTEXT,
    content LONGTEXT,
    data_json LONGTEXT,
    status VARCHAR(50) NOT NULL,
    is_public BOOLEAN DEFAULT FALSE,
    is_locked BOOLEAN DEFAULT FALSE,
    view_count BIGINT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table: payment_transactions
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
    version BIGINT DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table: user_subscriptions
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
    version BIGINT DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table: subscription_history
CREATE TABLE IF NOT EXISTS subscription_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    old_plan VARCHAR(50),
    new_plan VARCHAR(50) NOT NULL,
    change_type VARCHAR(50) NOT NULL,
    reason VARCHAR(50) NOT NULL,
    payment_id BIGINT,
    confirmed_by_admin_id BIGINT,
    changed_at TIMESTAMP NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table: admin_subscription_requests
CREATE TABLE IF NOT EXISTS admin_subscription_requests (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    payment_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    plan VARCHAR(50) NOT NULL,
    months INT NOT NULL,
    status VARCHAR(50) NOT NULL,
    reason TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    processed_at TIMESTAMP NULL,
    processed_by BIGINT,
    version BIGINT DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table: ai_usage (Naming standard: ai_usage)
CREATE TABLE IF NOT EXISTS ai_usage (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    usage_date DATE NOT NULL,
    request_count INT NOT NULL DEFAULT 0,
    last_request_at TIMESTAMP NULL,
    UNIQUE KEY uk_user_date (user_id, usage_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table: ai_analysis_jobs
CREATE TABLE IF NOT EXISTS ai_analysis_jobs (
    job_id VARCHAR(255) PRIMARY KEY,
    user_id BIGINT NOT NULL,
    status VARCHAR(50) NOT NULL,
    result TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------
-- 3. Dynamic Column Addition (Safe Refactoring)
-- -----------------------------------------------------

-- CV: template_snapshot
SET @exists := (SELECT COUNT(*) FROM information_schema.columns WHERE table_name='cv' AND column_name='template_snapshot' AND table_schema=DATABASE());
SET @sql := IF(@exists=0, 'ALTER TABLE cv ADD COLUMN template_snapshot LONGTEXT NULL', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- Templates: pdf_html, pdf_css, version
SET @exists := (SELECT COUNT(*) FROM information_schema.columns WHERE table_name='templates' AND column_name='pdf_html' AND table_schema=DATABASE());
SET @sql := IF(@exists=0, 'ALTER TABLE templates ADD COLUMN pdf_html LONGTEXT NULL', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @exists := (SELECT COUNT(*) FROM information_schema.columns WHERE table_name='templates' AND column_name='pdf_css' AND table_schema=DATABASE());
SET @sql := IF(@exists=0, 'ALTER TABLE templates ADD COLUMN pdf_css LONGTEXT NULL', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- Users: password nullable
ALTER TABLE users MODIFY COLUMN password VARCHAR(255) NULL;

-- -----------------------------------------------------
-- 4. Constraint & Index Sync
-- -----------------------------------------------------

-- Plan Definitions: Remove unique on 'plan' if exists (V2/V10 requirement)
SET @idx := (SELECT INDEX_NAME FROM information_schema.statistics WHERE table_name='plan_definitions' AND column_name='plan' AND non_unique=0 AND index_name!='PRIMARY' AND table_schema=DATABASE() LIMIT 1);
SET @sql := IF(@idx IS NOT NULL, CONCAT('ALTER TABLE plan_definitions DROP INDEX ', @idx), 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- AI Usage: Ensure naming matches Entity
-- (Already handled in CREATE TABLE, but ensure index exists for existing DBs)
SET @exists := (SELECT COUNT(*) FROM information_schema.statistics WHERE table_name='ai_usage' AND index_name='uk_user_date' AND table_schema=DATABASE());
SET @sql := IF(@exists=0, 'CREATE UNIQUE INDEX uk_user_date ON ai_usage(user_id, usage_date)', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- -----------------------------------------------------
-- 5. Foreign Keys Sync (Safe Creation)
-- -----------------------------------------------------

-- Example: fk_cv_user
SET @exists := (SELECT COUNT(*) FROM information_schema.table_constraints WHERE constraint_name='fk_cv_user' AND table_schema=DATABASE());
SET @sql := IF(@exists=0, 'ALTER TABLE cv ADD CONSTRAINT fk_cv_user FOREIGN KEY (user_id) REFERENCES users(id)', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- -----------------------------------------------------
-- 6. Data Initialization (Minimal required)
-- -----------------------------------------------------
INSERT IGNORE INTO roles (id, name) VALUES (1, 'ROLE_USER'), (2, 'ROLE_ADMIN');
