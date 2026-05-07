-- Migration: Add version column for optimistic locking and unique constraint for idempotency
-- Date: 2026-04-14
-- Purpose: Prevent race condition and duplicate payment records 

-- 1. Add version column to track optimistic locking in PaymentTransaction
ALTER TABLE payment_transactions 
ADD COLUMN version BIGINT DEFAULT 0 NOT NULL;

-- 2. Add UNIQUE constraint to prevent generating duplicate Admin requests for identical IPNs
ALTER TABLE admin_subscription_requests 
ADD CONSTRAINT uk_admin_req_payment_id UNIQUE (payment_id);
