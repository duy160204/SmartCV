-- Migration to remove incorrect UNIQUE constraint on plan_definitions.plan column

-- 1. Drop the specific unique index identified by the error key
-- Key Name provided: UKsslj74ry860cb627ioihx9g9m
-- This key corresponds to the 'plan' column (PlanType)

ALTER TABLE plan_definitions DROP INDEX UKsslj74ry860cb627ioihx9g9m;

-- 2. Verify: Ensure 'code' column still has a unique constraint
-- (This is usually handled by a separate index, typically UK_plan_definitions_code or similar)
-- If it's missing, we would add it back:
-- ALTER TABLE plan_definitions ADD CONSTRAINT UK_plan_code UNIQUE (code);
