#!/bin/bash

# Run keystore generation script
/docker-entrypoint.d/generate-keystore.sh

# Run Jetty start script
/docker-entrypoint.d/start-jetty.sh