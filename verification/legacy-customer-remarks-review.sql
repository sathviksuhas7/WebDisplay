-- READ-ONLY REVIEW ONLY. Do not run UPDATE/DELETE until each row is reviewed.
-- New Helpdesk messages are stored in ticket_conversation_messages; they are not
-- appended to support_tickets.customer_remarks in the current source.
-- This query helps inspect existing customer_remarks values for historical
-- Helpdesk-authored content that may have been mixed in before the separation fix.

SELECT ticket_id, customer_id, created_at, customer_remarks
FROM support_tickets
WHERE customer_remarks IS NOT NULL
  AND TRIM(customer_remarks) <> ''
ORDER BY created_at DESC;

-- Compare conversation history for a selected ticket (replace 123 with its ID).
SELECT message_id, ticket_id, sender_type, sender_name, message, created_at
FROM ticket_conversation_messages
WHERE ticket_id = 123
ORDER BY created_at ASC, message_id ASC;
