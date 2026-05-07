-- Migration: Add max_ai_requests_per_day to plan_definitions
-- Risk: Low. Small config table, no data loss expected.
-- Backward Compatibility: All existing plans will default to 50, then upgraded based on tier.

ALTER TABLE plan_definitions 
ADD COLUMN max_ai_requests_per_day INT NOT NULL DEFAULT 50;

-- Update PRO plans to a higher limit (e.g. 200)
UPDATE plan_definitions 
SET max_ai_requests_per_day = 200 
WHERE plan = 'PRO';

-- Update PREMIUM plans to a much higher limit (e.g. 1000)
UPDATE plan_definitions 
SET max_ai_requests_per_day = 1000 
WHERE plan = 'PREMIUM';
