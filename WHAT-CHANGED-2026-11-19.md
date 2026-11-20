# WebDisplay – usability, security and fixes (19 Nov 2026)

Apply this whole project over your current one (commit your current state first so you can go back).

## Before you start
1. `app_settings` table must exist (`database/remaining-work-migration.sql`) – you have already run it.
2. Document uploads are virus-scanned with ClamAV. If ClamAV is not running on your Mac, uploads are refused.
   For local testing only, start the app with the environment variable `DOCUMENT_SCAN_ENABLED=false`.
3. Run `./mvnw clean compile`, then start the app.

## What changed

### Dashboards are organised into tabs (less scrolling)
| Dashboard | Tabs |
|---|---|
| Customer | Overview · Send Money · Transactions & Statements · Help & Support |
| Employee | Customers · Open Account · Manage Accounts · Transactions |
| Helpdesk | Tickets · Customer Lookup · Investigation History · Statistics |
| Admin | Staff · Add Staff · Audit History · Settings |
| Bank Manager | ID Documents · Address Changes · Instructions |

- Only one tab's sections show at a time; the tab bar stays at the top while scrolling.
- Buttons that jump to another section (e.g. "Use" a beneficiary, "Use customer ID", "Select account", ticket history) open the right tab automatically.
- The last tab you used is remembered after a refresh.
- Helpdesk tabs for manager-only / executive-only sections appear only for that role.
- Helpdesk headings no longer have confusing numbers ("7." appeared twice).
- Files: `static/js/dashboard-nav.js`, `static/css/dashboard-nav.css`.

### Dark theme rebuilt
- Proper dark surfaces, readable text, inputs, tables, badges, messages, login and change-password cards.
- Applied before the page draws, so there is no white flash.
- Admin → Settings tab → choose theme previews instantly; Save applies it to every user.
- Files: `static/css/site-theme.css`, `static/js/site-theme.js`.

### Customer chat assistant (chatbot)
- Floating "Need help?" button opens a chat window (full screen on phones).
- Works without any setup. It can:
  - show balance, recent or latest transactions, saved beneficiaries, support tickets, a ticket's status ("status of ticket 12"), branch;
  - guide: send money, add beneficiary, download statement, update phone/email/address, change password, raise a complaint;
  - fraud / OTP questions get safety advice and an "urgent ticket" button that pre-fills the support form.
- Answers include buttons that open the right tab and field.
- Quick-reply chips, typing indicator, conversation kept until logout, "new chat" button.
- Read-only: it never transfers money or changes data. Customer identity always comes from the server session.
- Optional n8n generative AI is used only for questions the built-in assistant does not understand, with automatic fallback.
- Files: `CustomerAiAssistantController.java`, `service/assistant/CustomerAssistantEngine.java`, `static/js/assistant-widget.js`, `static/css/assistant-widget.css`.

### Login and passwords
- Eye button inside every password field (login, change password, admin create staff).
- Clear messages: "logged out", "session ended", login errors – no empty red bar.

### Session security
- **"Invalid session" with DevTools open – fixed.** The login page created a new security token every time it was loaded; DevTools (or a second tab) loads it again, so the form's token went stale. The token is now reused until login.
- Already signed in and you open `/login`? You go straight to your dashboard.
- **Back button after logout – fixed.** Logout tells the browser to clear cached pages (`Clear-Site-Data`), every signed-in page is `no-store`, and each dashboard re-checks the session when shown from the back/forward cache or when the tab becomes active again. No session → login page, before anything is shown.
- Any API call that finds the session expired sends the user to the login page ("Your session has ended") instead of an error inside the page.
- `/api/session/heartbeat` was blocked by the filter; it is now allowed for signed-in users and accepts GET (no CSRF token needed) as well as POST (CSRF token required).
- Legacy pages `Account.jsp` and `welcome.jsp` (not opened by any controller) now send the CSRF token on their POST/PUT calls, and the heartbeat there uses GET.
- Swagger / API docs are now Admin-only.

### Helpdesk: raise a ticket for a customer who calls or visits
- Customer Lookup tab → search → View Details → "Raise a ticket for this customer".
- Choose how they contacted us (phone call / branch visit / email), category, priority, subject and issue.
- The ticket belongs to the customer (they see it in their dashboard) and the conversation records "Ticket raised on behalf of the customer by <agent> (Phone call)".
- Audit log entry `HELPDESK_TICKET_RAISED_FOR_CUSTOMER` with ticket and customer IDs.

### Other fixes
- Admin "Staff details updated successfully" message now shows.
- JPEG uploads with extra data after the end marker are accepted; damaged/half images are rejected.
- Address-proof uploads now get the same checks + antivirus scan as ID documents (checked before the OTP is used up).
- Validation errors return HTTP 400 with the message (were 500); 401/403 from controllers keep their status.

### Removed
- `dashboard-sections.js/.css` (the "everything is a dropdown" layout).
- The old in-page AI section on the customer dashboard.

### Additional logout hardening in this integrated build
- Logout always invalidates the server-side session, even if writing the audit record throws an exception.
- The response expires the `JSESSIONID` cookie using the application's context path.
- All authenticated dashboard pages now run a shared session guard on initial load, browser Back/Forward restoration, tab re-focus, and every 60 seconds while visible. A 401/403 or inactive heartbeat redirects to the login page. Temporary network failures do not force a logout.
