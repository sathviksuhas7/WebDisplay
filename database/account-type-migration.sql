-- Run once against webdisplay_db before deploying the account-type update.
-- Existing accounts are classified as SAVINGS unless the bank updates them manually.
ALTER TABLE accounts
    ADD COLUMN account_type VARCHAR(20) NOT NULL DEFAULT 'SAVINGS';
