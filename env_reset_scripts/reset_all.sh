#!/bin/bash

# Get the directory where this script is located
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

echo "===== Starting execution of all reset scripts ====="

# Execute reset_dev.sh
echo "===== Starting development environment reset script ====="
if [ -f "$SCRIPT_DIR/reset_dev.sh" ]; then
    bash "$SCRIPT_DIR/reset_dev.sh"
    if [ $? -ne 0 ]; then
        echo "Warning: Development environment reset script execution failed"
    fi
else
    echo "Error: Development environment reset script not found: $SCRIPT_DIR/reset_dev.sh"
fi

# Execute reset_infra.sh
echo "===== Starting infrastructure reset script ====="
if [ -f "$SCRIPT_DIR/reset_infra.sh" ]; then
    bash "$SCRIPT_DIR/reset_infra.sh"
    if [ $? -ne 0 ]; then
        echo "Warning: Infrastructure reset script execution failed"
    fi
else
    echo "Error: Infrastructure reset script not found: $SCRIPT_DIR/reset_infra.sh"
fi

echo "===== All reset scripts execution completed ====="
