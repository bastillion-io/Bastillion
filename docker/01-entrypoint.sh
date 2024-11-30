#!/bin/bash

# Run keystore generation script (for Jetty SSL)
#/docker-entrypoint.d/generate-keystore.sh

# Configure app settings
/docker-entrypoint.d/03-configure-app.sh

# Run Jetty start script
/docker-entrypoint.d/99-start-jetty.sh