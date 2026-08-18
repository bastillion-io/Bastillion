# Changelog

All notable changes to this project are documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/).

## [5.2.0]

### Security

- **Critical: unauthenticated admin bypass.** Route and role checks matched on the raw request URI with a substring `contains()` instead of the container-normalized servlet path. A crafted URI like `/x;/manage/viewUsers.ktrl` could reach any `/manage/*` or `/admin/*` controller — including full account creation — with zero authentication. Now matched on `getServletPath()` with an exact match.
- **SSH terminal WebSocket had no real auth gate.** The `/admin/terms.ws` upgrade request bypasses the servlet-container `AuthFilter` entirely; it was only "failing safe" by accident, via an uncaught NPE. `SecureShellWS.onOpen` now explicitly validates a live admin auth token before allowing the connection.
- **Added login throttling.** Brute-force login attempts were previously unbounded. Now rate-limited per client IP (configurable via `maxLoginAttemptsPerIP` / `loginThrottleWindowMinutes`, default 10 attempts / 5 minutes) rather than per-account, so an attacker can't lock out a known admin by deliberately failing their password.
- **Fixed a file upload path traversal.** `UploadAndPushKtrl.push()` didn't sanitize the uploaded filename before using it to build SFTP push/cleanup paths, allowing a crafted `../../../etc/passwd`-style value to read or delete arbitrary local files.
- **Hardened DB connection handling.** All 12 DAO classes moved to try-with-resources, closing two real connection leaks: one in the terminal output-polling loop (a leak every 25ms on exception) and one on every failed external-auth attempt.

### Added

- **SAML 2.0 SSO**, alongside the existing LDAP/JAAS auth — works with Entra ID, Okta, ADFS, or any SAML 2.0 IdP.
  - Self-signed SP signing/encryption keys generated automatically (no config needed); every outgoing `AuthnRequest` is signed.
  - New `/saml/metadata` endpoint for IdP-side setup via URL import.
  - Optional encrypted-assertion support (`samlWantEncryptedAssertions`).
  - Config is entirely env-var/file driven — no new admin UI, matching the LDAP pattern.
  - SAML role/group claims map onto Bastillion profiles the same way LDAP groups already do.

### Changed

- Modernized UI throughout: new logo/branding (SVG), responsive navbar, consistent button styling across admin and manage views.
- Terminal polish: fixed resizing, cursor/clipboard input, selection clearing after copy, and a duplicate-session output race.
- Fixed zsh sessions leaking `PROMPT_EOL_MARK` markers into terminal/audit output.
- Raised the free system limit.
- Refreshed all product screenshots.
- Upgraded embedded Jetty from 11 (Jakarta EE 9) to 12 (Jakarta EE 10).

### Dependencies

- `xmlsec` 2.2.6 → 4.0.4 (also closes CVE-2023-44483, pinned similarly to the existing `mina-core` pin)
- `jakarta.servlet-api` 6.0.0 → 6.1.0
- `commons-codec` 1.22.0 → 1.22.1
- `com.github.mwiede:jsch` 2.28.4 → 2.28.6
- `org.bouncycastle:bcprov-jdk18on` 1.85 → 1.85.2
- `org.junit.jupiter:junit-jupiter` 6.1.2 → 6.1.3
- `maven-clean-plugin` 3.4.1 → 3.5.0
- `frontend-maven-plugin` 2.0.1 → 2.0.2
- `grunt` 1.6.2 → 1.6.3, `brace-expansion` (npm, transitive)
- `github/codeql-action` 4 → 4.37.7

**Upgrade note:** if you run behind LDAP/JAAS today, this release is a drop-in upgrade — no config changes required. SAML is opt-in via new env vars. Given the auth-bypass and WS-terminal-auth fixes above, upgrading promptly is strongly recommended for all deployments.
