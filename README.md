KeyBox
======

About
-----
A web-based ssh console to execute commands and manage multiple systems simultaneously. KeyBox allows you to
share terminal commands and upload files to all your systems. Once the sessions have been opened you can select
a single system or any combination to run your commands.  Also, additional system administrators can be added
and their terminal sessions and history can be audited.


Prerequisites
-------------
Java JDK 1.6 or greater
http://www.oracle.com/technetwork/java/javase/overview/index.html

Maven 3 or greater  ( Only needed if building from source )
http://maven.apache.org

Must run on *nix with OpenSSH version 2


To Run Bundled with Jetty
------
If your not big on the idea of building from source...

Download keybox-jetty-vXX.XX.tar.gz

https://github.com/skavanagh/KeyBox/releases

Export environment variables

     export JAVA_HOME=/path/to/jdk
     export PATH=$JAVA_HOME/bin:$PATH

Start KeyBox

        ./startKeyBox.sh

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
6. Start composite-ssh sessions or create and execute a script across multiple sessions
7. Add additional public keys to systems
8. Audit session history

Author
------
**Sean Kavanagh**

+ sean.p.kavanagh6@gmail.com
+ https://twitter.com/spkavanagh6


[![githalytics.com alpha](https://cruel-carlota.pagodabox.com/1d63734e95044db2bb95500235c0df9e "githalytics.com")](http://githalytics.com/skavanagh/KeyBox)


