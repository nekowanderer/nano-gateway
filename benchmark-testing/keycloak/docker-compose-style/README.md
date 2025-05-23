# Keycloak Test Suite

This project provides a containerized solution for performance testing of Keycloak authentication servers. It leverages the official Keycloak Benchmark tool to simulate various authentication scenarios and measure performance under load.

## Benchmark Test vs Load Test

Benchmark Tests and Load Tests have similarities but serve different purposes:

- **Benchmark Tests**: Focus on measuring system performance under specific conditions, providing clear and comparable performance metrics. They typically have standardized test scenarios and contexts, aiming to evaluate system performance under predefined conditions.

- **Load Tests**: Focus on simulating large numbers of concurrent users, aiming to identify system performance limits and bottlenecks. They test system stability and reliability under high pressure.

This test suite can achieve different testing objectives by adjusting parameters. For example, by modifying the `users-per-sec` parameter to increase load, or by selecting different `scenarios` to test various use cases.

## Project Structure

The project is organized as follows:

| File/Directory | Description |
|----------------|-------------|
| `run-benchmark.sh` | Main entry script for executing benchmark tests |
| `run_wrapper.sh` | Container wrapper script executed inside the Docker container |
| `dockerfile` | Docker container definition for the benchmark environment |
| `docker-compose.yml` | Docker Compose configuration for orchestrating containers |
| `keycloak-tools/` | Auto-managed directory containing Keycloak admin tools (kcadm.sh) and initialization scripts |
| `results/` | Directory where test results are stored |
| `aggregated-report/` | Directory containing aggregated HTML reports and summaries |
| `aggregated-report-template/` | Template for generating aggregated reports |
| `test_results_aggregator/` | Python package for aggregating and analyzing test results |
| `test_results_aggregator_legacy.py` | Legacy version of the results aggregator (for reference) |
| `aggregate_results.py` | Simplified entry point for running the results aggregator directly |

## How It Works

```
                                Docker Host
  +-----------------------------------------------------------------------+
  |                                                                       |
  |  +-----------------+     +-----------------------------------+        |
  |  |                 |     |                                   |        |
  |  | run-benchmark.sh| ==> | Multiple Docker Container         |        |
  |  | (Orchestration) |     | Instances (1-N)                   |        |
  |  |                 |     |                                   |        |
  |  +-----------------+     | +-------------------------------+ |        |
  |          |               | | Keycloak Benchmark Tool       | |        |
  |          |               | | (Load Generation)             | |        |
  |          v               | +-------------------------------+ |        |
  |  +-----------------+     |               |                   |        |
  |  |                 |     |               v                   |   +----+
  |  | Parameters      |     | +-------------------------------+ |   |    |
  |  | Configuration   | ==> | | Virtual Users                 |=====> K  |
  |  |                 |     | | (Simulated Clients/Browsers)  | |   | e  |
  |  +-----------------+     | +-------------------------------+ |   | y  |
  |          |               |               |                   |   | c  |
  |          v               |               v                   |   | l  |
  |  +-----------------+     | +-------------------------------+ |   | o  |
  |  | keycloak-tools/ |     | | Raw Result Collection         | |   | a  |
  |  | (--init only)   |     | | (Individual JSON/HTML)        | |   | k  |
  |  +-----------------+     | +-------------------------------+ |   |    |
  |                          |               |                   |   | S  |
  |                          +---------------|-------------------+   | e  |
  |                                          |                       | r  |
  |                                          v                       | v  |
  |                          +--------------------------------+      | e  |
  |                          |                                |      | r  |
  |                          | Host: ./results Directory      |      |    |
  |                          | (Collected from all containers)|      +----+
  |                          +--------------------------------+           |
  |                                          |                            |
  |                                          v                            |
  |                          +-------------------------------+            |
  |                          |                               |            |
  |                          | Host: Results Aggregator      |            |
  |                          | (Python processing)           |            |
  |                          +-------------------------------+            |
  |                                          |                            |
  |                                          v                            |
  |                          +-------------------------------+            |
  |                          |                               |            |
  |                          | Host: ./aggregated-report/    |            |
  |                          | (HTML Dashboard + JSON)       |            |
  |                          +-------------------------------+            |
  |                                                                       |
  +-----------------------------------------------------------------------+
```

### Keycloak Tools Management

The script can optionally set up the `keycloak-tools/` directory with necessary Keycloak administration tools when using the `--init` flag:

1. **Conditional Download**: When you run `./run-benchmark.sh --init`, it automatically downloads (if needed):
   - Keycloak 26.2.4 release (for admin tools)
   - Keycloak Benchmark repository (for initialization scripts)

2. **Admin Tools**: The `keycloak-tools/` directory contains:
   - `kcadm.sh` - Keycloak admin CLI tool
   - `client/` - Required JAR files and dependencies
   - `initialize-benchmark-entities.sh` - Script to set up test entities

3. **Password Requirement**: The `--init` flag requires the `-p, --password` parameter to:
   - Authenticate with your Keycloak server as admin
   - Create/configure test realms, clients, and users

4. **Clean Slate Testing**: The initialization process ensures that:
   - Test entities (realm, clients, users) are **completely recreated** for each test run
   - Previous test data is cleaned up automatically
   - QA teams get consistent, predictable test environments without manual cleanup

5. **Two Usage Modes**:
   - **With `--init`**: Downloads tools, initializes entities, then runs tests (⚠️ **Only use when test data doesn't exist or is corrupted**)
   - **Without `--init`**: Assumes test entities exist, runs tests directly (👍 **Recommended for regular testing**)

6. **Pre-configured Environments**:
   - **codacash-dev**: Already has `marketing-dev-benchmark-testing-realm` set up and ready to use
   - **Other environments**: May require `--init` for first-time setup

**Note**: Admin credentials are only used during the initialization phase on the host machine and are never passed to the Docker containers, ensuring better security.

## Quick Start with Docker

### Prerequisites
- Docker and Docker Compose installed
- Access to your Keycloak server endpoint
- **Keycloak Admin Credentials**: Valid admin username and password for your Keycloak instance
  - **Why required**: The benchmark tool needs admin privileges to:
    - Create and configure test realms, clients, and users
    - Initialize benchmark entities automatically
    - Clean up test data for consistent test environments
  - **How to obtain**: 
    - Use the default admin credentials you configured during Keycloak setup
      - For codacash-dev account, check in the AWS Secretes Manager
    - Ensure the user has `admin` role in the `master` realm
  - **Security considerations**:
    - Admin credentials are only used on the host machine during initialization
    - Passwords are never passed into Docker containers
    - Consider using a dedicated testing admin account rather than production admin credentials

### How to Run the Test

Run the benchmark using our convenient script:

```bash
./run-benchmark.sh -u http://your-keycloak-server:8080
```

**Important Note about Keycloak URL**: 
When specifying the Keycloak URL, ensure you use a hostname or IP address that is accessible from within Docker containers. Do not use `localhost` or `127.0.0.1` as the benchmark containers cannot reach your local machine's Keycloak instance. If your Keycloak is running locally, use your machine's LAN IP address (e.g., 192.168.x.x) or host.docker.internal instead.

By default, this will:
1. Build a Docker container with the Keycloak Benchmark tool
2. Run the benchmark with default parameters
3. Save results to the `./results` directory
4. Generate aggregated reports in the `./aggregated-report` directory

## Advanced Usage

The script supports various options to customize your test:

```bash
./run-benchmark.sh -h
```

### Key Options

| Option | Description | Required | Default |
|--------|-------------|----------|---------|
| `-u, --url` | Keycloak server URL. Must be an accessible hostname or IP address from the container. **Do not use localhost or 127.0.0.1** as the container cannot reach your local machine's Keycloak instance. | **Required** | - |
| `-p, --password` | Keycloak admin password. If the password contains special characters, please wrap it with quotes (e.g., `"aBcDeF)12]5566"`). | Optional | - |
| `--init` | Initialize Keycloak test entities (requires password). **Only use when test data doesn't exist or is corrupted and needs rebuilding. Do not use this option for regular testing.** | Optional | false |
| `--version` | Specify Docker image version tag. Useful for testing specific versions of the benchmark tool. | Optional | latest |
| `--use-local` | Use local Dockerfile build instead of DockerHub image. Useful for development or when making changes to the Dockerfile. | Optional | false |
| `--use-remote` | Use DockerHub image instead of local build. This is the default behavior. | Optional | true |
| `-s, --scenario` | Test scenario to run | Optional | keycloak.scenario.authentication.AuthorizationCode |
| `-n, --users-per-sec` | Users per second | Optional | 10 |
| `-t, --time` | Measurement time in seconds | Optional | 60 |
| `-r, --realm` | Test realm name. For codacash-dev environment, use `marketing-dev-benchmark-testing-realm` which is already set up and ready to use. | Optional | marketing-dev-benchmark-testing-realm |
| `-c, --clients` | Clients per realm | Optional | 1 |
| `-i, --instances` | Number of parallel container instances | Optional | 1 |
| `--clean` | Clean old containers and results | Optional | false |

### Examples

```bash
# Basic test (assumes test entities already exist in Keycloak)
./run-benchmark.sh -u http://your-keycloak-server:8080

# Test with codacash-dev environment using existing realm
./run-benchmark.sh -u http://your-keycloak-server:8080 -r marketing-dev-benchmark-testing-realm

# Initialize test environment and run test (only when data is missing/corrupted)
./run-benchmark.sh -u http://your-keycloak-server:8080 -p admin123 --init

# Run with 20 users per second for 2 minutes
./run-benchmark.sh -u http://your-keycloak-server:8080 -n 20 -t 120

# Run 3 parallel instances with 10 users per second each (30 total)
./run-benchmark.sh -u http://your-keycloak-server:8080 -i 3

# Test with the LoginUserPassword scenario
./run-benchmark.sh -u http://your-keycloak-server:8080 -s keycloak.scenario.authentication.LoginUserPassword

# Clean previous results before running
./run-benchmark.sh -u http://your-keycloak-server:8080 --clean

# High load test with codacash-dev environment
./run-benchmark.sh -u http://your-keycloak-server:8080m -r marketing-dev-benchmark-testing-realm -n 50 -t 10 -i 5 --clean

# For TeamCity pipeline:
./run-benchmark.sh \
 --url http://your-keycloak-server:8080 \
 --scenario keycloak.scenario.authentication.AuthorizationCode \
 --users-per-sec 50 \
 --time 600 \
 --realm marketing-dev-benchmark-testing-realm \
 --clients 1 \
 --instances 5 \
 --clean

# Full example with initialization (only when rebuilding is needed)
./run-benchmark.sh \
 --url http://your-keycloak-server:8080 \
 --password "aBcDeF)12]5566" \
 --init \
 --scenario keycloak.scenario.authentication.AuthorizationCode \
 --users-per-sec 10 \
 --time 60 \
 --realm benchmark-testing-realm \
 --clients 1 \
 --instances 5 \
 --clean

# Use a specific Docker image version 
./run-benchmark.sh -u http://your-keycloak-server:8080 --version v1.0.0

# Use local Dockerfile build instead of DockerHub image (for development)
./run-benchmark.sh -u http://your-keycloak-server:8080 --use-local

# Force using DockerHub image (default behavior)
./run-benchmark.sh -u http://your-keycloak-server:8080 --use-remote
```

## Scaling Load Tests

You can easily scale your load tests by:

1. Increasing users per second (`-n` option)
2. Running multiple container instances (`-i` option)
3. Running the script on multiple machines

The containerized approach ensures consistent testing environments across different machines.

## Available Scenarios

The test suite supports all scenarios from the Keycloak Benchmark tool, including:

- `keycloak.scenario.authentication.AuthorizationCode` - Standard OAuth2 authorization code flow
- `keycloak.scenario.authentication.LoginUserPassword` - Username/password login flow
- `keycloak.scenario.authentication.ClientSecret` - Client credentials grant flow
- And more...

See the [Keycloak Benchmark documentation](https://www.keycloak.org/keycloak-benchmark/benchmark-guide/latest/scenario-overview) for the complete list.

## Understanding Test Results

After running a test, you'll find:

1. **Detailed Results** in the `./results` directory:
   - HTML reports with response time graphs
   - Throughput statistics
   - Error rates and details

2. **Aggregated Reports** in the `./aggregated-report` directory:
   - Consolidated HTML report from all test instances
   - `result_summary.json` with key performance metrics

## Implementation Details

This project uses:
- Docker for containerization
- The official Keycloak Benchmark tool
- A custom wrapper script to simplify running tests
- Python-based test results aggregator for comprehensive reporting

## Docker Image Management

This project supports both local development and shared Docker images:

### Docker Hub Repository

The benchmark tool is available as a Docker image at:
```
nekowandrer/keycloak-benchmark
```

Available tags:
- `latest` - The most recent stable version 
- `v1.0.0` - Specific version releases

### Development Workflow

1. **Local Development**: 
   - Use `--use-local` flag to build from local Dockerfile
   - Useful when making changes to the Dockerfile or scripts
   - Example: `./run-benchmark.sh -u http://your-server:8080 --use-local`

2. **Production/Shared Usage**:
   - Use pre-built images from Docker Hub (default)
   - Faster startup and consistent environment
   - Example: `./run-benchmark.sh -u http://your-server:8080 --version v1.0.0`

3. **Creating New Versions**:
   ```bash
   # Build new version
   docker build -t nekowandrer/keycloak-benchmark:vX.Y.Z .
   
   # Tag as latest
   docker tag nekowandrer/keycloak-benchmark:vX.Y.Z nekowandrer/keycloak-benchmark:latest
   
   # Push to Docker Hub
   docker push nekowandrer/keycloak-benchmark:vX.Y.Z
   docker push nekowandrer/keycloak-benchmark:latest
   ```

## License Notice

Portions of this project are derived from the [Keycloak Benchmark project](https://github.com/keycloak/keycloak-benchmark/tree/main).
These files are licensed under the Apache License 2.0.
See `LICENSE` for details.
