# Use tomcat as the parent image
FROM tomcat:10-jre17

# Set the environment variable to configure Tomcat
ENV CATALINA_HOME /usr/local/tomcat

# Set the working directory
WORKDIR $CATALINA_HOME/webapps

# Copy the compiled war file to the container
COPY target/bastillion-*.war bastillion.war

# Expose the application port
EXPOSE 8080

# Run Tomcat
CMD ["catalina.sh", "run"]