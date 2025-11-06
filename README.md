![Build](https://github.com/bastillion-io/Bastillion/actions/workflows/github-build.yml/badge.svg)
![CodeQL](https://github.com/bastillion-io/Bastillion/actions/workflows/codeql-analysis.yml/badge.svg)

![Bastillion](https://www.bastillion.io/images/bastillion_40x40.png)

# Bastillion

**A modern, web-based SSH console and key management tool.**

Bastillion gives you a clean, browser-based way to manage SSH access across all your systems—like a bastion host with a friendly dashboard.

You can:
- Log in with **2-factor authentication** (Authy or Google Authenticator)
- Manage and distribute **SSH public keys**
- Launch secure web shells and **share commands** across sessions
- Stack **TLS/SSL over SSH** for extra protection

Read more: [Implementing a Trusted Third-Party System for Secure Shell](https://www.bastillion.io/docs/using/whitepaper).

![Terminals](https://www.bastillion.io/images/screenshots/medium/terminals.png)

---

## 🚀 What’s New
- Upgraded to **Java 21** and **Jakarta EE 11**
- Full support for **Ed25519** (default) and **Ed448** SSH keys
- New **daemon mode** for Jetty startup (`--daemon`)
- Updated dependencies for improved security and performance


---
## Installation Options
**Free:** https://github.com/bastillion-io/Bastillion/releases  
**AWS Marketplace:** https://aws.amazon.com/marketplace/pp/Loophole-LLC-Bastillion/B076PNFPCL

---

## Prerequisites

### Java 21 (OpenJDK or Oracle JDK)
```bash
apt-get install openjdk-21-jdk
```
> Oracle JDK download: http://www.oracle.com/technetwork/java/javase/downloads/index.html

### Authenticator (for 2FA)

| Application | Android | iOS |
|--------------|----------|-----|
| **Authy** | [Google Play](https://play.google.com/store/apps/details?id=com.authy.authy) | [iTunes](https://itunes.apple.com/us/app/authy/id494168017) |
| **Google Authenticator** | [Google Play](https://play.google.com/store/apps/details?id=com.google.android.apps.authenticator2) | [iTunes](https://itunes.apple.com/us/app/google-authenticator/id388497605) |

---

## Run with Jetty (Bundled)

Download: https://github.com/bastillion-io/Bastillion/releases

### Set Environment Variables
**Linux / macOS**
```bash
export JAVA_HOME=/path/to/jdk
export PATH=$JAVA_HOME/bin:$PATH
```
**Windows**
```cmd
set JAVA_HOME=C:\path\to\jdk
set PATH=%JAVA_HOME%\bin;%PATH%
```

### Start Bastillion
Foreground (interactive):
```bash
./startBastillion.sh
```

Daemon (background):
```bash
./startBastillion.sh --daemon
```
Logs are stored in `jetty/logs/YYYY_MM_DD.jetty.log`.

Enable debug output:
```bash
./startBastillion.sh -d
```

Stop:
```bash
./stopBastillion.sh
```

Access in browser:  
`https://<server-ip>:8443` (or for AMI instances: `https://<instance-ip>:443`)

Default credentials:
```
username: admin
password: changeme
```

---

## Build from Source

Install Maven 3+:
```bash
apt-get install maven
```

Build and run:
```bash
mvn package jetty:run
```

> ⚠️ `mvn clean` will remove the H2 database and user data.

---

## SSH Key Management

Settings live in `BastillionConfig.properties`:

```properties
# Disable key management (append instead of overwrite)
keyManagementEnabled=false

# authorized_keys refresh interval in minutes (no refresh for <=0)
authKeysRefreshInterval=120

# Force user key generation and strong passphrases
forceUserKeyGeneration=false
```

---

## Custom SSH Key Pair

Specify a custom SSH key pair or let Bastillion generate its own on startup:

```properties
# Regenerate and import SSH keys
resetApplicationSSHKey=true

# SSH key type ('rsa', 'ecdsa', 'ed25519', or 'ed448')
# Supported options:
#   rsa    - Classic, widely compatible (configurable length, default 4096)
#   ecdsa  - Faster, smaller keys (P-256/384/521 curves)
#   ed25519 - Default and recommended (≈ RSA-4096, secure and fast)
#   ed448  - Extra-strong (≈ RSA-8192, slower and less supported)
sshKeyType=ed25519

# Private key
privateKey=/Users/you/.ssh/id_rsa

# Public key
publicKey=/Users/you/.ssh/id_rsa.pub

# Passphrase (leave blank if none)
defaultSSHPassphrase=myPa$$w0rd
```

Once registered, you can remove the key files and passphrase from the configuration.

---

## Database Settings

Embedded H2 example:
```properties
dbUser=bastillion
dbPassword=p@$$w0rd!!
dbDriver=org.h2.Driver
dbConnectionURL=jdbc:h2:keydb/bastillion;CIPHER=AES;
```

Remote H2 example:
```properties
dbConnectionURL=jdbc:h2:tcp://<host>:<port>/~/bastillion;CIPHER=AES;
```

---

## External Authentication (LDAP)

Enable external auth in `BastillionConfig.properties`:
```properties
jaasModule=ldap-ol
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

---

## Auditing

Auditing is disabled by default.

Enable it in **log4j2.xml** by uncommenting:
- `io.bastillion.manage.util.SystemAudit`
- `audit-appender`

> https://github.com/bastillion-io/Bastillion/blob/master/src/main/resources/log4j2.xml#L19-L22

Also enable in `BastillionConfig.properties`:
```properties
enableInternalAudit=true
```

---

## Screenshots

![Login](https://www.bastillion.io/images/screenshots/medium/login.png)

![Two-Factor](https://www.bastillion.io/images/screenshots/medium/two-factor.png)

![Terminals](https://www.bastillion.io/images/screenshots/medium/terminals.png)

![Manage Systems](https://www.bastillion.io/images/screenshots/medium/manage_systems.png)

![Manage Users](https://www.bastillion.io/images/screenshots/medium/manage_users.png)

![Define SSH Keys](https://www.bastillion.io/images/screenshots/medium/manage_keys.png)

![Disable SSH Keys](https://www.bastillion.io/images/screenshots/medium/disable_keys.png)

---

## Thanks to

- [JSch](http://www.jcraft.com/jsch)
- [term.js](https://github.com/chjj/term.js)

See full dependencies in [_3rdPartyLicenses.md_](3rdPartyLicenses.md).

---

## License

Bastillion is available under the **Prosperity Public License**.

---

## Author

**Loophole, LLC — Sean Kavanagh**  
Email: [sean.p.kavanagh6@gmail.com](mailto:sean.p.kavanagh6@gmail.com)  
Instagram: [@spkavanagh6](https://www.instagram.com/spkavanagh6/)
