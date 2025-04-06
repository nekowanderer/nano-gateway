#!/bin/bash

# Set the base directory for script paths
# Assume the current script is at the same level as components directory
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
BASE_DIR="$(dirname "$(dirname "$SCRIPT_DIR")")"

# Check and execute redis/script/clean_dev_data.sh
REDIS_CLEAN_SCRIPT="$BASE_DIR/components/redis/script/clean_dev_data.sh"
if [ -f "$REDIS_CLEAN_SCRIPT" ]; then
    echo "Executing redis data cleanup script..."
    bash "$REDIS_CLEAN_SCRIPT"
    if [ $? -ne 0 ]; then
        echo "Warning: redis data cleanup script execution failed"
    fi
else
    echo "Error: redis data cleanup script not found: $REDIS_CLEAN_SCRIPT"
fi

# Check and execute keycloak/script/clean_dev_data.sh
KEYCLOAK_CLEAN_SCRIPT="$BASE_DIR/components/keycloak/script/clean_dev_data.sh"
if [ -f "$KEYCLOAK_CLEAN_SCRIPT" ]; then
    echo "Executing keycloak data cleanup script..."
    bash "$KEYCLOAK_CLEAN_SCRIPT"
    if [ $? -ne 0 ]; then
        echo "Warning: keycloak data cleanup script execution failed"
    fi
else
    echo "Error: keycloak data cleanup script not found: $KEYCLOAK_CLEAN_SCRIPT"
fi

# Check and execute keycloak/script/generate_truststore.sh
KEYCLOAK_TRUSTSTORE_SCRIPT="$BASE_DIR/components/keycloak/script/generate_truststore_dev.sh"
if [ -f "$KEYCLOAK_TRUSTSTORE_SCRIPT" ]; then
    echo "Executing keycloak truststore generation script..."
    bash "$KEYCLOAK_TRUSTSTORE_SCRIPT"
    if [ $? -ne 0 ]; then
        echo "Warning: keycloak truststore generation script execution failed"
    fi
else
    echo "Error: keycloak truststore generation script not found: $KEYCLOAK_TRUSTSTORE_SCRIPT"
fi

# Check and execute postgresql/script/clean_dev_data.sh
POSTGRESQL_CLEAN_SCRIPT="$BASE_DIR/components/postgresql/script/clean_dev_data.sh"
if [ -f "$POSTGRESQL_CLEAN_SCRIPT" ]; then
    echo "Executing postgresql data cleanup script..."
    bash "$POSTGRESQL_CLEAN_SCRIPT"
    if [ $? -ne 0 ]; then
        echo "Warning: postgresql data cleanup script execution failed"
    fi
else
    echo "Error: postgresql data cleanup script not found: $POSTGRESQL_CLEAN_SCRIPT"
fi

echo "All scripts execution completed"