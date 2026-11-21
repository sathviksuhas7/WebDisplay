# WebDisplay implementation checklist

## AOP audit coverage
- [ ] `ACCOUNT_UPDATED` appears after a successful account update.
- [ ] Beneficiary add/delete continue to create one audit row each.
- [ ] Customer phone/email/address changes continue to create their existing audit rows.
- [ ] Customer creation, employee create/update, account create/activate/deactivate, document approve/reject, Helpdesk assign/reassign and review create audit rows.
- [ ] Force a validation failure for an audited operation; confirm no success audit row is created.
- [ ] Confirm deposit, withdrawal and transfer activity is recorded only in the financial transaction tables, not duplicated in `audit_logs`.
- [ ] Review `verification/audit-verification.sql`.

## Document upload security
- [ ] Start ClamAV and verify the daemon listens on configured host/port.
- [ ] Upload a genuine PDF, JPEG and PNG; each must pass after scanning.
- [ ] Rename a text file to `.pdf`; it must be rejected.
- [ ] Rename a JPEG to `.pdf`; it must be rejected.
- [ ] Upload a truncated/corrupt PDF or image; it must be rejected.
- [ ] Upload a file larger than 5 MB; it must be rejected.
- [ ] Use the EICAR antivirus test file in a permitted test environment; ClamAV must detect it and the application must reject it. Do not use a real malware sample.
- [ ] Stop ClamAV and attempt an upload; it must fail closed with a safe error.
- [ ] Repeat the checks for employee re-upload of a rejected document.

## Swagger
- [ ] Sign in and open `/swagger-ui.html`.
- [ ] Confirm controller endpoints and request/response schemas appear.
- [ ] Confirm `/v3/api-docs` returns the OpenAPI document.
- [ ] Confirm unauthenticated access remains blocked by the application session filter.
- [ ] Confirm role restrictions and CSRF validation still apply to API operations.

## n8n AI assistant
- [ ] Import the workflow JSON and configure Header Auth on the Webhook node.
- [ ] Configure the OpenAI Chat Model credential in n8n.
- [ ] Activate the workflow and configure matching backend webhook URL/shared secret.
- [ ] Ask a general portal question and confirm a useful response.
- [ ] Ask for the signed-in customer's account overview and confirm only masked account details are provided.
- [ ] Try to submit another customer's ID in the browser request; backend must ignore it because customer identity comes from the session.
- [ ] Confirm the assistant refuses to request secrets or perform transfers/profile changes/document approvals.
- [ ] Confirm the assistant shows a safe unavailable message when n8n is down or not configured.

## Dashboard usability and existing fixes
- [ ] Confirm only the first top-level section is expanded initially and other sections expand on click.
- [ ] Confirm hidden/edit forms still open and close correctly.
- [ ] Confirm all original dashboard actions and tables still work after sections are collapsed/expanded.
- [ ] Confirm Helpdesk shows the escalation timestamp for escalated tickets.
- [ ] Confirm Admin theme selection persists after restart and applies to all role dashboards.
- [ ] Confirm Admin no longer has the obsolete ID Document Verification panel.
- [ ] Confirm Edit Staff save closes the edit form and displays success feedback.
- [ ] Confirm number inputs cannot be changed by wheel/spinner but accept normal typing.

## Final regression
- [ ] Run `./mvnw clean compile` after Maven downloads the new dependencies.
- [ ] Run the original login/session/CSRF and role access flows.
- [ ] Run customer, employee, Admin, Bank Manager and Helpdesk smoke tests.
- [ ] Re-run Mend vulnerability scan after dependency updates.


## Additional usability fixes (21 Nov 2026)
- [ ] Logout completes promptly and invalidates the session.
- [ ] Failed login retains the username but never the password.
- [ ] Browser Back does not cycle through old login POST pages.
- [ ] Login works with browser DevTools open; expired form message is concise.
- [ ] Temporary-password customers can use the change-password page without heartbeat redirect loops.
- [ ] OTP verification buttons appear before resend controls.
- [ ] Phone fields accept only 10-digit Indian mobile numbers.
- [ ] Employee account creation requires Savings or Current account type.
- [ ] Customer transaction history filters All, Deposits, Withdrawals and Transfers.
- [ ] Customer can select a date range and open/download a PDF account statement.
- [ ] Previous transfer messages clear when a new transfer is attempted.
- [ ] Helpdesk agent-created on-behalf tickets are initially assigned to that agent.
- [ ] Helpdesk Manager can reassign tickets but cannot create on-behalf tickets; Bank Manager remains a separate role.
- [ ] Wrong-role page access shows a friendly Access Denied page rather than the Whitelabel error.
- [ ] Optional local ELK stack ingests multiple log files from `logs/`.
