#!/bin/bash

# Define color codes
RED='\033[0;31m'
YELLOW='\033[1;33m'
GREEN='\033[0;32m'
NC='\033[0m' # No Color

# Create results directory
mkdir -p /benchmark-results
mkdir -p /benchmark/keycloak-benchmark-26.2-SNAPSHOT/results

# Set default parameters
SCENARIO=${SCENARIO:-"keycloak.scenario.authentication.AuthorizationCode"}
SCENARIO_SUFFIX=${SCENARIO_SUFFIX:-"authorizationcode"}
SERVER_URL=${KEYCLOAK_URL}
USERS_PER_SEC=${USERS_PER_SEC:-"10"}
MEASUREMENT=${MEASUREMENT_TIME:-"60"}
REALM_NAME=${REALM_NAME:-"marketing-dev-benchmark-testing-realm"}
CLIENTS_PER_REALM=${CLIENTS_PER_REALM:-"1"}

# Debug output - show received environment variables
echo "===== Container Environment Variables ====="
echo "SCENARIO from environment: ${SCENARIO}"
echo "SCENARIO_SUFFIX from environment: ${SCENARIO_SUFFIX}"
echo "KEYCLOAK_URL from environment: ${KEYCLOAK_URL}"
echo "USERS_PER_SEC from environment: ${USERS_PER_SEC}"
echo "MEASUREMENT_TIME from environment: ${MEASUREMENT_TIME}"
echo "REALM_NAME from environment: ${REALM_NAME}"
echo "CLIENTS_PER_REALM from environment: ${CLIENTS_PER_REALM}"
echo "=============================="

# Display test configuration
echo "===== Benchmark Configuration ====="
echo "Java version:"
java -version
echo "Scenario: ${SCENARIO}"
echo "Scenario suffix: ${SCENARIO_SUFFIX}"
echo "Server URL: ${SERVER_URL}"
echo "Users per sec: ${USERS_PER_SEC}"
echo "Measurement time: ${MEASUREMENT}"
echo "Realm name: ${REALM_NAME}"
echo "Clients per realm: ${CLIENTS_PER_REALM}"
echo "=============================="

# Execute test
cd /benchmark
./keycloak-benchmark-26.2-SNAPSHOT/bin/kcb.sh \
  --scenario=${SCENARIO} \
  --server-url=${SERVER_URL} \
  --users-per-sec=${USERS_PER_SEC} \
  --measurement=${MEASUREMENT} \
  --realm-name=${REALM_NAME} \
  --clients-per-realm=${CLIENTS_PER_REALM}

# Copy results to the aggregated results directory
RESULTS_DIR="/benchmark/keycloak-benchmark-26.2-SNAPSHOT/results"
if [ -d "${RESULTS_DIR}" ] && [ "$(ls -A ${RESULTS_DIR})" ]; then
  echo "Copying results from ${RESULTS_DIR} to /benchmark-results..."
  cp -r ${RESULTS_DIR}/* /benchmark-results/ || echo "Failed to copy some results"
  echo "Results copied successfully"
else
  echo -e "${RED}Warning: No results directory found at ${RESULTS_DIR}${NC}"
fi

echo -e "${GREEN}Container benchmark completed successfully${NC}" 
