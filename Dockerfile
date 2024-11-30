# Use a base image to set file permissions
FROM alpine as file_prep

# Create a directory for the files
RUN mkdir -p /app

COPY target/bastillion-* /app

# Change the ownership of the files to jetty:jetty
RUN chown -R 999:999 /app

# Use the official Jetty 9.4 image with JRE 21
FROM jetty:9.4-jre21

# Add a description for the Docker image
LABEL org.opencontainers.image.description="Bastillion is a web-based SSH console that centrally manages administrative access to systems. Web-based administration is combined with management and distribution of user's public SSH keys."

# Set the environment variable to configure Jetty
ENV JETTY_BASE=/var/lib/jetty

# Set default environment variables for Bastillion
ENV RESET_APPLICATION_SSH_KEY=false
ENV SSH_KEY_TYPE=rsa
ENV SSH_KEY_LENGTH=4096
ENV PRIVATE_KEY=
ENV PUBLIC_KEY=
ENV DEFAULT_SSH_PASSPHRASE=\${randomPassphrase}
ENV ENABLE_INTERNAL_AUDIT=false
ENV DELETE_AUDIT_LOG_AFTER=90
ENV SERVER_ALIVE_INTERVAL=60
ENV WEBSOCKET_TIMEOUT=0
ENV AGENT_FORWARDING=false
ENV ONE_TIME_PASSWORD=optional
ENV KEY_MANAGEMENT_ENABLED=true
ENV FORCE_USER_KEY_GENERATION=true
ENV AUTH_KEYS_REFRESH_INTERVAL=120
ENV PASSWORD_COMPLEXITY_REGEX="((?=.*\\d)(?=.*[A-Z])(?=.*[a-z])(?=.*[!@#$%^&*()_=\\[\\]{};':\"\\\\|\,.<>\\/?+-]).{8,20})"
ENV PASSWORD_COMPLEXITY_MSG="Passwords must be 8 to 20 characters\, contain one digit\, one lowercase\, one uppercase\, and one special character"
ENV ACCOUNT_EXPIRATION_DAYS=-1
ENV CLIENT_IP_HEADER=
ENV JAAS_MODULE=
ENV DEFAULT_PROFILE_FOR_LDAP=
ENV SESSION_TIMEOUT=15
ENV DB_USER=bastillion
ENV DB_PASSWORD=
ENV DB_DRIVER=org.h2.Driver
ENV DB_CONNECTION_URL=jdbc:h2:file:keydb/bastillion;CIPHER=AES;
ENV MAX_ACTIVE=25
ENV TEST_ON_BORROW=true
ENV MIN_IDLE=2
ENV MAX_WAIT=15000

# Copy the scripts from the docker directory
COPY docker/*.sh /docker-entrypoint.d/

# Copy the files with correct permissions from the file_prep stage
COPY --from=file_prep /app $JETTY_BASE/webapps/root

# Expose port 8080
EXPOSE 8080

# Set the entrypoint script as the default command
#ENTRYPOINT ["/docker-entrypoint.d/"]
CMD  ["/docker-entrypoint.d/01-entrypoint.sh"]