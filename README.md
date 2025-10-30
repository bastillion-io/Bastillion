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

![Terminals](https://www.bastillion.io/images/screenshots/medium/terminals.png)
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

External Authentication
------
External Authentication can be enabled through the BastillionConfig.properties.

For example:

	#specify a external authentication module (ex: ldap-ol, ldap-ad).  Edit the jaas.conf to set connection details
	jaasModule=ldap-ol

Connection details need to be set in the jaas.conf file

    ldap-ol {
    	com.sun.security.auth.module.LdapLoginModule SUFFICIENT
    	userProvider="ldap://hostname:389/ou=example,dc=bastillion,dc=com"
    	userFilter="(&(uid={USERNAME})(objectClass=inetOrgPerson))"
    	authzIdentity="{cn}"
    	useSSL=false
    	debug=false;
    };


Administrators will be added as they are authenticated and profiles of systems may be assigned by full-privileged users.

User LDAP roles can be mapped to profiles defined in Bastillion through the use of the org.eclipse.jetty.jaas.spi.LdapLoginModule.

    ldap-ol-with-roles {
        //openldap auth with roles that can map to profiles
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

Users will be added/removed from defined profiles as they login and when the role name matches the profile name.

Auditing
------
Auditing is disabled by default. Audit logs can be enabled through the **log4j2.xml** by uncommenting the **io.bastillion.manage.util.SystemAudit** and the **audit-appender** definitions.

> https://github.com/bastillion-io/Bastillion/blob/master/src/main/resources/log4j2.xml#L19-L22

Auditing through the application is only a proof of concept.  It can be enabled in the BastillionConfig.properties.

	#enable audit  --set to true to enable
	enableInternalAudit=true


Screenshots
-----------
![Login](https://www.bastillion.io/images/screenshots/medium/login.png)

![Two-Factor](https://www.bastillion.io/images/screenshots/medium/two-factor.png)

![More Terminals](https://www.bastillion.io/images/screenshots/medium/terminals.png)

![Manage Systems](https://www.bastillion.io/images/screenshots/medium/manage_systems.png)

![Manage Users](https://www.bastillion.io/images/screenshots/medium/manage_users.png)

![Define SSH Keys](https://www.bastillion.io/images/screenshots/medium/manage_keys.png)

![Disable SSH Keys](https://www.bastillion.io/images/screenshots/medium/disable_keys.png)

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
