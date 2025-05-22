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
  |  +-----------------+     |               |                   |   +-------------+
  |  |                 |     |               v                   |   |             |
  |  | Parameters      |     | +-------------------------------+ |   | Keycloak    |
  |  | Configuration   | ==> | | Virtual Users                 |======> Server     |
  |  |                 |     | | (Simulated Clients/Browsers)  | |   | Under Test  |
  |  +-----------------+     | +-------------------------------+ |   |             |
  |                          |               |                   |   +-------------+
  |                          |               v                   |
  |                          | +-------------------------------+ |
  |                          | | Result Collection             | |
  |                          | | (Raw Data)                    | |
  |                          | +-------------------------------+ |
  |                          |               |                   |
  |                          +---------------|-------------------+
  |                                          |
  |                                          v
  |  +-----------------+     +-------------------------------+
  |  |                 |     |                               |
  |  | Aggregator      | <== | Raw Results                   |
  |  | (Processing)    |     | (JSON Files)                  |
  |  |                 |     |                               |
  |  +-----------------+     +-------------------------------+
  |          |
  |          v
  |  +----------------------------------------------+
  |  |                                              |
  |  | Consolidated Reports                         |
  |  | (HTML Dashboard + JSON Summary)              |
  |  |                                              |
  |  +----------------------------------------------+
  |
  +-----------------------------------------------------------------------+
```

> For detailed information about the results aggregator, please refer to: [Test Results Aggregator README](test_results_aggregator/README.md)

## Quick Start with Docker

### Prerequisites
- Docker and Docker Compose installed
- Access to your Keycloak server endpoint

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
| `-s, --scenario` | Test scenario to run | Optional | keycloak.scenario.authentication.AuthorizationCode |
| `-n, --users-per-sec` | Users per second | Optional | 10 |
| `-t, --time` | Measurement time in seconds | Optional | 60 |
| `-r, --realm` | Test realm name | Optional | benchmark-testing-realm |
| `-c, --clients` | Clients per realm | Optional | 1 |
| `-i, --instances` | Number of parallel container instances | Optional | 1 |
| `--clean` | Clean old containers and results | Optional | false |

### Examples

```bash
# Run with 20 users per second for 2 minutes
./run-benchmark.sh -u http://your-keycloak-server:8080 -n 20 -t 120

# Run 3 parallel instances with 10 users per second each (30 total)
./run-benchmark.sh -u http://your-keycloak-server:8080 -i 3

# Test with the LoginUserPassword scenario
./run-benchmark.sh -u http://your-keycloak-server:8080 -s keycloak.scenario.authentication.LoginUserPassword

# Clean previous results before running
./run-benchmark.sh -u http://your-keycloak-server:8080 --clean

# High load test: 50 users/sec for 10 seconds with 5 parallel instances (250 users/sec total)
./run-benchmark.sh -u http://your-keycloak-server:8080 -n 50 -t 10 -i 5 --clean
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

## License Notice

Portions of this project are derived from the [Keycloak Benchmark project](https://github.com/keycloak/keycloak-benchmark/tree/main).
These files are licensed under the Apache License 2.0.
See `LICENSE` for details.
