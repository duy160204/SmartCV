ALTER TABLE admin_subscription_requests
ADD COLUMN version BIGINT NOT NULL DEFAULT 0;

ALTER TABLE user_subscriptions
ADD COLUMN version BIGINT NOT NULL DEFAULT 0;
