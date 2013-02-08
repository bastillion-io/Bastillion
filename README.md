KeyBox
======

About
-----
KeyBox provides a way to manage OpenSSH v2 public keys and distribute the generated authorized_keys files to systems that have been defined.

Steps:

1. Create users with public key
2. Create systems
3. Create profiles
4. Assign systems to profile
5. Assign profiles to users
6. Generate and set authorized key file for systems or users


Prerequisites
-------------
Maven 2 or greater
http://maven.apache.org

Java JDK 1.6 or greater
http://www.oracle.com/technetwork/java/javase/overview/index.html

  
    export JAVA_HOME=/path/to/jdk
    export M2_HOME=/path/to/maven
    export PATH=$JAVA_HOME/bin:$M2_HOME/bin:$PATH

Must run on *nix with OpenSSH version 2

To Run
------
In the directory that contains the pom.xml run

	mvn package jetty:run

Open browser to http://localhost:8090/keybox

Login with 

	username:admin 
	password:changeme

**Note: Doing a mvn clean will delete the SQLite DB and wipe out all the data.

Donate
------
If you find this tool helpful and want to donate you can do so through PayPal [here](https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=54K7AB3NRBM76)
