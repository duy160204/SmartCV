-- Idempotent, Flyway-safe, no procedure, no delimiter

SET @idx := (
    SELECT INDEX_NAME
    FROM INFORMATION_SCHEMA.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'plan_definitions'
      AND COLUMN_NAME = 'plan'
      AND NON_UNIQUE = 0
      AND INDEX_NAME != 'PRIMARY'
    LIMIT 1
);

SET @sql = IF(
    @idx IS NOT NULL,
    CONCAT('ALTER TABLE plan_definitions DROP INDEX ', @idx),
    'SELECT 1'
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
