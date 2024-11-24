# Use the official Jetty 12 image with JRE 17
FROM jetty:9.4-jre17

# Add a description for the Docker image
LABEL org.opencontainers.image.description="Bastillion is a web-based SSH console that centrally manages administrative access to systems. Web-based administration is combined with management and distribution of user's public SSH keys."

# Set the environment variable to configure Jetty
ENV JETTY_BASE=/var/lib/jetty

# Ensure the necessary directories are created
RUN mkdir -p $JETTY_BASE/etc

# Copy the scripts from the docker directory
COPY docker/generate-keystore.sh /docker-entrypoint.d/
COPY docker/start-jetty.sh /docker-entrypoint.d/
COPY docker/entrypoint.sh /docker-entrypoint.d/

# Copy the updated Jetty configuration files
COPY docker/jetty.xml $JETTY_BASE/etc/jetty.xml
COPY docker/jetty-ssl.xml $JETTY_BASE/etc/jetty-ssl.xml
COPY docker/jetty-https.xml $JETTY_BASE/etc/jetty-https.xml

# Copy the WAR file into the webapps directory
COPY target/bastillion-*.war $JETTY_BASE/webapps/root.war

# Expose port 8443
EXPOSE 8443

# Set the entrypoint script as the default command
CMD ["/docker-entrypoint.d/entrypoint.sh"]
