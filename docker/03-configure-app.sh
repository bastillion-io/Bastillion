#!/bin/sh

# Path to BastillionConfig.properties file
CONFIG_FILE=$JETTY_BASE/webapps/root/WEB-INF/classes/BastillionConfig.properties

# Function to escape special characters for use in sed
escape_for_sed() {
    printf '%s\n' "$1" | sed -e 's/[\/&]/\\&/g' -e 's/\\/\\\\/g'
}

# Function to set the property if it's unset or empty, or update if different
set_property_if_unset() {
    local key=$1
    local value=$2
    local current_value
    local escaped_value

    # Function to remove escape characters
    unescape_value() {
        echo "$1" | sed 's/\\//g'
    }

    # Get the current value of the key from the config file
    current_value=$(grep "^$key=" $CONFIG_FILE | cut -d'=' -f2-)
    unescaped_current_value=$(unescape_value "$current_value")
    unescaped_value=$(unescape_value "$value")

    # Skip updating if both the ENV variable and the property are empty
    if [ -z "$unescaped_value" ] && [ -z "$unescaped_current_value" ]; then
        echo "$key and ENV variable are both empty. Skipping update."
        return
    fi

    # Set the property if the ENV variable contains a value and the property is empty
    if [ -n "$unescaped_value" ] && [ -z "$unescaped_current_value" ]; then
        echo "$key is empty. Setting value."
        escaped_value=$(printf '%s\n' "$value" | sed -e 's/[\/&]/\\&/g' -e 's/\\/\\\\/g')
        sed -i "s|^$key=$|$key=$escaped_value|" $CONFIG_FILE
        return
    fi

    # Update the property if the ENV variable contains a value that is different from the property
    if [ -n "$unescaped_value" ] && [ "$unescaped_value" != "$unescaped_current_value" ]; then
        echo "$key has a different value. Updating."
        escaped_value=$(printf '%s\n' "$value" | sed -e 's/[\/&]/\\&/g' -e 's/\\/\\\\/g')
        sed -i "s|^$key=.*|$key=$escaped_value|" $CONFIG_FILE
        return
    fi

    # Skip updating if the ENV variable contains a value that is the same as the property
    if [ -n "$unescaped_value" ] && [ "$unescaped_value" = "$unescaped_current_value" ]; then
        echo "$key is already set to the same value. Skipping update."
        return
    fi
}

# Function to forcefully update a property in the config file
set_property() {
    local key=$1
    local value=$2
    sed -i "s|^$key=.*|$key=$value|" $CONFIG_FILE || echo "$key=$value" >> $CONFIG_FILE
}

# Function to update or generate db password as needed
check_db_password() {
    if grep -q "^dbPassword=[^ ]" $CONFIG_FILE; then
        echo "dbPassword is already set in the config file."
    else
        if [ -z "$DB_PASSWORD" ]; then
            echo "DB_PASSWORD is not set. Generating a new password..."
            DB_PASSWORD=$(tr -dc A-Za-z0-9 </dev/urandom | head -c 16)
            echo "**"
            echo "Generated DB_PASSWORD: $DB_PASSWORD"
            echo "**"
            echo "Please make sure to save this password."
        fi
        # Use set_property_if_unset function to set the dbPassword property
        set_property_if_unset "dbPassword" "$DB_PASSWORD"
    fi
}

echo ""
echo "***************************************************"
echo ""
echo "   Configuring Bastillion based on ENV variables"
echo ""
echo "***************************************************"
echo ""

# Update properties from environment variables
[ -n "$RESET_APPLICATION_SSH_KEY" ] && set_property_if_unset "resetApplicationSSHKey" "$RESET_APPLICATION_SSH_KEY"
[ "$RESET_APPLICATION_SSH_KEY" = "true" ] && {
    [ -n "$SSH_KEY_TYPE" ] && set_property "sshKeyType" "$SSH_KEY_TYPE"
    [ -n "$SSH_KEY_LENGTH" ] && set_property "sshKeyLength" "$SSH_KEY_LENGTH"
    [ -n "$PRIVATE_KEY" ] && set_property "privateKey" "$PRIVATE_KEY"
    [ -n "$PUBLIC_KEY" ] && set_property "publicKey" "$PUBLIC_KEY"
    [ -n "$DEFAULT_SSH_PASSPHRASE" ] && set_property "defaultSSHPassphrase" "$DEFAULT_SSH_PASSPHRASE"
}

[ -n "$ENABLE_INTERNAL_AUDIT" ] && set_property_if_unset "enableInternalAudit" "$ENABLE_INTERNAL_AUDIT"
[ -n "$DELETE_AUDIT_LOG_AFTER" ] && set_property_if_unset "deleteAuditLogAfter" "$DELETE_AUDIT_LOG_AFTER"
[ -n "$SERVER_ALIVE_INTERVAL" ] && set_property_if_unset "serverAliveInterval" "$SERVER_ALIVE_INTERVAL"
[ -n "$WEBSOCKET_TIMEOUT" ] && set_property_if_unset "websocketTimeout" "$WEBSOCKET_TIMEOUT"
[ -n "$AGENT_FORWARDING" ] && set_property_if_unset "agentForwarding" "$AGENT_FORWARDING"
[ -n "$ONE_TIME_PASSWORD" ] && set_property_if_unset "oneTimePassword" "$ONE_TIME_PASSWORD"
[ -n "$KEY_MANAGEMENT_ENABLED" ] && set_property_if_unset "keyManagementEnabled" "$KEY_MANAGEMENT_ENABLED"
[ -n "$FORCE_USER_KEY_GENERATION" ] && set_property_if_unset "forceUserKeyGeneration" "$FORCE_USER_KEY_GENERATION"
[ -n "$AUTH_KEYS_REFRESH_INTERVAL" ] && set_property_if_unset "authKeysRefreshInterval" "$AUTH_KEYS_REFRESH_INTERVAL"
[ -n "$PASSWORD_COMPLEXITY_REGEX" ] && set_property_if_unset "passwordComplexityRegEx" "$PASSWORD_COMPLEXITY_REGEX"
[ -n "$PASSWORD_COMPLEXITY_MSG" ] && set_property_if_unset "passwordComplexityMsg" "$PASSWORD_COMPLEXITY_MSG"
[ -n "$ACCOUNT_EXPIRATION_DAYS" ] && set_property_if_unset "accountExpirationDays" "$ACCOUNT_EXPIRATION_DAYS"
[ -n "$CLIENT_IP_HEADER" ] && set_property_if_unset "clientIPHeader" "$CLIENT_IP_HEADER"
[ -n "$JAAS_MODULE" ] && set_property_if_unset "jaasModule" "$JAAS_MODULE"
[ -n "$DEFAULT_PROFILE_FOR_LDAP" ] && set_property_if_unset "defaultProfileForLdap" "$DEFAULT_PROFILE_FOR_LDAP"
[ -n "$SESSION_TIMEOUT" ] && set_property_if_unset "sessionTimeout" "$SESSION_TIMEOUT"

# Update DB settings from environment variables only if they are not already set
[ -n "$DB_USER" ] && set_property_if_unset "dbUser" "$DB_USER"
check_db_password
[ -n "$DB_DRIVER" ] && set_property_if_unset "dbDriver" "$DB_DRIVER"
[ -n "$DB_CONNECTION_URL" ] && set_property_if_unset "dbConnectionURL" "$DB_CONNECTION_URL"
[ -n "$MAX_ACTIVE" ] && set_property_if_unset "maxActive" "$MAX_ACTIVE"
[ -n "$TEST_ON_BORROW" ] && set_property_if_unset "testOnBorrow" "$TEST_ON_BORROW"
[ -n "$MIN_IDLE" ] && set_property_if_unset "minIdle" "$MIN_IDLE"
[ -n "$MAX_WAIT" ] && set_property_if_unset "maxWait" "$MAX_WAIT"


# Print the raw value of PASSWORD_COMPLEXITY_REGEX
echo "Raw value of PASSWORD_COMPLEXITY_REGEX:"
echo "$PASSWORD_COMPLEXITY_REGEX"

