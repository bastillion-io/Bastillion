KeyBox
======

About
-----
KeyBox provides a way to manage OpenSSH v2 public keys and can start a web-based ssh terminal to execute commands and
scripts on multiple ssh sessions simultaneously.


Prerequisites
-------------
SQLite3
http://www.sqlite.org/download.html

    sudo apt-get install sqlite3 sqlite3-dev 

**Should already be installed in Mac OS X v10.5 or greater

Java JDK 1.6 or greater
http://www.oracle.com/technetwork/java/javase/overview/index.html

Maven 3 or greater ( Only needed if building from source )
http://maven.apache.org

Must run on *nix with OpenSSH version 2


To Build from Source and Run with Maven
------
Export environment variables

    export JAVA_HOME=/path/to/jdk
    export M2_HOME=/path/to/maven
    export PATH=$JAVA_HOME/bin:$M2_HOME/bin:$PATH

In the directory that contains the pom.xml run

	mvn package jetty:run

**Note: Doing a mvn clean will delete the SQLite DB and wipe out all the data.


To Run Jetty Build
------
Export Environment Variables

     export JAVA_HOME=/path/to/jdk
     export PATH=$JAVA_HOME/bin:$PATH

Start KeyBox
	
	./startKeyBox.sh


Using KeyBox
------
Open browser to http://localhost:8090

Login with 

	username:admin 
	password:changeme

Steps:

1. Create users with public key
2. Create systems
3. Create profiles
4. Assign systems to profile
5. Assign profiles to users
6. Generate and distribute authorized key file for systems or users
7. Start composite-ssh sessions or create or execute a script across multiple sessions


Donate
------
If really like this tool useful and are feeling awesome you can donate through my [PayPal](https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=54K7AB3NRBM76)

[![githalytics.com alpha](https://cruel-carlota.pagodabox.com/1d63734e95044db2bb95500235c0df9e "githalytics.com")](http://githalytics.com/skavanagh/KeyBox)
