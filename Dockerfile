# Use tomcat as the parent image
FROM tomcat:10-jre17

LABEL org.opencontainers.image.description "Bastillion is a web-based SSH console that centrally manages administrative access to systems. Web-based administration is combined with management and distribution of user's public SSH keys.""

# Set the environment variable to configure Tomcat
ENV CATALINA_HOME /usr/local/tomcat

# Set the working directory
WORKDIR $CATALINA_HOME/webapps

# Copy the compiled war file to the container
COPY target/bastillion-*.war bastillion.war

# Debug logging
COPY logging.properties $CATALINA_HOME/conf/

# Expose the application port
EXPOSE 8080

# Run Tomcat
CMD ["catalina.sh", "run"]