#!/bin/bash

# Create results directory
mkdir -p /benchmark-results
mkdir -p /benchmark/keycloak-benchmark-26.2-SNAPSHOT/results

# Set default parameters
SCENARIO=${SCENARIO:-"keycloak.scenario.authentication.AuthorizationCode"}
SCENARIO_SUFFIX=${SCENARIO_SUFFIX:-"authorizationcode"}
SERVER_URL=${KEYCLOAK_URL}
USERS_PER_SEC=${USERS_PER_SEC:-"10"}
MEASUREMENT=${MEASUREMENT_TIME:-"60"}
REALM_NAME=${REALM_NAME:-"benchmark-testing-realm"}
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

# Copy results to mounted volume
if [ -d "/benchmark/keycloak-benchmark-26.2-SNAPSHOT/results" ]; then
  cp -r /benchmark/keycloak-benchmark-26.2-SNAPSHOT/results/* /benchmark-results/ || true
  echo "Results copied to mounted volume"
else
  echo "Warning: Results directory not found"
fi

# Output result summary
echo "===== Test Result Summary ====="
echo "Scenario: ${SCENARIO}"
echo "Scenario suffix: ${SCENARIO_SUFFIX}"
echo "Server: ${SERVER_URL}"
echo "Users per second: ${USERS_PER_SEC}"
echo "Measurement time (seconds): ${MEASUREMENT}"
echo "Test realm: ${REALM_NAME}"
echo "Number of clients per realm: ${CLIENTS_PER_REALM}"
echo "Full report saved to ./results directory"

# Analyze test results
echo "===== Analyzing Test Results ====="
cd /benchmark

# Create directory for aggregated report
mkdir -p /benchmark-results/aggregated-report

# Create result_summary.json file
OUTPUT_FILE="/benchmark-results/aggregated-report/result_summary.json"

# Verify python is available
if command -v python3 &> /dev/null; then
  echo "Running test results aggregator..."
  python3 -c "
import sys
sys.path.insert(0, '/benchmark')
from test_results_aggregator.main import main
sys.argv = ['', '${SCENARIO_SUFFIX}', '--dir', '/benchmark-results', '--output', '${OUTPUT_FILE}']
main()
"
  # Check if aggregation was successful
  if [ -f "${OUTPUT_FILE}" ]; then
    echo "Results successfully aggregated to ${OUTPUT_FILE}"
    
    # Optional: Generate HTML report
    python3 -c "
import sys
sys.path.insert(0, '/benchmark')
from test_results_aggregator.main import main
sys.argv = ['', '${SCENARIO_SUFFIX}', '--dir', '/benchmark-results', '--html', '/benchmark/aggregated-report']
main()
"
    # Copy aggregated report to mounted volume
    if [ -d "/benchmark/aggregated-report" ]; then
      cp -r /benchmark/aggregated-report/* /benchmark-results/aggregated-report/ || true
      echo "HTML report generated in /benchmark-results/aggregated-report/"
    fi
  else
    echo "Warning: Failed to generate result summary"
  fi
else
  echo "Warning: Python3 not available, skipping results aggregation"
fi 
