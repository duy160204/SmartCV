-- Migration: Make password column nullable for OAuth users
-- Date: 2026-01-20
-- Purpose: Fix constraint mismatch - OAuth users are created without passwords

ALTER TABLE users MODIFY COLUMN password VARCHAR(255) NULL;
