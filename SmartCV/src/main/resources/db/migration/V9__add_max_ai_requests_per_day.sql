-- Migration: Ensure max_ai_requests_per_day exists in plan_definitions
-- This is a recovery migration to ensure the column is present even if previous migrations were inconsistent.

SET @dbname = DATABASE();
SET @tablename = 'plan_definitions';
SET @columnname = 'max_ai_requests_per_day';
SET @preparedStatement = (SELECT IF(
  (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
   WHERE TABLE_SCHEMA = @dbname
     AND TABLE_NAME = @tablename
     AND COLUMN_NAME = @columnname) > 0,
  'SELECT 1',
  'ALTER TABLE plan_definitions ADD COLUMN max_ai_requests_per_day INT NOT NULL DEFAULT 50'
));
PREPARE stmt FROM @preparedStatement;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Ensure standard plan limits are set
UPDATE plan_definitions SET max_ai_requests_per_day = 50 WHERE plan = 'FREE';
UPDATE plan_definitions SET max_ai_requests_per_day = 200 WHERE plan = 'PRO';
UPDATE plan_definitions SET max_ai_requests_per_day = 1000 WHERE plan = 'PREMIUM';
