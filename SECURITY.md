# Security Policy

## Scope

This covers [Bastillion](https://github.com/bastillion-io/Bastillion) itself — the SSH
console and key management application in this repo.

It does not cover [loophole.company](https://loophole.company) (the license purchase
site); report issues with that separately, in its own repo.

## Reporting a Vulnerability

If you've found a vulnerability, we would like to know so we can patch it.

Please email **support@loophole.company** with a description and steps to reproduce.
We'll acknowledge your report and let you know once it's fixed. Please don't open a public
GitHub issue for anything that isn't already public.

## In Scope

- Authentication or session bypass (login, 2FA, CSRF, session fixation)
- Anything that lets one user reach a system or key outside their assigned profile
- SSH key handling issues — key generation, storage, or distribution to the wrong host
- License forgery or a way to raise the system cap without a valid signed license
- SQL injection, command injection, XXE, SSRF, or similar
- Privilege escalation from a non-admin account to admin

## Out of Scope

- The self-signed TLS certificate warning on first run — that's expected (see the README's
  [TLS / HTTPS](README.md#tls--https) section); bring your own CA-signed cert if that's a
  problem for your deployment
- Issues that require an attacker to already have root/admin on the machine running
  Bastillion, or physical access to it
- Denial of service against a single self-hosted instance
- Automated scanner output without a working proof of concept
