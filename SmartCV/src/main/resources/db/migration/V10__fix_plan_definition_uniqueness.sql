-- Migration: V10__fix_plan_definition_uniqueness.sql
-- Description: Handle multiple plan definitions for the same PlanType by ensuring code uniqueness and adding lookup indices.

-- 1. Xóa duplicate plan_definitions nếu trùng plan + code (giữ lại bản ghi mới nhất)
DELETE FROM plan_definitions 
WHERE id NOT IN (
    SELECT id FROM (
        SELECT MAX(id) as id FROM plan_definitions GROUP BY plan, code
    ) tmp
);

-- 2. Thêm constraint UNIQUE(code)
ALTER TABLE plan_definitions MODIFY COLUMN code VARCHAR(100) NOT NULL;

SET @dbname = DATABASE();
SET @tablename = 'plan_definitions';
SET @indexname = 'uk_plan_def_code';
SET @preparedStatement = (SELECT IF(
  (SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS
   WHERE TABLE_SCHEMA = @dbname
     AND TABLE_NAME = @tablename
     AND INDEX_NAME = @indexname) > 0,
  'SELECT 1',
  'CREATE UNIQUE INDEX uk_plan_def_code ON plan_definitions(code)'
));
PREPARE stmt FROM @preparedStatement;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 3. Thêm index (plan, is_active) tối ưu cho safe lookup
SET @indexname = 'idx_plan_isActive';
SET @preparedStatement = (SELECT IF(
  (SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS
   WHERE TABLE_SCHEMA = @dbname
     AND TABLE_NAME = @tablename
     AND INDEX_NAME = @indexname) > 0,
  'SELECT 1',
  'CREATE INDEX idx_plan_isActive ON plan_definitions(plan, is_active)'
));
PREPARE stmt FROM @preparedStatement;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

