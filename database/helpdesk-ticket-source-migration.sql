-- Run once against webdisplay_db before starting the updated application.
-- Existing customer-created tickets are classified as CUSTOMER.
ALTER TABLE support_tickets
    ADD COLUMN created_via VARCHAR(30) NOT NULL DEFAULT 'CUSTOMER';
