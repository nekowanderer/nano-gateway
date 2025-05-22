#!/bin/bash

# Display help information
function show_help {
  echo "Keycloak Benchmark Test Script"
  echo ""
  echo "Usage: ./run-benchmark.sh -u <KEYCLOAK_URL> [options]"
  echo ""
  echo "Required Parameters:"
  echo "  -u, --url URL            Keycloak server URL (required)"
  echo ""
  echo "Optional Parameters:"
  echo "  -h, --help               Display this help information"
  echo "  -s, --scenario SCENARIO  Test scenario (default: keycloak.scenario.authentication.AuthorizationCode)"
  echo "  -r, --realm REALM        Test realm name (default: benchmark-testing-realm)"
  echo "  -c, --clients NUM        Number of clients per realm (default: 1)"
  echo "  -n, --users-per-sec NUM  Users per second (default: 10)"
  echo "  -t, --time SECONDS       Measurement time in seconds (default: 60)"
  echo "  -i, --instances NUM      Number of parallel container instances to run (default: 1)"
  echo "  --clean                  Clean old containers and results before running"
  echo ""
  echo "Examples:"
  echo "  ./run-benchmark.sh -u http://keycloak.example.com --users-per-sec 20 --time 120 --instances 3"
  echo "  ./run-benchmark.sh -u http://keycloak.example.com -n 50 -t 300 -i 5 --clean"
  exit 0
}

# Default parameters
KEYCLOAK_URL=""
SCENARIO="keycloak.scenario.authentication.AuthorizationCode"
REALM_NAME="benchmark-testing-realm"
CLIENTS_PER_REALM=1
USERS_PER_SEC=10
MEASUREMENT_TIME=60
INSTANCES=1
CLEAN=false

# Parse command line arguments
while [[ $# -gt 0 ]]; do
  case $1 in
    -h|--help)
      show_help
      ;;
    -u|--url)
      KEYCLOAK_URL="$2"
      shift 2
      ;;
    -s|--scenario)
      SCENARIO="$2"
      shift 2
      ;;
    -r|--realm)
      REALM_NAME="$2"
      shift 2
      ;;
    -c|--clients)
      CLIENTS_PER_REALM="$2"
      shift 2
      ;;
    -n|--users-per-sec)
      USERS_PER_SEC="$2"
      shift 2
      ;;
    -t|--time)
      MEASUREMENT_TIME="$2"
      shift 2
      ;;
    -i|--instances)
      INSTANCES="$2"
      shift 2
      ;;
    --clean)
      CLEAN=true
      shift
      ;;
    *)
      echo "Unknown option: $1"
      echo "Use --help to see help information"
      exit 1
      ;;
  esac
done

# Check if Keycloak URL is provided
if [ -z "$KEYCLOAK_URL" ]; then
  echo "ERROR: Keycloak URL is required"
  echo "Please provide the Keycloak URL using the -u or --url parameter"
  echo "Example: ./run-benchmark.sh -u http://keycloak.example.com"
  echo ""
  echo "Use ./run-benchmark.sh --help for more information"
  exit 1
fi

# Extract the last part of the scenario name and convert to lowercase
SCENARIO_SUFFIX=$(echo "$SCENARIO" | rev | cut -d. -f1 | rev | tr '[:upper:]' '[:lower:]')

# Clean up
if [ "$CLEAN" = true ]; then
  echo "Cleaning old containers and results..."
  docker-compose down
  rm -rf results
  rm -rf aggregated-report
fi

# Create results directory
mkdir -p results

# Copy aggregated-report-template to aggregated-report
if [ -d "aggregated-report-template" ]; then
  echo "Copying aggregated-report-template to aggregated-report..."
  cp -r aggregated-report-template aggregated-report
else
  echo "Warning: aggregated-report-template directory not found. Creating empty aggregated-report directory."
  mkdir -p aggregated-report
fi

# Display test configuration
echo "===== Benchmark Configuration ====="
echo "Keycloak URL: $KEYCLOAK_URL"
echo "Test scenario: $SCENARIO"
echo "Test realm: $REALM_NAME"
echo "Clients per realm: $CLIENTS_PER_REALM"
echo "Users per second: $USERS_PER_SEC"
echo "Measurement time (seconds): $MEASUREMENT_TIME"
echo "Container instances: $INSTANCES"
echo "Scenario suffix: $SCENARIO_SUFFIX"
echo "=============================="

# Set environment variables and run docker-compose
export KEYCLOAK_URL="$KEYCLOAK_URL"
export SCENARIO="$SCENARIO"
export SCENARIO_SUFFIX="$SCENARIO_SUFFIX"
export REALM_NAME="$REALM_NAME"
export CLIENTS_PER_REALM="$CLIENTS_PER_REALM"
export USERS_PER_SEC="$USERS_PER_SEC"
export MEASUREMENT_TIME="$MEASUREMENT_TIME"

# Debug output - show exported variables
echo "===== Exported Environment Variables ====="
echo "KEYCLOAK_URL=$KEYCLOAK_URL"
echo "SCENARIO=$SCENARIO"
echo "SCENARIO_SUFFIX=$SCENARIO_SUFFIX"
echo "REALM_NAME=$REALM_NAME"
echo "CLIENTS_PER_REALM=$CLIENTS_PER_REALM"
echo "USERS_PER_SEC=$USERS_PER_SEC"
echo "MEASUREMENT_TIME=$MEASUREMENT_TIME"
echo "=============================="

# Run docker-compose with --build to ensure container is rebuilt with new environment variables
if [ "$INSTANCES" -gt 1 ]; then
  echo "Starting $INSTANCES test instances..."
  docker-compose up --build --scale benchmark=$INSTANCES
else
  echo "Starting a single test instance..."
  docker-compose up --build
fi

echo "Test completed. Results saved in ./results directory." 
