# WebDisplay integrated build – verification report

Date: 19 November 2026
Baseline: `webdisplay copy 8.zip` (saved original project)
Feature reference: `WebDisplay-fixed-2026-11-19-v2.zip`

## Checks executed in the isolated workspace

- ZIP extraction and archive integrity: PASS.
- Original project source-file retention comparison: PASS for application source files; no original Java/JSP/CSS/JS source file was deleted by the feature integration. The only baseline-only files were macOS `.DS_Store` metadata / empty template metadata.
- `pom.xml` XML parsing: PASS.
- n8n workflow JSON parsing: PASS.
- Node syntax check for all 6 standalone JavaScript files: PASS.
- Inline JSP JavaScript: one block could be independently parsed; JSP-containing blocks were skipped because they require JSP compilation. No failure was reported by the parseable block.
- Dashboard CSRF logout form/source checks across six authenticated pages: PASS.
- Logout server-side invalidation and explicit JSESSIONID cookie expiry source checks: PASS.
- Heartbeat GET mapping and filter allow-rule source checks: PASS.
- Session guard references present in all six authenticated pages: PASS.

## Checks not executable in this environment

- Maven compile / test: NOT RUN. This environment has Java 21, while the project targets Java 17; Maven is not installed and the Maven wrapper cannot download Maven/dependencies because Maven Central is unreachable.
- Spring application startup: NOT RUN.
- Live MySQL migrations / JPA schema validation: NOT RUN.
- Browser end-to-end workflows, including real logout, session timeout, browser Back/Forward, role access, banking operations, Helpdesk, AI, theme rendering and uploads: NOT RUN.
- ClamAV upload scanning: NOT RUN; requires a running ClamAV daemon.
- n8n generative fallback: NOT RUN; requires webhook URL and shared secret.

## Important environment configuration

The application expects `DB_URL`, `DB_USERNAME`, and `DB_PASSWORD` environment variables. The new app-settings migration must be applied before startup if it has not already been applied. ClamAV scanning is enabled by default and document uploads fail closed when ClamAV is unavailable. For isolated local development only, `DOCUMENT_SCAN_ENABLED=false` disables the scanner; do not treat that as an antivirus scan.

## Scope

This report records only checks actually executed. Static/source checks are not represented as full application or browser tests. The local Java 17 + MySQL + browser verification checklist must be completed before treating this build as fully end-to-end verified.
