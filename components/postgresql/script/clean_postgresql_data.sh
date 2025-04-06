#!/bin/bash

# Navigate to the directory where this script is located
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

# Determine the path of the postgresql_data directory (same level as script directory)
PG_DATA_DIR="$(dirname "$SCRIPT_DIR")/postgresql_data"

# Check if the postgresql_data directory exists
if [ ! -d "$PG_DATA_DIR" ]; then
    echo "Error: postgresql_data directory does not exist: $PG_DATA_DIR"
    exit 1
fi

echo "Clearing the contents of $PG_DATA_DIR..."

# Delete all contents within the directory but keep the directory itself
find "$PG_DATA_DIR" -mindepth 1 -delete

if [ $? -eq 0 ]; then
    echo "Successfully cleared the postgresql_data directory."
else
    echo "Error occurred while clearing the postgresql_data directory."
    exit 1
fi

exit 0
