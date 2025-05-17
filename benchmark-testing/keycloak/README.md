# Keycloak Benchmark Testing

This project provides tools and scripts to perform benchmark testing on an existing Keycloak cluster. It leverages the Keycloak Benchmark tool to simulate various authentication scenarios and measure the cluster's performance under load.

## Quick Start

### Prerequisites
- Java 21
- AWS CLI configured and logged in
- Ansible
- Keycloak 26.2.4 or newer

### Installation Steps

1. Set up environment variables:
```bash
export KEYCLOAK_HOME=PATH_TO_YOUR_KEYCLOAK/keycloak-26.2.4
export PATH=$PATH:$KEYCLOAK_HOME/bin
```

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
| `scenario` | Test scenario |
| `serverUrl` | Keycloak server URL |
| `measurement` | Test duration in seconds |
| `usersPerSec` | Number of simulated users per second |
| `realmName` | Keycloak realm name |
| `clientsPerRealm` | Number of clients per realm |

## Documentation

- [Manual Testing Guide](manual_testing.md) - Complete setup and testing procedures
- [Keycloak Benchmark Official Documentation](https://www.keycloak.org/keycloak-benchmark/benchmark-guide/latest/)

## Important Notes

- Always run `./aws_ec2.sh delete <REGION>` after testing to clean up AWS resources
- Test reports will be generated in the `benchmark-runner/reports` directory

## License Notice

Portions of this project are derived from the [Keycloak Benchmark project](https://github.com/keycloak/keycloak-benchmark/tree/main).
These files are licensed under the Apache License 2.0.
See `LICENSE` for details.
