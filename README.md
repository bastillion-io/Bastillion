![Build](https://github.com/bastillion-io/Bastillion/actions/workflows/github-build.yml/badge.svg)
![CodeQL](https://github.com/bastillion-io/Bastillion/actions/workflows/codeql-analysis.yml/badge.svg)
![License](https://img.shields.io/badge/license-Prosperity%203.0.0-blue)
![Java](https://img.shields.io/badge/Java-21-orange)

<p align="center">
  <img src="src/main/webapp/img/bastillion_40x40.png" alt="Bastillion">
</p>

<h1 align="center">Bastillion</h1>
<p align="center"><strong>A modern, web-based SSH console and SSH key management tool.</strong></p>

Bastillion gives you a clean, browser-based way to manage SSH access across all your
systems — like a bastion host with a friendly dashboard. It does two things:

1. **SSH key management** — Bastillion holds its own SSH keypair and pushes/rotates public
   keys across the hosts you register, so individual users never need to hold or manage
   long-lived keys to those systems themselves.
2. **Web-based SSH terminal** — once a host is registered, authorized users can open one or
   more live terminal sessions to it directly from the browser, with commands optionally
   broadcast across every open session at once (think tmux's synchronized panes, but for a
   fleet of remote hosts instead of local panes).

- Log in with **2-factor authentication** (Authy or Google Authenticator)
- Manage and distribute **SSH public keys**, and disable/rotate them centrally
- Launch secure multi-session web shells and **share commands** across sessions
- Group systems into **Profiles** and control exactly who can reach what
- Save and re-run **Composite Scripts** across a whole fleet at once
- Stack **TLS/SSL over SSH** for extra protection

![Multiple terminals broadcasting the same command to three hosts at once](docs/screenshots/web-terminal.png)
<p align="center"><sub>Three real, independent SSH sessions — one command, typed once, run everywhere.</sub></p>

---

## Contents

- [How It Works](#how-it-works)
- [What's New](#-whats-new)
- [Licensing](#licensing)
- [Installation Options](#installation-options)
- [Prerequisites](#prerequisites)
- [Download and Run](#download-and-run)
- [Build from Source](#build-from-source)
- [TLS / HTTPS](#tls--https)
- [Configuration](#configuration)
- [More Screenshots](#more-screenshots)
- [License](#license)

---

## How It Works

Bastillion sits between your users and the systems they need to reach, acting as a trusted
third party rather than a simple password vault. Here's the whole lifecycle, end to end.

### 1. Bastillion generates its own SSH keypair

On first startup, before anything else, Bastillion generates an Ed25519 keypair for
itself — this is the *one* key that ever gets pushed to your hosts. It's shown in the
console output and always visible under **Settings**.

### 2. Register a system

An admin adds a host under **Manage → Systems** (user, host, port, and the path to that
host's `authorized_keys` file). Bastillion authenticates **once** with a password or
passphrase you supply, then pushes its own public key into that host's `authorized_keys`.
From then on it connects using that key — no stored passwords, ever. Status flips to
**Success** the moment the key is in place.

![Manage Systems — three hosts registered, all showing Success status](docs/screenshots/manage-systems.png)

### 3. Group systems into Profiles, assign Users

Systems get grouped into named **Profiles** — think "Production," "Staging," "Database
Tier." Users are then linked to profiles under **Manage → Users**, which is the only thing
that controls who can reach what. Revoke a profile assignment and that access is gone
immediately, no key rotation needed.

![Assigning three systems to a Production profile](docs/screenshots/assign-systems.png)

### 4. Open terminals — and broadcast to all of them at once

Assigned users open **Secure Shell → Terminals**, pick one or more systems, and get live,
resizable, xterm-based terminals in the browser, side by side. Type once, and it goes to
every terminal marked active — the same keystroke, the same command, the same output shape,
across as many hosts as you selected.

![A health-check command broadcast to three terminals simultaneously, same output shape across all three](docs/screenshots/web-terminal.png)

### 5. Rotate or revoke keys centrally

Because every host trusts the *same* application key (not one key per user), disabling it
once under **Manage SSH Keys** revokes access everywhere immediately — no need to touch
target systems by hand, no hunting down which server has which stale key.

![Every SSH key in use, with a one-click Disable per key](docs/screenshots/manage-ssh-keys.png)

---

## 🚀 What's New
- **Licensing** — free at up to 5 systems, paid tiers available at [loophole.company/pricing.html](https://loophole.company/pricing.html) (see [Licensing](#licensing) below)
- Runs as a **self-contained jar** (`java -jar`) with HTTPS out of the box — see [Download and Run](#download-and-run)
- Upgraded to **Java 21** and **Jakarta EE 11**
- Full support for **Ed25519** (default) and **Ed448** SSH keys
- **v4 → v5 migration tool** to bring over users, systems, keys, and audit logs from an existing instance — see [`tools/migrate`](tools/migrate/README.md)
- Hardened with a **CSRF filter** and app-wide **security headers**

---

## Licensing

Bastillion runs unlicensed at up to **5 registered systems** — enough to try it for real
before buying. A license raises that cap.

1. Buy a license at **[loophole.company/pricing.html](https://loophole.company/pricing.html)**
   (Starter/Team/Business — priced by system count). Payment redirects back and downloads a
   `.lic` file automatically.
2. Open the `.lic` file and copy its contents (one line).
3. Set it via the `LICENSE_KEY` environment variable:
   ```bash
   export LICENSE_KEY=<paste license file contents here>
   ```
   or paste it into `licenseKey` in `BastillionConfig.properties` instead — the environment
   variable takes precedence if both are set.
4. Restart Bastillion. **Settings** shows the licensee, system cap, and expiry, with a
   warning starting 90 days before it expires.

Licenses are annual and don't auto-renew — no card kept on file. Buy again from the same
pricing page when you get the expiry warning.

---

## Installation Options
**Free:** https://github.com/bastillion-io/Bastillion/releases

---

## Prerequisites

### Java 21 (OpenJDK)
```bash
apt-get install openjdk-21-jdk
```

### Authenticator (for 2FA)

| Application | Android | iOS |
|--------------|----------|-----|
| **Authy** | [Google Play](https://play.google.com/store/apps/details?id=com.authy.authy) | [iTunes](https://itunes.apple.com/us/app/authy/id494168017) |
| **Google Authenticator** | [Google Play](https://play.google.com/store/apps/details?id=com.google.android.apps.authenticator2) | [iTunes](https://itunes.apple.com/us/app/google-authenticator/id388497605) |

---

## Download and Run

Download the latest jar from [Releases](https://github.com/bastillion-io/Bastillion/releases):
```bash
java -jar bastillion-<version>.jar
```

Access in browser: `https://<server-ip>:8443` — see [TLS / HTTPS](#tls--https) below for the
self-signed certificate Bastillion generates on first run.

Default credentials:
```
username: admin
password: changeme
```

Runs in the foreground; stop with Ctrl+C. For background/daemon operation use whatever your
platform normally uses for a long-running Java process — `nohup java -jar ... &`, a systemd
unit, a container, etc.

---

## Build from Source

Install Maven 3+:
```bash
apt-get install maven
```

Build and run (packages a self-contained jar with an embedded Jetty server — see
`io.bastillion.Main` — and runs it):
```bash
mvn package
java -jar target/bastillion-5.0.0-SNAPSHOT.jar
```

Or for local dev without repackaging on every change:

```bash
mvn compile exec:java
```

Listens on `https://localhost:8443` by default, same as the downloaded release above — see
[TLS / HTTPS](#tls--https) below for how that certificate gets set up and how to use your
own instead.

> ⚠️ `mvn clean` will remove the H2 database and user data.

---

<details>
<summary><strong>Migrating from v4</strong></summary>

Upgrading from an old Bastillion v4 install and want to keep your users, systems, profiles,
scripts, and (most importantly) the application's existing SSH keypair instead of starting
over? `tools/migrate/` has a standalone migration tool for exactly that — it exports every
table from the old H2 database (decrypting the app-level-encrypted columns with the OLD
instance's keystore) to a JSON file, then imports it into a fresh v5 instance (re-encrypting
with the NEW instance's keystore). Existing users can log in with their current passwords
immediately after — no forced resets.

```bash
cd tools/migrate

# 1. Export the old database
./migrate.sh export /opt/Bastillion-jetty/jetty/bastillion/WEB-INF/classes/ ~/bastillion-export.json

# 2. Start the new v5 instance once against the config dir you're migrating into, then
#    stop it (Ctrl+C) once it's finished booting - this creates the schema, jceks, and
#    default admin user.
cd ../..
java -DCONFIG_DIR=/data/bastillion/ -jar target/bastillion-5.0.0-SNAPSHOT.jar

# 3. Import into the new database (full replace of all 12 tables)
cd tools/migrate
./migrate.sh import /data/bastillion/ ~/bastillion-export.json --yes-replace-all-data

# 4. Delete the export file - it contains decrypted secrets
rm ~/bastillion-export.json
```

See **[tools/migrate/README.md](tools/migrate/README.md)** for the full details — finding
your old install's config directory, what exactly gets migrated, and the security notes on
the plaintext export file.
</details>

---

## TLS / HTTPS

Bastillion generates its own self-signed certificate on first startup and serves HTTPS —
nothing to configure. Browsers will show a warning once (it's self-signed, not issued by a
CA); click through it, same as you would for any other self-hosted appliance. The
certificate and its password persist across restarts (`keystore/bastillion.p12` next to
where you run it, password stored the same encrypted way as the database password).

**Use your own certificate** (e.g. one from Let's Encrypt/certbot) by converting it to
PKCS12 and pointing Bastillion at it:
```bash
openssl pkcs12 -export -in fullchain.pem -inkey privkey.pem \
  -out bastillion.p12 -name bastillion -passout pass:changeit

export KEYSTORE_PATH=/path/to/bastillion.p12
export KEYSTORE_PASSWORD=changeit
```

**Behind a reverse proxy or load balancer that already terminates TLS** (nginx, Cloud
Run, etc.) — disable Bastillion's own HTTPS and let it serve plain HTTP instead:
```bash
export TLS_ENABLED=false
```
Defaults to port 8080 in this mode; set `PORT` to change it.

---

## Configuration

Every setting below can be set as an **environment variable** — take the property name and
insert an underscore before each capital letter, then uppercase it: `licenseKey` →
`LICENSE_KEY`, `dbUser` → `DB_USER`, `sshKeyType` → `SSH_KEY_TYPE`. This is the recommended
way to configure Bastillion, especially in containers — no file to mount or bake in.

`BastillionConfig.properties` still works as a fallback (env vars always win if both are
set), and is where any value Bastillion generates for you at first startup — like a random
DB password — gets persisted. See `src/main/resources/BastillionConfig.properties` for the
full list of settings and their defaults.

**Consolidating everything under one directory** (e.g. a single Docker volume mount):
Bastillion's state defaults to three independent sibling paths relative to the working
directory — `keystore/bastillion.p12`, `keydb/bastillion` (the H2 database), and
`BastillionConfig.properties` itself (via `CONFIG_DIR`) — rather than one shared root. Point
them all at the same parent to keep everything in one place:
```bash
export CONFIG_DIR=/data/bastillion/
export KEYSTORE_PATH=/data/bastillion/keystore/bastillion.p12
export DB_CONNECTION_URL=jdbc:h2:file:/data/bastillion/keydb/bastillion;CIPHER=AES;
```

<details>
<summary><strong>SSH Key Management</strong></summary>

```bash
# Disable key management (append instead of overwrite)
export KEY_MANAGEMENT_ENABLED=false

# authorized_keys refresh interval in minutes (no refresh for <=0)
export AUTH_KEYS_REFRESH_INTERVAL=120

# Force user key generation and strong passphrases
export FORCE_USER_KEY_GENERATION=false
```
</details>

<details>
<summary><strong>Custom SSH Key Pair</strong></summary>

By default Bastillion generates its own Ed25519 keypair on first startup. To use your own
instead, the easiest way is through the UI: **Settings → Replace Application SSH Key**
(manager accounts only) — paste in a private key, public key, and passphrase if it has one,
and it takes effect immediately, no restart needed.

⚠️ This replaces the *one* key every registered system trusts. It's meant as a one-time step
when first setting up Bastillion, **before** you've registered any systems — if you already
have systems registered, Bastillion loses SSH access to all of them the instant you replace
the key, unless this exact key is already sitting in `authorized_keys` on every one of them
first. The Settings page requires an extra confirmation checkbox once you have systems
registered, precisely because of this.

**Already have systems registered and need to rotate the key anyway?** Pre-stage the new key
through Bastillion itself rather than editing `authorized_keys` by hand everywhere:

1. Set `FORCE_USER_KEY_GENERATION=false` so **Manage SSH Keys → Add SSH Key** lets you paste
   an existing public key instead of only generating a new one.
2. Add the new key there against a profile covering all your systems, and confirm (under
   Manage SSH Keys, or each system's status) that it's actually landed everywhere — keep
   `AUTH_KEYS_REFRESH_INTERVAL` in mind, since that's what pushes it out.
3. Only once you're sure it's on every system, replace the application key in Settings.
4. Set `FORCE_USER_KEY_GENERATION` back to its previous value, then once you've confirmed
   the new application key has propagated to every system (again, mind
   `AUTH_KEYS_REFRESH_INTERVAL`), remove the key you added in step 2 from Manage SSH Keys —
   it was only staged there to pre-seed `authorized_keys` and isn't needed going forward.

For scripted/headless setups, the same thing can be done via environment variables and a
restart instead:

```bash
# Regenerate and import SSH keys
export RESET_APPLICATION_SSH_KEY=true

# Private key
export PRIVATE_KEY=/Users/you/.ssh/id_rsa

# Public key
export PUBLIC_KEY=/Users/you/.ssh/id_rsa.pub

# Passphrase (leave blank if none)
export DEFAULT_SSH_PASSPHRASE=myPa$$w0rd
```

Once registered, you can drop these — the key pair is already stored in the database.

`SSH_KEY_TYPE` (`rsa`, `ecdsa`, `ed25519`, or `ed448`) only matters when Bastillion is
*generating* a fresh key, not when importing one — the type of an imported key is read from
the key itself:
```bash
# SSH key type ('rsa', 'ecdsa', 'ed25519', or 'ed448')
# Supported options:
#   rsa    - Classic, widely compatible (configurable length, default 4096)
#   ecdsa  - Faster, smaller keys (P-256/384/521 curves)
#   ed25519 - Default and recommended (≈ RSA-4096, secure and fast)
#   ed448  - Extra-strong (≈ RSA-8192, slower and less supported)
export SSH_KEY_TYPE=ed25519
```
</details>

<details>
<summary><strong>Database Settings</strong></summary>

Embedded H2 example:
```bash
export DB_USER=bastillion
export DB_PASSWORD=p@$$w0rd!!
export DB_DRIVER=org.h2.Driver
export DB_CONNECTION_URL=jdbc:h2:file:keydb/bastillion;CIPHER=AES;
```

Remote H2 example:
```bash
export DB_CONNECTION_URL=jdbc:h2:tcp://<host>:<port>/~/bastillion;CIPHER=AES;
```
</details>

<details>
<summary><strong>External Authentication (LDAP)</strong></summary>

Enable external auth:
```bash
export JAAS_MODULE=ldap-ol
```

Configure `jaas.conf`:
```
ldap-ol {
    com.sun.security.auth.module.LdapLoginModule SUFFICIENT
    userProvider="ldap://hostname:389/ou=example,dc=bastillion,dc=com"
    userFilter="(&(uid={USERNAME})(objectClass=inetOrgPerson))"
    authzIdentity="{cn}"
    useSSL=false
    debug=false;
};
```

To map LDAP roles to Bastillion profiles:
```
ldap-ol-with-roles {
    org.eclipse.jetty.jaas.spi.LdapLoginModule required
    debug="false"
    useLdaps="false"
    contextFactory="com.sun.jndi.ldap.LdapCtxFactory"
    hostname="<SERVER>"
    port="389"
    bindDn="<BIND-DN>"
    bindPassword="<BIND-DN PASSWORD>"
    authenticationMethod="simple"
    forceBindingLogin="true"
    userBaseDn="ou=users,dc=bastillion,dc=com"
    userRdnAttribute="uid"
    userIdAttribute="uid"
    userPasswordAttribute="userPassword"
    userObjectClass="inetOrgPerson"
    roleBaseDn="ou=groups,dc=bastillion,dc=com"
    roleNameAttribute="cn"
    roleMemberAttribute="member"
    roleObjectClass="groupOfNames";
};
```

Admins are added upon first login and can be assigned system profiles.
Users are synced with profiles when their LDAP role names match Bastillion profiles.
</details>

<details>
<summary><strong>Auditing</strong></summary>

Auditing is disabled by default.

Enable it in **log4j2.xml** by uncommenting:
- `io.bastillion.manage.util.SystemAudit`
- `audit-appender`

> https://github.com/bastillion-io/Bastillion/blob/master/src/main/resources/log4j2.xml#L19-L22

Also enable it:
```bash
export ENABLE_INTERNAL_AUDIT=true
```
</details>

---

## More Screenshots

Login, 2FA enrollment, the main menu, and a few other screens not already shown in
[How It Works](#how-it-works) above.

<table>
<tr>
<td width="50%">

**Login** — username/password, with an optional OTP access code field for 2FA.

![Login](docs/screenshots/login.png)

</td>
<td width="50%">

**Two-Factor Setup** — scan the QR code with Authy or Google Authenticator.

![Two-Factor Setup](docs/screenshots/two-factor-setup.png)

</td>
</tr>
<tr>
<td width="50%">

**Main Menu** — scoped to what the logged-in user is allowed to see.

![Main Menu](docs/screenshots/main-menu.png)

</td>
<td width="50%">

**Manage Profiles** — group systems into named profiles that control access.

![Manage Profiles](docs/screenshots/manage-profiles.png)

</td>
</tr>
<tr>
<td width="50%">

**Manage Users** — create accounts and assign them a user type; users are then linked to
profiles to grant access to specific systems.

![Manage Users](docs/screenshots/manage-users.png)

</td>
<td width="50%">

**Terminals** — pick one or more systems (optionally filtered by profile) to open
simultaneously.

![Terminals](docs/screenshots/terminals-select.png)

</td>
</tr>
<tr>
<td width="50%">

**Composite Scripts** — save a script once and execute it across every selected terminal.

![Composite Scripts](docs/screenshots/composite-scripts.png)

</td>
<td width="50%">

**User Settings** — change your password, pick a terminal theme, and view the public key
Bastillion uses to authenticate to registered systems.

![User Settings](docs/screenshots/user-settings.png)

</td>
</tr>
</table>

---

## License

Bastillion is available under the **Prosperity Public License**.

Full list of third-party dependencies and their licenses in [3rdPartyLicenses.md](3rdPartyLicenses.md).

**Loophole, LLC** — Sean Kavanagh

[sean@loophole.company](mailto:sean@loophole.company)
