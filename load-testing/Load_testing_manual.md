# Load Testing Manual for nano-gateway

## About Testing Tool
- This project leverages the [Grafana k6](https://k6.io/), which is based on the script written in JavaScript. Please refer to the official site for more information.

## How to Write the Test Script?

Here we can take the `spec/gateway-api/echo.js` as an example:

| Code Block        | Definition                                                                                                                                                                                                                                                                                                                                                                    |
|-------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Import modules    | - http: For sending the HTTP request.</br>- check: To verify whether the response meets the expectations or not.</br>- SharedArray: Share the data among multiple virtual users (vus).</br>- htmlReport/textSummary: For generating the report.</br>                                                                                                                          |
| Load testing data | - Load data from specific source, the example script here will read the data from a csv file.                                                                                                                                                                                                                                                                                 |
| options           | - scenarios: Leveraging the per-vu-iterations executor so each virtual user can perform specific times of tests.</br> - thresholds.http_req_failed: Define the fail rate threshold.</br> - thresholds.http_req_duration: Define the fail threshold for specific percentage levels of requests.</br>- summaryTrendStats: The indicator you want to show up in the report.</br> |
| setup()           | - Perform the initialization logic before the testing.</br>- This will be executed before the default method.</br>- The return value will be leveraged by the `default` method.</br>                                                                                                                                                                                          |
| default()         | - Write your testing logic here.                                                                                                                                                                                                                                                                                                                                              |
| handleSummary()   | - For generating the testing report, in the example here, it'll generate an HTML report.                                                                                                                                                                                                                                                                                      |

Generally, the execution sequence of a k6 script should be:
- Initialization phase
- setup()
- default()
- handleSummary()

## How to Perform the Load Testing Locally?
- First, install k6 by the following command:

```commandline
$ brew install k6
```
Then, check the installation with:

```commandline
$ k6 version
```

In the previous section, you should be able to see several environment variables with the prefix `__ENV` inside the script. Those environment variables could be passed in with the command line.

For the basic command to perform the load testing with k6, here is the general term:

```commandline
$ k6 run --vus VIRTUAL_USER_AMOUNT --duration DURATION_VALUE -e ENV_VAR1 -e ENV_VAR2 ...
```
- `vus`: the integer amount for representing the virtual user amount, e.g., 10.
- `duration`: the value for how long you want to perform the test, e.g., 10m means 10 minutes.
- `i`: the iteration count, this option is an alternative if you don’t want to specify the duration, e.g., 1 means only performs the test for each virtual user for one time without control the total duration time.

For example, if you want to try the `spec/gateway-api/echo.js` testing on the local machine, please follow the instructions below:

Ensure the gateway-api is running, either local or docker-compose instance should be fine.

Navigating to the folder: `spec/gateway-api`, then run the following command:
```commandline
$ k6 run --vus 1 -i 1 -e ECHO_URL=http://127.0.0.1:8080/gateway-api/route/simple_api/echo echo.js
```
This command will only run for one iteration for one virtual user, which is convenient for debugging.

Once the test is finished, you will see the result like:
<img src="https://github.com/user-attachments/assets/c1a0b737-55e6-4212-ad3a-29e9a809da08" width=1000 alt="">

If you want to run the test for a specific duration, please use the following command:
```commandline
k6 run --vus 10 --duration 1m -e ECHO_URL=http://127.0.0.1:8080/gateway-api/route/simple_api/echo echo.js
```
This way is more recommended since it shows you the real-time performance statistics for the real load testing.

The result will look like:
<img src="https://github.com/user-attachments/assets/dc315e19-a48a-465a-9279-83394ce741b2" width=1000 alt="">

Also, you should be able to see the HTML report generated under the same folder with the testing script, for example:
<img src="https://github.com/user-attachments/assets/ee374866-ecf1-4fbc-abf2-ffb2a0a7ed14" width=1000 alt="">

That's it. With k6, now we can handle the load testing easily.