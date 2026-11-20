-- Read-only audit review queries for webdisplay_db.

-- Review latest business audit actions and counts.
SELECT action, COUNT(*) AS total_records, MAX(created_at) AS last_record
FROM audit_logs
GROUP BY action
ORDER BY last_record DESC;

-- Check account/customer/staff/helpdesk actions individually.
SELECT id, user_id, username, role, action, details, created_at
FROM audit_logs
WHERE action IN (
    'ACCOUNT_CREATED', 'ACCOUNT_UPDATED',
    'CUSTOMER_PROFILE_CREATED', 'CUSTOMER_PHONE_UPDATED',
    'CUSTOMER_EMAIL_UPDATED', 'CUSTOMER_ADDRESS_CHANGE_REQUESTED',
    'CUSTOMER_ADDRESS_CHANGE_APPROVED', 'CUSTOMER_ADDRESS_CHANGE_REJECTED',
    'BENEFICIARY_ADDED', 'BENEFICIARY_DELETED',
    'EMPLOYEE_PROFILE_CREATED', 'EMPLOYEE_PROFILE_UPDATED',
    'HELPDESK_TICKET_ASSIGNED_OR_REASSIGNED', 'HELPDESK_TICKET_REVIEWED'
)
ORDER BY created_at DESC;

-- Confirm financial transaction actions have not been added to audit_logs.
-- This is a diagnostic search, not a data mutation.
SELECT id, action, details, created_at
FROM audit_logs
WHERE UPPER(action) REGEXP 'DEPOSIT|WITHDRAW|TRANSFER|PAYMENT'
   OR UPPER(details) REGEXP 'DEPOSIT|WITHDRAW|TRANSFER|PAYMENT'
ORDER BY created_at DESC;
