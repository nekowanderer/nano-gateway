#!/bin/bash

# Define color codes
RED='\033[0;31m'
YELLOW='\033[0;33m'
GREEN='\033[1;32m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

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
  echo "  -p, --password PASSWORD  Keycloak admin password (optional)"
  echo "  -s, --scenario SCENARIO  Test scenario (default: keycloak.scenario.authentication.AuthorizationCode)"
  echo "  -r, --realm REALM        Test realm name (default: benchmark-testing-realm)"
  echo "  -c, --clients NUM        Number of clients per realm (default: 1)"
  echo "  -n, --users-per-sec NUM  Users per second (default: 10)"
  echo "  -t, --time SECONDS       Measurement time in seconds (default: 60)"
  echo "  -i, --instances NUM      Number of parallel container instances to run (default: 1)"
  echo "  --clean                  Clean old containers and results before running"
  echo "  --init                   Initialize Keycloak test entities (requires password)"
  echo "  --version VERSION        Specify Docker image version tag (default: latest)"
  echo "  --use-local              Use local Dockerfile build instead of DockerHub image"
  echo "  --use-remote             Use DockerHub image instead of local build (default)"
  echo ""
  echo "Examples:"
  echo "  ./run-benchmark.sh -u http://keycloak.example.com --users-per-sec 20 --time 120 --instances 3"
  echo "  ./run-benchmark.sh -u http://keycloak.example.com -p admin123 --init -n 50 -t 300 -i 5 --clean"
  echo "  ./run-benchmark.sh -u http://keycloak.example.com --version 1.0.0"
  echo "  ./run-benchmark.sh -u http://keycloak.example.com --use-local  # Use local Dockerfile build"
  exit 0
}

# Check and install Python dependencies for report generation
function check_python_dependencies {
  echo -e "${CYAN}===== Checking Python Dependencies =====${NC}"
  
  # Check if Python 3 is available
  if ! command -v python3 &> /dev/null; then
    echo -e "${RED}ERROR: Python 3 is required but not installed${NC}"
    echo "Please install Python 3 and try again"
    echo "Visit: https://www.python.org/downloads/"
    exit 1
  fi
  
  # Check if pip3 is available
  if ! command -v pip3 &> /dev/null; then
    echo -e "${RED}ERROR: pip3 is required but not installed${NC}"
    echo "Please install pip3 and try again"
    exit 1
  fi
  
  # Check if BeautifulSoup4 is installed
  if ! python3 -c "import bs4" &> /dev/null; then
    echo -e "${YELLOW}BeautifulSoup4 not found. Installing...${NC}"
    pip3 install beautifulsoup4
    if [ $? -eq 0 ]; then
      echo -e "${GREEN}✓ BeautifulSoup4 installed successfully${NC}"
    else
      echo -e "${RED}✗ Failed to install BeautifulSoup4${NC}"
      echo -e "${YELLOW}⚠ Aggregated report generation may fail${NC}"
    fi
  else
    echo -e "${GREEN}✓ BeautifulSoup4 is already installed${NC}"
  fi
  
  echo -e "${GREEN}===== Python Dependencies Check Complete =====${NC}"
}

# Default parameters
KEYCLOAK_URL=""
KEYCLOAK_PASSWORD=""
SCENARIO="keycloak.scenario.authentication.AuthorizationCode"
REALM_NAME="benchmark-testing-realm"
CLIENTS_PER_REALM=1
USERS_PER_SEC=10
MEASUREMENT_TIME=60
INSTANCES=1
CLEAN=false
INIT=false
VERSION="latest"
USE_LOCAL=false

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
    -p|--password)
      KEYCLOAK_PASSWORD="$2"
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
    --init)
      INIT=true
      shift
      ;;
    --version)
      VERSION="$2"
      shift 2
      ;;
    --use-local)
      USE_LOCAL=true
      shift
      ;;
    --use-remote)
      USE_LOCAL=false
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
  echo -e "${RED}ERROR: Keycloak URL is required${NC}"
  echo "Please provide the Keycloak URL using the -u or --url parameter"
  echo "Example: ./run-benchmark.sh -u http://keycloak.example.com"
  echo ""
  echo "Use ./run-benchmark.sh --help for more information"
  exit 1
fi

# Check if --init and password dependencies are satisfied
if [ "$INIT" = true ] && [ -z "$KEYCLOAK_PASSWORD" ]; then
  echo -e "${RED}ERROR: --init requires password${NC}"
  echo "When using --init flag, you must provide the Keycloak admin password using -p or --password"
  echo "Example: ./run-benchmark.sh -u http://keycloak.example.com -p admin123 --init"
  echo ""
  echo "Use ./run-benchmark.sh --help for more information"
  exit 1
fi

if [ -n "$KEYCLOAK_PASSWORD" ] && [ "$INIT" != true ]; then
  echo -e "${YELLOW}WARNING: Password provided but --init not specified${NC}"
  echo "Password will be ignored unless --init flag is used"
  echo ""
fi

# Extract the last part of the scenario name and convert to lowercase
SCENARIO_SUFFIX=$(echo "$SCENARIO" | rev | cut -d. -f1 | rev | tr '[:upper:]' '[:lower:]')

# Clean up
if [ "$CLEAN" = true ]; then
  echo -e "${YELLOW}Cleaning old containers and results...${NC}"
  docker-compose down
  rm -rf results
  rm -rf aggregated-report
fi

# Create results directory
mkdir -p results

# Copy aggregated-report-template to aggregated-report
if [ -d "aggregated-report-template" ]; then
  echo -e "${CYAN}Copying aggregated-report-template to aggregated-report...${NC}"
  cp -r aggregated-report-template aggregated-report
else
  echo -e "${YELLOW}Warning: aggregated-report-template directory not found. Creating empty aggregated-report directory.${NC}"
  mkdir -p aggregated-report
fi

# Display test configuration
echo "===== Benchmark Configuration ====="
echo "Keycloak URL: $KEYCLOAK_URL"
echo "Keycloak password: ${KEYCLOAK_PASSWORD:+[PROVIDED]}"
echo "Test scenario: $SCENARIO"
echo "Test realm: $REALM_NAME"
echo "Clients per realm: $CLIENTS_PER_REALM"
echo "Users per second: $USERS_PER_SEC"
echo "Measurement time (seconds): $MEASUREMENT_TIME"
echo "Container instances: $INSTANCES"
echo "Scenario suffix: $SCENARIO_SUFFIX"
echo "Docker image version: $VERSION"
echo "Use local build: $([ "$USE_LOCAL" = true ] && echo "Yes" || echo "No")"
echo "=============================="

# Set environment variables and run docker-compose
export KEYCLOAK_URL="$KEYCLOAK_URL"
export SCENARIO="$SCENARIO"
export SCENARIO_SUFFIX="$SCENARIO_SUFFIX"
export REALM_NAME="$REALM_NAME"
export CLIENTS_PER_REALM="$CLIENTS_PER_REALM"
export USERS_PER_SEC="$USERS_PER_SEC"
export MEASUREMENT_TIME="$MEASUREMENT_TIME"
export VERSION="$VERSION"

# Debug output - show exported variables
echo "===== Exported Environment Variables ====="
echo "KEYCLOAK_URL=$KEYCLOAK_URL"
echo "SCENARIO=$SCENARIO"
echo "SCENARIO_SUFFIX=$SCENARIO_SUFFIX"
echo "REALM_NAME=$REALM_NAME"
echo "CLIENTS_PER_REALM=$CLIENTS_PER_REALM"
echo "USERS_PER_SEC=$USERS_PER_SEC"
echo "MEASUREMENT_TIME=$MEASUREMENT_TIME"
echo "VERSION=$VERSION"
echo "=============================="

# Initialize Keycloak (only if password is provided)
if [ "$INIT" = true ] && [ -n "$KEYCLOAK_PASSWORD" ]; then
  echo -e "${CYAN}===== Initializing Keycloak =====${NC}"
  
  # Prepare Keycloak tools first
  echo -e "${CYAN}===== Preparing Keycloak Tools =====${NC}"
  mkdir -p keycloak-tools

  # Check if keycloak-tools is complete
  if [ ! -f "keycloak-tools/kcadm.sh" ] || [ ! -d "keycloak-tools/client" ] || [ ! -f "keycloak-tools/initialize-benchmark-entities.sh" ]; then
    echo -e "${YELLOW}Setting up Keycloak tools...${NC}"
    
    # Download Keycloak 26.2.4 if not exists
    if [ ! -f "keycloak-26.2.4.tar.gz" ]; then
      echo -e "${CYAN}Downloading Keycloak 26.2.4...${NC}"
      curl -L -o keycloak-26.2.4.tar.gz https://github.com/keycloak/keycloak/releases/download/26.2.4/keycloak-26.2.4.tar.gz
    fi
    
    # Extract Keycloak
    echo -e "${CYAN}Extracting Keycloak...${NC}"
    tar -xzf keycloak-26.2.4.tar.gz
    
    # Copy kcadm.sh and client directory
    echo -e "${CYAN}Copying kcadm.sh and client libraries...${NC}"
    cp keycloak-26.2.4/bin/kcadm.sh keycloak-tools/
    cp -r keycloak-26.2.4/bin/client keycloak-tools/
    chmod +x keycloak-tools/kcadm.sh
    
    # Download Keycloak Benchmark repository if not exists
    if [ ! -d "keycloak-benchmark-repo" ]; then
      echo -e "${CYAN}Downloading Keycloak Benchmark repository...${NC}"
      git clone https://github.com/keycloak/keycloak-benchmark.git keycloak-benchmark-repo
    fi
    
    # Copy initialize-benchmark-entities.sh
    echo -e "${CYAN}Copying initialize-benchmark-entities.sh...${NC}"
    cp keycloak-benchmark-repo/benchmark/src/main/content/bin/initialize-benchmark-entities.sh keycloak-tools/
    chmod +x keycloak-tools/initialize-benchmark-entities.sh
    
    # Clean up temporary files
    echo -e "${CYAN}Cleaning up temporary files...${NC}"
    rm -rf keycloak-26.2.4
    rm -rf keycloak-benchmark-repo
    
    echo -e "${GREEN}✓ Keycloak tools setup complete${NC}"
  else
    echo -e "${GREEN}✓ Keycloak tools already available${NC}"
  fi
  
  # Configure Keycloak admin credentials
  echo -e "${CYAN}Configuring Keycloak admin credentials...${NC}"
  ./keycloak-tools/kcadm.sh config credentials --server "$KEYCLOAK_URL" --realm master --user admin --password "$KEYCLOAK_PASSWORD"
  if [ $? -eq 0 ]; then
    echo -e "${GREEN}✓ Keycloak admin credentials configured successfully${NC}"
  else
    echo -e "${RED}✗ Failed to configure Keycloak admin credentials${NC}"
    exit 1
  fi
  
  # Initialize benchmark entities
  echo -e "${CYAN}Initializing benchmark entities...${NC}"
  ./keycloak-tools/initialize-benchmark-entities.sh -r "$REALM_NAME" -c gatling -u user-0 -d
  if [ $? -eq 0 ]; then
    echo -e "${GREEN}✓ Benchmark entities initialized successfully${NC}"
  else
    echo -e "${RED}✗ Failed to initialize benchmark entities${NC}"
    exit 1
  fi
  
  echo -e "${GREEN}===== Initialization Complete =====${NC}"
else
  echo -e "${YELLOW}===== Skipping Initialization =====${NC}"
  echo -e "${YELLOW}Use --init flag with password to initialize test entities${NC}"
fi

# Run docker-compose with --build to ensure container is rebuilt with new environment variables
if [ "$INSTANCES" -gt 1 ]; then
  echo -e "${CYAN}Starting $INSTANCES test instances...${NC}"
  if [ "$USE_LOCAL" = true ]; then
    # Use local Dockerfile and force rebuild
    docker-compose build --no-cache
    docker-compose up --scale benchmark=$INSTANCES
  else
    # Use DockerHub image
    docker-compose pull
    docker-compose up --scale benchmark=$INSTANCES
  fi
else
  echo -e "${CYAN}Starting a single test instance...${NC}"
  if [ "$USE_LOCAL" = true ]; then
    # Use local Dockerfile and force rebuild
    docker-compose build --no-cache
    docker-compose up
  else
    # Use DockerHub image
    docker-compose pull
    docker-compose up
  fi
fi

echo -e "${GREEN}Test completed. Results saved in ./results directory.${NC}"

# Generate aggregated report from collected results
if [ -d "results" ] && [ "$(ls -A results)" ]; then
  # Check Python dependencies before generating report
  check_python_dependencies
  
  echo -e "${CYAN}===== Generating Aggregated Report =====${NC}"
  python3 aggregate_results.py "$SCENARIO_SUFFIX"
  if [ $? -eq 0 ]; then
    echo -e "${GREEN}✓ Aggregated report generated successfully${NC}"
    echo -e "${GREEN}Report available at: ./aggregated-report/index.html${NC}"
  else
    echo -e "${YELLOW}⚠ Aggregated report generation had issues, but individual results are available${NC}"
  fi
else
  echo -e "${YELLOW}⚠ No results found, aggregated report not generated${NC}"
fi 
