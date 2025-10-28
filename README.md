![Build](https://github.com/bastillion-io/Bastillion/actions/workflows/github-build.yml/badge.svg)
![CodeQL](https://github.com/bastillion-io/Bastillion/actions/workflows/codeql-analysis.yml/badge.svg)

![Bastillion logo](https://www.bastillion.io/images/bastillion_40x40.png)

# Bastillion

**A modern, web-based SSH console and key management tool.**

Bastillion gives you a clean, browser-based way to manage SSH access across all your systems.  
Think of it like a bastion host with a friendly dashboard.

You can:
- Log in with **2-factor authentication** (Authy or Google Authenticator)
- Manage and distribute **SSH public keys**
- Launch secure web shells and **share commands** across sessions
- Stack **TLS/SSL over SSH** for extra protection

Read more: [Implementing a Trusted Third-Party System for Secure Shell](https://www.bastillion.io/docs/using/whitepaper).

---

## Quick Start

Get the latest release:  
https://github.com/bastillion-io/Bastillion/releases

Or from AWS Marketplace:  
https://aws.amazon.com/marketplace/pp/Loophole-LLC-Bastillion/B076PNFPCL

FreeBSD:
```bash
pkg install security/bastillion
```

---

## Requirements

**Java (OpenJDK or Oracle JDK 1.9+)**
```bash
apt-get install openjdk-9-jdk
```

**Two-Factor Authentication**  
Install Authy or Google Authenticator on your device.

---

## Run with Jetty

Download latest bundle: https://github.com/bastillion-io/Bastillion/releases

Set environment variables:

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

Start:
```bash
./startBastillion.sh      # Linux / macOS
startBastillion.bat       # Windows
```

Open: `https://<server-ip>:8443`

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

Settings live in `BastillionConfig.properties`. Sample:
```properties
keyManagementEnabled=false
authKeysRefreshInterval=120
forceUserKeyGeneration=false
```

---

## Custom SSH Key Pair

Example config:
```properties
resetApplicationSSHKey=true
sshKeyType=rsa
privateKey=/Users/you/.ssh/id_rsa
publicKey=/Users/you/.ssh/id_rsa.pub
defaultSSHPassphrase=myPa$$w0rd
```

---

## Database Settings

Embedded H2 example:
```properties
dbUser=bastillion
dbPassword=p@$$w0rd!!
dbDriver=org.h2.Driver
dbConnectionURL=jdbc:h2:keydb/bastillion;CIPHER=AES;
```

Remote DB example:
```properties
dbConnectionURL=jdbc:h2:tcp://<host>:<port>/~/bastillion;CIPHER=AES;
```

---

## External Authentication (LDAP)

Set:
```properties
jaasModule=ldap-ol
```

And add LDAP details in `jaas.conf`.

---

## Auditing

Enable in `log4j2.xml` and:
```properties
enableInternalAudit=true
```

---

## Screenshots

(Images are shown using Markdown image syntax; no raw HTML.)

---

## Thanks to

- [JSch](http://www.jcraft.com/jsch)
- [term.js](https://github.com/chjj/term.js)

See full dependencies in `_3rdPartyLicenses.md_`.

---

## License

Prosperity Public License.

## Author

Loophole, LLC — Sean Kavanagh  
Email: sean.p.kavanagh6@gmail.com  
