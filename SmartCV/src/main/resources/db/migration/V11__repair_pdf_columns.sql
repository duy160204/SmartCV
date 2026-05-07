-- =============================================================================
-- Migration: V11__repair_pdf_columns.sql
-- Description: Safely add missing columns for MySQL 8 (Render/Railway compatibility)
-- =============================================================================

-- 1. Add pdf_html to templates
SET @exists_pdf_html := (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'templates'
      AND COLUMN_NAME = 'pdf_html'
);

SET @sql_pdf_html := IF(@exists_pdf_html = 0,
    'ALTER TABLE templates ADD COLUMN pdf_html LONGTEXT NULL COMMENT "Table-based HTML for PDF export"',
    'SELECT 1');

PREPARE stmt1 FROM @sql_pdf_html;
EXECUTE stmt1;
DEALLOCATE PREPARE stmt1;

-- 2. Add pdf_css to templates
SET @exists_pdf_css := (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'templates'
      AND COLUMN_NAME = 'pdf_css'
);

SET @sql_pdf_css := IF(@exists_pdf_css = 0,
    'ALTER TABLE templates ADD COLUMN pdf_css LONGTEXT NULL COMMENT "A4 CSS for PDF export"',
    'SELECT 1');

PREPARE stmt2 FROM @sql_pdf_css;
EXECUTE stmt2;
DEALLOCATE PREPARE stmt2;

-- 3. Add max_ai_requests_per_day to plan_definitions (if missing)
SET @exists_ai_quota := (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'plan_definitions'
      AND COLUMN_NAME = 'max_ai_requests_per_day'
);

SET @sql_ai_quota := IF(@exists_ai_quota = 0,
    'ALTER TABLE plan_definitions ADD COLUMN max_ai_requests_per_day INT NOT NULL DEFAULT 50',
    'SELECT 1');

PREPARE stmt3 FROM @sql_ai_quota;
EXECUTE stmt3;
DEALLOCATE PREPARE stmt3;

-- 4. Add version columns for optimistic locking (if missing)
-- Templates
SET @exists_tmpl_version := (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'templates'
      AND COLUMN_NAME = 'version'
);
SET @sql_tmpl_version := IF(@exists_tmpl_version = 0,
    'ALTER TABLE templates ADD COLUMN version INT DEFAULT 0',
    'SELECT 1');
PREPARE stmt4 FROM @sql_tmpl_version;
EXECUTE stmt4;
DEALLOCATE PREPARE stmt4;

-- Admin Subscription Requests
SET @exists_admin_version := (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'admin_subscription_requests'
      AND COLUMN_NAME = 'version'
);
SET @sql_admin_version := IF(@exists_admin_version = 0,
    'ALTER TABLE admin_subscription_requests ADD COLUMN version BIGINT NOT NULL DEFAULT 0',
    'SELECT 1');
PREPARE stmt5 FROM @sql_admin_version;
EXECUTE stmt5;
DEALLOCATE PREPARE stmt5;

-- User Subscriptions
SET @exists_sub_version := (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'user_subscriptions'
      AND COLUMN_NAME = 'version'
);
SET @sql_sub_version := IF(@exists_sub_version = 0,
    'ALTER TABLE user_subscriptions ADD COLUMN version BIGINT NOT NULL DEFAULT 0',
    'SELECT 1');
PREPARE stmt6 FROM @sql_sub_version;
EXECUTE stmt6;
DEALLOCATE PREPARE stmt6;

-- CV
SET @exists_cv_version := (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'cv'
      AND COLUMN_NAME = 'version'
);
SET @sql_cv_version := IF(@exists_cv_version = 0,
    'ALTER TABLE cv ADD COLUMN version BIGINT DEFAULT 0',
    'SELECT 1');
PREPARE stmt7 FROM @sql_cv_version;
EXECUTE stmt7;
DEALLOCATE PREPARE stmt7;
