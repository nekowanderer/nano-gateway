# Perform the Benchmark Testing for Keycloak

### Intro
Suppose you've set up the Keycloak cluster in the AWS environment, which could be leveraged for the benchmark testing. This note will describe the detailed steps for how to do it.

### Prerequisites
- A fully set up Keycloak environment, here will use the Keycloak cluster in the codacash-dev for example.
- Install the required JDK on your local machine. At the moment of writing this page, the required version is `Java 21`.
- Clone the keycloak-benchmark repository from [here](https://github.com/keycloak/keycloak-benchmark). We’ll need to launch the script from this repo.
- You can download the specific version of the Keycloak benchmark execution file from [here](https://github.com/keycloak/keycloak-benchmark/releases). Since we installed version `26.2.4`, this page will use [Keycloak Benchmark 26.2-SNAPSHOT](https://github.com/keycloak/keycloak-benchmark/releases/tag/26.2-SNAPSHOT) as an example.
- Download the Keycloak distribution from [the official site](https://www.keycloak.org/downloads) on your local machine. To interact with Keycloak, we need to leverage the kcadm.sh script; you don't need to launch Keycloak at all. On this page, we’ll take `26.2.4` as an example.
- Add the following environment variable and export the PATH in your .zshrc or the bash profile:

```bash
export KEYCLOAK_HOME=PATH_TO_YOUR_KEYCLOAK/keycloak-xx.xx.xx
export PATH=$PATH:$KEYCLOAK_HOME/bin
```

### Prepare the testing data in advance

Log in to the Keycloak server using the kcadm.sh CLI script, which comes with any Keycloak distribution you’ve downloaded in the previous step.

Bad case (password includes special symbols)”

```bash
$KEYCLOAK_HOME/bin/kcadm.sh config credentials --server YOUR_KEYCLOAK_SERVER_URL --realm master --user admin --password raabcZ)eM]N9oM4P
# zsh: parse error near `)'
# Solution: Put the password into "" pair.
```

Normal case (include the password inside the quotation mark pair):

```bash
$ $KEYCLOAK_HOME/bin/kcadm.sh config credentials --server YOUR_KEYCLOAK_SERVER_URL --realm master --user admin --password "rPDbcZ)eM]N9oM4P"
```

Then, please download the [initilaize-benchmark-entities.sh](https://github.com/keycloak/keycloak-benchmark/blob/main/benchmark/src/main/content/bin/initialize-benchmark-entities.sh) from [this page](https://www.keycloak.org/keycloak-benchmark/benchmark-guide/latest/preparing-keycloak). And decide where you want to put the .sh file, here I’ll put the script right under the `$KEYCLOAK_HOME`. Next, please execute the following command to grant permission to the script file:

```bash
$chmod 777 $KEYCLOAK_HOME/initialize-benchmark-entities.sh
```

Next, execute the following command to create the test data in the remote Keycloak server:

```bash
$ initialize-benchmark-entities.sh -r realm_name_you_want_to_use -c gatling -u user-0
```

Please keep the value `gating` and `user-0` as default, since this will be leveraged by the benchmark script. What you can customize here is the realm name.

After executing the command, you should see something like the following block in the console:
```bash
Setting up kcadm.sh in PATH

WARN: not deleting the Client and Realm
Resource not found for url: YOUR_KEYCLOAK_SERVER_URL/admin/realms/benchmark-testing-realm
INFO: Creating Realm with realm id: benchmark-testing-realm

INFO: Created New gatling Client with Client ID c46f49ca-1574-41e5-be88-e20763f24d88
INFO: Created New client-0 Client
Created new user with id '29abe6d8-d4ed-4e66-b70e-ed15d4e3d829'
INFO: Created New user user-0 in benchmark-testing-realm
```

### Set up the benchmark script
Navigate to the keycloak-benchmark repository you downloaded in the first step, move to the ansible directory, then create the env.yml file and write the following content into the file:

```yaml
# Create `env.yml` for the `aws_ec2.sh` and `benchmark.sh` scripts to pick up custom parameters.

# Overrides for AWS EC2. Defaults located in `roles/aws_ec2/defaults/main.yml`.
cluster_size: 5
instance_type: t4g.small
instance_volume_size: 30

# Overrides for Keycloak Benchmark. Defaults located in `roles/benchmark/defaults/main.yml`.

# This will download this version from the GitHub releases
kcb_version: 26.2-SNAPSHOT

# This will use a locally built version
kcb_zip: ../benchmark/target/keycloak-benchmark-26.2-SNAPSHOT.zip

kcb_heap_size: 1G
```

Copy the keycloak-benchmark-xxx-SNAPSHOT.zip you downloaded in the first step to the directory you want and define it in the env.yml you created in the previous step. For example, the above setting will look like screenshot below on the local machine:
  - <img src="https://github.com/user-attachments/assets/ab0f301a-a58a-440c-936e-a252bcea1322" width=300 alt="">

Install Ansible, please adjust the command according to the platform you’re using:

```bash
$ brew install ansible
```

Make sure you’ve already installed and logged in to AWS via the AWS CLI on your machine. Also, make sure the ~/.aws/credentials file has the correct default profile.

Now we need to create the necessary AWS EC2 instance for the testing , please note that the instances will be bound to the IP address of the system that creates them.
  - Notice: When creating the instances, the public IP address of the host creating the machines is added to the EC2 security group, and only this IP address is allowed to log in to the load drivers via SSH. When the public IP address changes, you’ll need to re-run the create command. The public IP address changes, for example, when changing locations or networks, or when the internet connection at home renews the IP address every night. The message displayed when the IP address of the host running Ansible can’t connect is Failed to connect to the host via ssh.

Next, navigate to keycloak-benchmark/ansible, and install the required Ansible AWS collections by the following command:

```bash
$ ./aws_ec2.sh requirements
```

Then, create EC2 instances and related infrastructure. It takes about 5 minutes for this to complete:

```bash
$ ./aws_ec2.sh create <REGION>
```

This will create an Ansible host inventory file and a matching SSH private key to access the hosts:
  - `benchmark_<USERNAME>_<REGION>_inventory.yml`
  - `benchmark_<benchmark_<USERNAME>_<REGION>.pem`

### Perform the benchmark test
Navigate to keycloak-benchmark/ansible, execute the following command:

```bash
$ ./benchmark.sh ap-southeast-1 \
--scenario=keycloak.scenario.authentication.AuthorizationCode \
--server-url=YOUR_KEYCLOAK_SERVER_URL \
--users-per-sec=10 \
--measurement=60 \
--realm-name=benchmark-testing-realm \
--clients-per-realm=1
```

This is just a simple example that only runs for 60 seconds. Here is the explanation of the parameters:

| Parameter             | Description                                                                                                                                                                                                                                                                                                     |
| --------------------- | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `--scenario`          | Defines the Gatling scenario to run. A scenario simulates a specific type of user behavior, such as logging in with client credentials or executing token refreshes. You can find the list of available scenarios [here](https://www.keycloak.org/keycloak-benchmark/benchmark-guide/latest/scenario-overview). |
| `--server-url`        | The base URL of the Keycloak instance you want to test (e.g., the load balancer endpoint or public-facing Keycloak address).                                                                                                                                                                                    |
| `--users-per-sec`     | Number of virtual users to simulate per second. This controls the request load rate generated during the benchmark.                                                                                                                                                                                             |
| `--measurement`       | Duration of the test in seconds. This defines how long the benchmark will run and continuously apply load to the Keycloak server.                                                                                                                                                                               |
| `--realm-name`        | The name of the Keycloak realm to be used in the benchmark. This must match the realm created during initialization.                                                                                                                                                                                            |
| `--clients-per-realm` | Number of client IDs per realm used in the benchmark test. This should correspond to the number of clients generated during test data setup.                                                                                                                                                                    |

In the end of the execution, the log might look like:
```bash
TASK [aws_ec2 : Delete inventory, key, and log] *********************************************************************************************************************
changed: [localhost] => (item=benchmark_clu_ap-northeast-1_inventory.yml) => {"ansible_loop_var": "item", "changed": true, "item": "benchmark_clu_ap-northeast-1_inventory.yml", "path": "benchmark_clu_ap-northeast-1_inventory.yml", "state": "absent"}
changed: [localhost] => (item=benchmark_clu_ap-northeast-1.pem) => {"ansible_loop_var": "item", "changed": true, "item": "benchmark_clu_ap-northeast-1.pem", "path": "benchmark_clu_ap-northeast-1.pem", "state": "absent"}

PLAY RECAP **********************************************************************************************************************************************************
localhost                  : ok=9    changed=4    unreachable=0    failed=0    skipped=2    rescued=0    ignored=0
```

### Check the report
By default, the benchmark testing is implemented by the Gatling load testing framework. You should be able to see the test report under `keycloak-benchmark/ansible/files/benchmark/keycloak-benchmark-xx.xx.xx-SNAPSHOT/results`
- <img src="https://github.com/user-attachments/assets/11467abb-8ede-49e8-850c-23fa316207fa" width=500 alt="">


### Delete EC2 instances and related resources
When no longer needed, delete all EC2 load generator nodes in a region:

```bash
$ ./aws_ec2.sh delete <REGION>
```

This will delete the instances and related resources, and the local inventory file and private key.

This step is critical since if we don’t delete it, we’ll be charged by AWS.

### References
- [Keycloak Benchmark Guide](https://www.keycloak.org/keycloak-benchmark/benchmark-guide/latest/) 
