# WebDisplay – complete update setup

## 1. Preserve your original project
Keep the separately supplied `WebDisplay-original-backup-2026-11-18.zip` untouched. Extract this updated project into a new folder. Do not overwrite your only working copy.

## 2. Database migration
Run `database/remaining-work-migration.sql` against `webdisplay_db` before starting the application. The project uses `spring.jpa.hibernate.ddl-auto=validate`, so the `app_settings` table must exist before startup.

## 3. Document upload security
The ID document upload and re-upload paths now validate:
- File size and allowed extension.
- Actual file signature (PDF/JPEG/PNG), independently of the supplied name.
- Extension/content-type agreement.
- PDF structure and page count using PDFBox.
- JPEG/PNG decoding using ImageIO.
- Malware scan using ClamAV's INSTREAM protocol.

The antivirus integration is fail-closed: if ClamAV is unavailable, times out, or returns an inconclusive response, the upload is rejected. For local development without ClamAV you can start the app with `DOCUMENT_SCAN_ENABLED=false` (never in production). Install ClamAV on macOS (for example, `brew install clamav`), update signatures with `freshclam`, configure `clamd` to listen on TCP `127.0.0.1:3310`, and start the daemon. Set `CLAMAV_HOST`, `CLAMAV_PORT`, and `CLAMAV_TIMEOUT_MS` if needed. Keep `DOCUMENT_SCAN_ENABLED=true` in production. A clean scan is not an absolute guarantee against every novel threat.

## 4. Swagger / OpenAPI
After starting and logging into WebDisplay, open `http://localhost:1111/swagger-ui.html`. OpenAPI JSON is at `/v3/api-docs`. Existing session, role and CSRF protections remain active. State-changing API calls need the current `X-CSRF-TOKEN`. Do not expose the documentation publicly in production without access control.

## 5. Customer chat assistant
The floating chat assistant on the customer dashboard works immediately, with no extra setup. It answers from the signed-in customer's own data (balance, transactions, beneficiaries, tickets) and guides them to the right screen. It never moves money or changes data.

Optional: to let a generative AI (via n8n) answer open-ended questions the built-in assistant does not understand, import `n8n/webdisplay-customer-ai-workflow.json`, configure its Header Auth (`X-WebDisplay-AI-Secret`) and model credential, activate it, and set `N8N_AI_WEBHOOK_URL` and `N8N_AI_SHARED_SECRET`. If n8n is down, the built-in assistant answers instead.

## 6. Dashboard layout
Each dashboard groups its sections into a few tabs (for example Customer: Overview, Send Money, Transactions & Statements, Help & Support). The chosen tab is remembered while the browser tab is open. Buttons that jump to another section open the right tab automatically.

## 7. Theme and other UI changes
The included patch adds Admin-controlled Light/Dark theme persistence, removes the obsolete Admin ID Document Verification section, improves Edit Staff save feedback, prevents number spinner/wheel changes, and displays the Helpdesk escalation timestamp.

## 8. Audit logging
Beneficiary add/delete and existing customer profile/address operations are already audited in ZIP 8. `ACCOUNT_UPDATED` was added in the patch. Deposit, withdrawal and transfer events remain in the dedicated financial transaction tables and must not be duplicated in `audit_logs`. Run the read-only scripts in `verification/` to review audit action counts and legacy remarks. Failed service calls should not create success events because the aspect uses `@AfterReturning`.

## 9. Dependencies and build
This update adds Springdoc OpenAPI and Apache PDFBox. Reload Maven dependencies in IntelliJ, then run `./mvnw clean compile`. A build/runtime success is not claimed until it has been run in your local environment with the required dependencies available.
