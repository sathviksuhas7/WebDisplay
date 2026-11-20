# WebDisplay – complete outstanding-work update (18 Nov 2026)

This folder is a full project based on the supplied `webdisplay copy 8.zip`, with the requested outstanding changes integrated. React is intentionally excluded and deferred.

## Included work

- Preserved the existing Spring Boot/JSP/MySQL banking application and role dashboards.
- Added `ACCOUNT_UPDATED` AOP audit logging. Existing beneficiary add/delete audits, customer phone/email/address audit events, employee create/update audits, account create/activate/deactivate audits, document approval/rejection audits, and Helpdesk assignment/review audits were retained.
- Kept deposit, withdrawal and transfer records in the financial transaction tables; they are not added as duplicate `audit_logs` events.
- Hardened ID document upload and re-upload: extension/size checks, actual signature detection, declared MIME consistency, PDF parsing with PDFBox, image decoding and dimension limits, and fail-closed ClamAV scanning.
- Added Springdoc OpenAPI / Swagger UI and API metadata, plus `API-SERVICE-INVENTORY.md` (static service/controller inventory).
- Added the customer-facing AI chat widget, authenticated Spring Boot proxy endpoint (own account overview, recent transactions, beneficiaries and ticket summaries), and importable n8n workflow with session-scoped conversation memory.
- Added progressive disclosure for long dashboard sections to reduce initial page length.
- Included Admin-controlled Light/Dark theme persistence, escalation timestamp display, Admin ID verification section removal, Edit Staff save feedback, and numeric input wheel/spinner prevention.
- Included SQL and verification scripts under `database/` and `verification/`.

## Important external setup

Read `SECURITY-AND-INTEGRATION-SETUP.md` before starting the application. The database migration must run first. ClamAV must be installed and running because upload scanning is fail-closed. n8n must be configured with a Header Auth credential and an LLM provider credential before the AI assistant can answer.

## Start/build

1. Open this project folder (the folder containing `pom.xml`) in IntelliJ IDEA.
2. Run `database/remaining-work-migration.sql` against `webdisplay_db`.
3. Configure the required environment variables described in `SECURITY-AND-INTEGRATION-SETUP.md`.
4. Reload Maven dependencies.
5. Run `./mvnw clean compile` and then start the Spring Boot application.

A Maven build could not be run in the preparation environment because Maven is not installed and the Maven wrapper distribution was not cached; therefore this package is not represented as compile-verified. The original ZIP 8 backup is separate and unchanged.
