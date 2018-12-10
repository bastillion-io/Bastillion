Bastillion
======
Bastillion is a web-based SSH console that centrally manages administrative access to systems. Web-based administration is combined with management and distribution of user's public SSH keys. Key management and administration is based on profiles assigned to defined users.

Administrators can login using two-factor authentication with [FreeOTP](https://freeotp.github.io/) or [Google Authenticator](https://github.com/google/google-authenticator). From there they can manage their public SSH keys or connect to their systems through a web-shell. Commands can be shared across shells to make patching easier and eliminate redundant command execution.

Bastillion layers TLS/SSL on top of SSH and acts as a bastion host for administration. Protocols are stacked (TLS/SSL + SSH) so infrastructure cannot be exposed through tunneling / port forwarding. More details can be found in the following whitepaper: [Implementing a Trusted Third-Party System for Secure Shell](https://www.bastillion.io/docs/using/whitepaper). Also, SSH key management is enabled by default to prevent unmanaged public keys and enforce best practices.

![Terminals](https://www.bastillion.io/images/screenshots/medium/terminals.png)

Bastillion Releases
------
Commercial users can purchase a license through an annual subscription and access the binary releases. 

https://www.bastillion.io/subscription

Also, users can purchase through the AWS Marketplace.

Feel free to try out Bastillion for a limited amount of users(6) or systems(12)!

https://github.com/bastillion-io/Bastillion/releases

Prerequisites
-------------
**Open-JDK / Oracle-JDK - 1.9 or greater**

*apt-get install openjdk-9-jdk*
> http://www.oracle.com/technetwork/java/javase/downloads/index.html

**Maven 3 or greater**  *(Only needed if building from source)*

*apt-get install maven* 
> http://maven.apache.org 

**Loophole MVC**  *(Only needed if building from source)*

> https://github.com/bastillion-io/lmvc

**Install [FreeOTP](https://freeotp.github.io/) or [Google Authenticator](https://github.com/google/google-authenticator)** to enable two-factor authentication with Android or iOS

| Application          | Android                                                                                             | iOS                                                                        |             
|----------------------|-----------------------------------------------------------------------------------------------------|----------------------------------------------------------------------------|
| FreeOTP              | [Google Play](https://play.google.com/store/apps/details?id=org.fedorahosted.freeotp)               | [iTunes](https://itunes.apple.com/us/app/freeotp/id872559395)              |
| Google Authenticator | [Google Play](https://play.google.com/store/apps/details?id=com.google.android.apps.authenticator2) | [iTunes](https://itunes.apple.com/us/app/google-authenticator/id388497605) |
    
Build from Source
------
Export environment variables

    export JAVA_HOME=/path/to/jdk
    export M2_HOME=/path/to/maven
    export PATH=$JAVA_HOME/bin:$M2_HOME/bin:$PATH

In the directory that contains the pom.xml run

	mvn package jetty:run

*Note: Doing a mvn clean will delete the H2 DB and wipe out all the data.*

Using Bastillion
------
Open browser to https://\<whatever ip\>:8443

Login with

	username:admin
	password:changeme
	
*Note: When using the AMI instance, the password is defaulted to the \<Instance ID\>. Also, the AMI uses port 443 as in https://\<Instance IP\>:443*

Steps:

1. Create systems
2. Create profiles
3. Assign systems to profile
4. Assign profiles to users
5. Users can login to create sessions on assigned systems
6. Start a composite SSH session or create and execute a script across multiple sessions
7. Add additional public keys to systems
8. Disable any administrative public key forcing key rotation.
9. Audit session history

Managing SSH Keys
------
By default Bastillion will overwrite all values in the specified authorized_keys file for a system.  You can disable key management by editing BastillionConfig.properties file and use Bastillion only as a bastion host.  This file is located in the jetty/bastillion/WEB-INF/classes directory. (or the src/main/resources directory if building from source)

	#set to false to disable key management. If false, the Bastillion public key will be appended to the authorized_keys file (instead of it being overwritten completely).
	keyManagementEnabled=false

Also, the authorized_keys file is updated/refreshed periodically based on the relationships defined in the application.  If key management is enabled the refresh interval can be specified in the BastillionConfig.properties file.

	#authorized_keys refresh interval in minutes (no refresh for <=0)
	authKeysRefreshInterval=120

By default Bastillion will generated and distribute the SSH keys managed by administrators while having them download the generated private. This forces admins to use strong passphrases for keys that are set on systems.  The private key is only available for download once and is not stored on the application side.  To disable and allow administrators to set any public key edit the BastillionConfig.properties.

	#set to true to generate keys when added/managed by users and enforce strong passphrases set to false to allow users to set their own public key
	forceUserKeyGeneration=false

Supplying a Custom SSH Key Pair
------
Bastillion generates its own public/private SSH key upon initial startup for use when registering systems.  You can specify a custom SSH key pair in the BastillionConfig.properties file.

For example:

	#set to true to regenerate and import SSH keys  --set to true
	resetApplicationSSHKey=true

	#SSH Key Type 'dsa' or 'rsa'
	sshKeyType=rsa

	#private key  --set pvt key
	privateKey=/Users/kavanagh/.ssh/id_rsa

	#public key  --set pub key
	publicKey=/Users/kavanagh/.ssh/id_rsa.pub
	
	#default passphrase  --leave blank if passphrase is empty
	defaultSSHPassphrase=myPa$$w0rd
	
After startup and once the key has been registered it can then be removed from the system. The passphrase and the key paths will be removed from the configuration file.

Adjusting Database Settings
------
Database settings can be adjusted in the configuration properties.

    #Database user
    dbUser=bastillion
    #Database password
    dbPassword=p@$$w0rd!!
    #Database JDBC driver
    dbDriver=org.h2.Driver
    #Connection URL to the DB
    dbConnectionURL=jdbc:h2:keydb/bastillion;CIPHER=AES;

By default the datastore is set as embedded, but a remote H2 database can supported through adjusting the connection URL.

    #Connection URL to the DB
	dbConnectionURL=jdbc:h2:tcp://<host>:<port>/~/bastillion;CIPHER=AES;

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
Auditing is disabled by default and is only a proof of concept.  Can be enabled in the BastillionConfig.properties.

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

Acknowledgments
------
Special thanks goes to these amazing projects which makes this (and other great projects) possible.

+ [JSch](http://www.jcraft.com/jsch) Java Secure Channel - by [ymnk](https://github.com/ymnk)
+ [term.js](https://github.com/chjj/term.js) A terminal written in javascript - by [chjj](https://github.com/chjj)

Third-party dependencies are mentioned in the [_3rdPartyLicenses.md_](3rdPartyLicenses.md)

Dual License
-----------
Bastillion is available for non-commercial use under the Affero General Public License

A commercial license is also available through a subscription

https://www.bastillion.io/subscription

or when running an AMI from the AWS marketplace.

http://aws.amazon.com/marketplace/pp/B076PNFPCL

Author
------
**Loophole, LLC - Sean Kavanagh**

+ sean.p.kavanagh6@gmail.com
+ https://twitter.com/spkavanagh6

