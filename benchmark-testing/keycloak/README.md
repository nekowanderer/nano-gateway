# Keycloak Benchmark Testing

This project provides tools and scripts to perform benchmark testing on an existing Keycloak cluster. It leverages the Keycloak Benchmark tool to simulate various authentication scenarios and measure the cluster's performance under load.

## Quick Start

### Prerequisites
- Java 21
- AWS CLI configured and logged in
- Ansible
- Keycloak 26.2.4 or newer

### Installation Steps

1. Initialize benchmark entities
- Please refer to [Keycloak Branchmark Testing Guide](Keycloak_branchmark_testing_manual.md) for more details.

2. Run the test:
```bash
./gradlew runBenchmark \
    -Pregion=ap-northeast-1 \
    -Pscenario=authentication.AuthorizationCode \
    -PserverUrl=YOUR_KEYCLOAK_SERVER_URL \
    -Pmeasurement=60 \
    -PusersPerSec=10 \
    -PrealmName=benchmark-testing-realm \
    -PclientsPerRealm=1
```

## Important Parameters

| Parameter | Description |
|-----------|-------------|
| `region` | AWS region |
| `scenario` | Defines the Gatling scenario to run. A scenario simulates a specific type of user behavior, such as logging in with client credentials or executing token refreshes. You can find the list of available scenarios [here](https://www.keycloak.org/keycloak-benchmark/benchmark-guide/latest/scenario-overview). |
| `serverUrl` | The base URL of the Keycloak instance you want to test (e.g., the load balancer endpoint or public-facing Keycloak address). |
| `measurement` | Duration of the test in seconds. This defines how long the benchmark will run and continuously apply load to the Keycloak server. |
| `usersPerSec` | Number of virtual users to simulate per second. This controls the request load rate generated during the benchmark. |
| `realmName` | The name of the Keycloak realm to be used in the benchmark. This must match the realm created during initialization. |
| `clientsPerRealm` | Number of client IDs per realm used in the benchmark test. This should correspond to the number of clients generated during test data setup. |

## Documentation

- [Keycloak Branchmark Testing Guide](Keycloak_branchmark_testing_manual.md) - Complete setup and testing procedures
- [Keycloak Benchmark Official Documentation](https://www.keycloak.org/keycloak-benchmark/benchmark-guide/latest/)

## Important Notes

- Always run `./aws_ec2.sh delete <REGION>` after testing to clean up AWS resources
- Test reports will be generated in the `benchmark-runner/reports` directory

## License Notice

Portions of this project are derived from the [Keycloak Benchmark project](https://github.com/keycloak/keycloak-benchmark/tree/main).
These files are licensed under the Apache License 2.0.
See `LICENSE` for details.
