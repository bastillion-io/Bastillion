#!/bin/sh

# Start Jetty in the background to allow it to unpack the WAR file
echo "Starting Jetty to unpack the WAR file..."
java -jar /usr/local/jetty/start.jar &

# Give Jetty some time to unpack the WAR file
sleep 10

# Stop Jetty after it has unpacked the WAR file
echo "Stopping Jetty after unpacking the WAR file..."
kill $(pgrep -f 'jetty.start')

# Step 2: Modify the BastillionConfig.properties file
CONFIG_FILE=$JETTY_BASE/webapps/root/WEB-INF/classes/BastillionConfig.properties

if [ -f "$CONFIG_FILE" ]; then
    echo "Extraction successful."
else
    echo "Configuration file not found!"
fi