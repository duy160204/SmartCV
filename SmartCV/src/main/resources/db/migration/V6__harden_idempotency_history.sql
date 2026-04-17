-- Migration: Add unique constraint to payment_id in subscription_history for DB-level idempotency
-- Date: 2026-04-17

ALTER TABLE subscription_history
ADD CONSTRAINT uk_history_payment_id UNIQUE (payment_id);
