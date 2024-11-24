#!/bin/bash

echo "Starting Jetty 12..."

# Start Jetty with SSL and HTTPS configuration
java -Dorg.eclipse.jetty.LEVEL=ALL -jar /usr/local/jetty/start.jar --config jetty.xml --config jetty-ssl.xml --config jetty-https.xml