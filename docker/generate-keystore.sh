#!/bin/bash

KEYSTORE_PATH="/var/lib/jetty/etc/keystore.p12"
KEYSTORE_PASSWORD=${KEYSTORE_PASSWORD:-changeit}
DOMAIN_NAME=${DOMAIN_NAME:-$(hostname)}

# Ensure the directory exists and set correct permissions
mkdir -p /var/lib/jetty/etc
chown -R jetty:jetty /var/lib/jetty/etc
chmod -R 755 /var/lib/jetty/etc

generate_keystore() {
  echo "Keystore not found, generating a new one..."
  keytool -genkeypair -alias jetty -keyalg RSA -keysize 2048 -dname "CN=$DOMAIN_NAME, OU=IT, O=Company, L=City, S=State, C=US" -keypass "$KEYSTORE_PASSWORD" -keystore "$KEYSTORE_PATH" -storepass "$KEYSTORE_PASSWORD" -validity 365 -deststoretype pkcs12
}

decode_keystore() {
  echo "Verifying keystore decoding..."
  keytool -list -keystore "$KEYSTORE_PATH" -storepass "$KEYSTORE_PASSWORD" > /dev/null 2>&1
  if [ $? -eq 0 ]; then
    echo "Keystore decoded successfully."
  else
    echo "Failed to decode keystore. Please check the keystore password and try again."
    exit 1
  fi
}

if [ ! -f "$KEYSTORE_PATH" ]; then
  generate_keystore
  decode_keystore
else
  echo "Keystore already exists."
  decode_keystore
fi
