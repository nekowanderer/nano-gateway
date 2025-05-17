#!/usr/bin/env bash
set -euxo pipefail

# Enable Gradle colored output
export GRADLE_OPTS="-Dorg.gradle.console=rich"

# Default values (can be overridden by environment variables)
AWS_REGION=${AWS_REGION:-"ap-northeast-1"}
SCENARIO=${SCENARIO:-"authentication.AuthorizationCode"}
KC_SERVER_URL=${KC_SERVER_URL:-""}
MEASUREMENT=${MEASUREMENT:-"5"}
USERS_PER_SEC=${USERS_PER_SEC:-"5"}
REALM_NAME=${REALM_NAME:-"benchmark-testing-realm"}
CLIENTS_PER_REALM=${CLIENTS_PER_REALM:-"1"}

# Parse command line arguments (these will override environment variables)
while [[ $# -gt 0 ]]; do
  case $1 in
    --region)
      AWS_REGION="$2"
      shift 2
      ;;
    --scenario)
      SCENARIO="$2"
      shift 2
      ;;
    --server-url)
      KC_SERVER_URL="$2"
      shift 2
      ;;
    --measurement)
      MEASUREMENT="$2"
      shift 2
      ;;
    --users-per-sec)
      USERS_PER_SEC="$2"
      shift 2
      ;;
    --realm-name)
      REALM_NAME="$2"
      shift 2
      ;;
    --clients-per-realm)
      CLIENTS_PER_REALM="$2"
      shift 2
      ;;
    *)
      echo "Unknown parameter: $1"
      exit 1
      ;;
  esac
done

# Validate required parameters
if [ -z "$KC_SERVER_URL" ]; then
  echo "Error: --server-url is required (can be set via environment variable KC_SERVER_URL or command line parameter)"
  exit 1
fi

# Set environment variables for the rest of the script
export AWS_REGION
export REPORTS_HOME="reports"
export SCENARIO
export KC_SERVER_URL
export MEASUREMENT
export USERS_PER_SEC
export REALM_NAME
export CLIENTS_PER_REALM

# $KEYCLOAK_HOME/bin/kcadm.sh config credentials --server SERVER_URL --realm master --user admin --password 

# Ensure we're in the correct directory
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
cd "$SCRIPT_DIR/benchmark-runner"

./aws_ec2.sh requirements

./aws_ec2.sh create ${AWS_REGION}

./benchmark.sh ${AWS_REGION} \
  --scenario=keycloak.scenario.${SCENARIO} \
  --server-url=${KC_SERVER_URL} \
  --users-per-sec=${USERS_PER_SEC} \
  --measurement=${MEASUREMENT} \
  --realm-name=${REALM_NAME} \
  --clients-per-realm=${CLIENTS_PER_REALM}

# Check if results directory exists
RESULTS_DIR=$(find files/benchmark/keycloak-benchmark-*/results -type d 2>/dev/null)
if [ -z "$RESULTS_DIR" ]; then
  echo "Error: Results directory not found in files/benchmark/keycloak-benchmark-*/results"
  exit 1
fi

# Create reports directory if it doesn't exist
mkdir -p ${REPORTS_HOME}

# Copy results
cp -r ${RESULTS_DIR}/* ${REPORTS_HOME}/

# Clean up files directory
rm -rf files

# Clean up AWS resources
./aws_ec2.sh delete ${AWS_REGION}
