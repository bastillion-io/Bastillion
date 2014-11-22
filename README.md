KeyBox
======

![Terminals](http://sshkeybox.com/img/screenshots/medium/terms.png)

About
-----
A web-based ssh console to execute commands and manage multiple systems
simultaneously. KeyBox allows you to share terminal commands and upload files to
all your systems. Once the sessions have been opened you can select a single
system or any combination to run your commands.  Additional system
administrators can be added and their terminal sessions and history can be
audited. Also, KeyBox can manage and distribute public keys that have been setup
and defined.

Prerequisites
-------------
* Java JDK 1.7 or greater
http://www.oracle.com/technetwork/java/javase/overview/index.html

* Browser with Web Socket support
http://caniuse.com/websockets *Note: In Safari if using a self-signed certificate you must import the certificate into your Keychain.
Select 'Show Certificate' -> 'Always Trust' when prompted in Safari*

* Maven 3 or greater  ( Only needed if building from source )
http://maven.apache.org

* Install [FreeOTP](https://fedorahosted.org/freeotp) or [Google Authenticator](https://github.com/google/google-authenticator) to enable two-factor authentication with Android or iOS

| FreeOTP       | Link                                                                                 |
|:------------- |:------------------------------------------------------------------------------------:|
| Android       | [Google Play](https://play.google.com/store/apps/details?id=org.fedorahosted.freeotp)|
| iOS           | [iTunes](https://itunes.apple.com/us/app/freeotp/id872559395)                        |

| Google Authenticator| Link                                                                                                                                                                                   |
|:------------------- |:--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------:|
| Android             | [Google Play](https://play.google.com/store/apps/details?id=com.google.android.apps.authenticator2)|
| iOS                 | [iTunes](https://itunes.apple.com/us/app/google-authenticator/id388497605)                                                                                                              |

To Run Bundled with Jetty
------
If your not big on the idea of building from source...

Download keybox-jetty-vXX.XX.tar.gz

https://github.com/skavanagh/KeyBox/releases

Export environment variables

for Linux/Unix/OSX

     export JAVA_HOME=/path/to/jdk
     export PATH=$JAVA_HOME/bin:$PATH

for Windows

     set JAVA_HOME=C:\path\to\jdk
     set PATH=%JAVA_HOME%\bin;%PATH%

Start KeyBox

for Linux/Unix/OSX

        ./startKeyBox.sh

for Windows

        startKeyBox.bat

How to Configure SSL in Jetty
(it is a good idea to add or generate your own unique certificate)

http://wiki.eclipse.org/Jetty/Howto/Configure_SSL

To Build from Source
------
Export environment variables

    export JAVA_HOME=/path/to/jdk
    export M2_HOME=/path/to/maven
    export PATH=$JAVA_HOME/bin:$M2_HOME/bin:$PATH

In the directory that contains the pom.xml run

	mvn package jetty:run

**Note: Doing a mvn clean will delete the H2 DB and wipe out all the data.

Supplying a Custom SSH Key Pair
------
KeyBox generates its own public/private SSH key upon initial startup for use when registering systems.  You can specify a custom SSH key pair in the KeyBoxConfig.properties file.  This file is located in the jetty/keybox/WEB-INF/classes directory. (or the src/main/resources directory if building from source)

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

Using KeyBox
------
Open browser to https://\<whatever ip\>:8443

Login with

	username:admin
	password:changeme

Steps:

1. Create systems
2. Create profiles
3. Assign systems to profile
4. Assign profiles to users
5. Users can login to create sessions on assigned systems
6. Start a composite SSH session or create and execute a script across multiple sessions
7. Add additional public keys to systems
8. Audit session history

Screenshots
-----------
![Login](http://sshkeybox.com/img/screenshots/medium/login.png)

![Two-Factor](http://sshkeybox.com/img/screenshots/medium/two-factor.png)

![More Terminals](http://sshkeybox.com/img/screenshots/medium/more_terms.png)

![Upload Files](http://sshkeybox.com/img/screenshots/medium/upload_files.png)

![Manage Systems](http://sshkeybox.com/img/screenshots/medium/manage_systems.png)

![Manage Users](http://sshkeybox.com/img/screenshots/medium/manage_users.png)

![Define SSH Keys](http://sshkeybox.com/img/screenshots/medium/manage_keys.png)

Acknowledgments
------
Special thanks goes to these amazing projects which makes this (and other great projects) possible.

+ [JSch](http://www.jcraft.com/jsch) Java Secure Channel - by [ymnk](https://github.com/ymnk)
+ [term.js](https://github.com/chjj/term.js) A terminal written in javascript - by [chjj](https://github.com/chjj)

Author
------
**Sean Kavanagh**

+ sean.p.kavanagh6@gmail.com
+ https://twitter.com/spkavanagh6

(Follow me on twitter for release updates, but mostly nonsense)


