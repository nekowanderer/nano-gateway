# HTTP Round Robing API
#### This project is a simple demonstration for the round robing load balancer, it will contains the following components:
- Routing API
  - The load balancer which will face to the end user directly and will dispatch the requests to the backend service behind it.
  - By default, it will adopt the round-robin algorithm for routing the requests.
- Simple API
  - The backend service behind the scene, it will provide some simple endpoints based on HTTP protocol and RESTful style.

## Technical stack
- Java (JDK 17, amazoncorretto-17)
- Quarkus framework
- OkHttp
- JUnit 5
- Mockito
- Maven 3.9.9
- Docker compose
- Bruno/Hoppscotch (For end-to-end testing)

## How to build/debug/test the project (same for all modules)
- For build/test/packaging:
```commandline
./mvnw clean test verify package -Dquarkus.package.type=fast-jar -DskipTests=false
```
- For local testing:
```commandline
./mvnw quarkus:dev -DAVAILABLE_API_INSTANCES=http://localhost:8081,http://localhost:8082,http://localhost:8083
```

## How to run the project
- Clone the repo from GitHub directly.
- Ensure Docker and Docker Compose have already been installed on your machine.
- Then, run the following command right under the round_robing_api folder:
```commandline
docker compose up --build
```
- Which should start the system successfully, it might cost some time to build the container images.

## Requirements
- Simple API
  - A POST method endpoint that responds with a successful response containing an exact copu of the JSON request it received. It's better to implement it with JSON format.
  - We should launch at least 3 instances of simple-api.
- Routing API
  - Can route the HTTP POST requests from the user to the simple-api endpoint with round-robin algorithm and should return the response to the user without modification.
  - Should be able to configure a list which contains the app API instances of simple-api, the round-robing algorithm will dispatch the requests based on this list.
  - Design some mechanisms to deal with the following scenarios:
    - What if one of the simple-api instances goes down?
    - What if one of the simple-api instances becomes slow, no matter timeout/temporarilly unavailable/too busy?
    - How to test the app?

## Solutions
- Implement the app infrastructure with Java + Quarkus Framework.
- Implement the RESTful endpoints with JAX-RS (Jakarta REST).
- Implement the RESTful client with OkHttpClient.
- For launching multiple instances of simple-api, we can directly duplicate the `simple_api_n` service block in the `docker-compose.yml`, make sure the suffix number is updated to the correct one and add the new port number in the `.env` file accordingly (e.g., `SIMPLE_API_N_PORT=xxxx`).
- For configuring the round-robin API list, set the environment variable `AVAILABLE_API_INSTANCES` with comma-separated values in the `.env` file, this variable represents each host of the app, like the following screenshot, please ensure the port here align with the container port inside the `docker-compose.yml` file: 
  - <img src="https://github.com/user-attachments/assets/d71c3515-a62a-4d6e-8660-07d5124682da" width=1000 alt=""> 
- With the instance list configured, make sure it's set in the `routing-api` block of the `docker-compose.yml`:    
  - <img src="https://github.com/user-attachments/assets/7d20359c-2baa-4f6d-af10-68decbb79eeb" width=600 alt="">
- Implement a retry mechanism for handling endpoint temporary unavailability/too busy/low-response rate. 
- Implement a circuit breaker for traffic control and dispatch handling.
- For timeout detection threshold, please adjust the property values of `client.timeout.*` in the application.properties of routing-api. By default, the `client.timeout.read` is `5` seconds (`5000` milliseconds). 

## Unit test report
- Simple API
  - <img src="https://github.com/user-attachments/assets/6b00f9ef-d71f-4c00-afb5-bd942a52af7d" width=1000 alt=""> 
- Routing API
  - <img src="https://github.com/user-attachments/assets/ff1d51e8-dc3e-4860-a5fb-82131ebe5e02" width=1000  alt=""> 
  
## Default endpoint setting
| Service    | Hostname              | Path                                     | Description                                                           |
|------------|-----------------------|------------------------------------------|-----------------------------------------------------------------------|
| routing-api| http://localhost:8080 | /routing-api/route/simple_api/echo       | Redirect to the echo endpoint of simple-api.                          |
| simple-api | http://localhost:xxxx | /simple-api/rest_resource/echo           | Will return the response content which identical to the request body. |
| simple-api | http://localhost:xxxx | /simple-api/rest_resource/delay          | Use this endpoint to set the respnse latency in milliseconds.         |
| simple-api | http://localhost:xxxx | /simple-api/rest_resource/simulate_error | Use this endpoint to simulate the server down scenario.               |
- Please configure the port xxxx via `docker-compose.yml` and `.env` file.

## How to perform the end-to-end test? 
### Set up
- Make sure you have launched the whole docker-compose.yml successfully on your local machine.
- Install [Bruno](https://www.usebruno.com/) API client, it's similar to Postman, but it's free.
  - If you prefer the curl command, feel free to read the content of client-scripts and transform it to curl commands as you need.
- Import the round_robing_api folder under the client-scripts/bruno folder into Bruno on your local machine.
- In the client-scripts folder, you should be able to see the following two folders
  - `bruno/round_robin_api/Routing_API`
    - This is for invoking the endpoints of the routing-api, which means you can interact with the load balancer via the script here. 
  - `bruno/round_robin_api/Simple_API`
    - This is for invoking the dnpoints of simple-api, which is the real backend service we want to access.
### Basic round-robin testing
  - For example, we can open `Route for simple API echo` request in Bruno and send out the post requests, it will send the requests to the simple-api in a round-robin way.
  - <img src="https://github.com/user-attachments/assets/0946602a-9fb2-48a0-8d77-cbbc7676f519" width=1000 alt="">
  - In the docker container console of routing-api, you'll see something like this:
  - <img src="https://github.com/user-attachments/assets/aad33bf2-88fe-49a4-9c4e-6f03157cbe7b" width=1000 alt="">
### Test for endpoints start to go slowly
  - Also, you can invoke the `Set delay` endpoint (simple-api endpoint) with a specified delay number (in millisecond) to specific instance and make it delay according to your setting.
```json lines
{
  "delay": 5000
}
```
  - <img src="https://github.com/user-attachments/assets/9ab18640-e5a6-4ded-a53a-267dd3b93e44" width=1000 alt="">
  - After setting the delay, we can try to test the circuit breaker capability, which will help the routing-api to access some other available endpoints instead of keep accessing the instance that become too slow or even timeout/temporarily unavailable.
  - In the screenshot below, it's clear that the second instance (`port=8082`) takes longer to respond, resulting in a timeout. By default, the `routing-api` has a timeout limit of 5 seconds. When an instance times out, the circuit breaker records its state as `OPEN`, temporarily rerouting all traffic to another available instance. In this example, the traffic is redirected to the next instance using the round-robin algorithm.
  - As long as the circuit breaker is in the `OPEN` state for specific endpoint, you'll see the related log in the routing-api docker console, please refer to the following screenshots:
  - <img src="https://github.com/user-attachments/assets/394171a8-b471-4972-96bd-9d8aeef1c9b2" width=1000 alt="">
### Test for endpoints go down
  - To test it, please invoke the `Simulate error` endpoint (simple-api endpoint) with the following JSON payload example:
```json lines
{
  "serverDown": true,          // means to simulate the server down scenario
  "recoverAfterMillis": 20000  // means the server will recover after 20000 milliseconds
}
```
 - <img src="https://github.com/user-attachments/assets/8bc9ed27-3ab9-4239-a69b-1a4bf3a68a11" width=1000 alt="">
 - For example, the following screenshot is for the scenario that both the endpoint2 and endpoint4 are temporarily unavailable, for these two nodes, the routing-api will try to visit the next available endpoints by round-robin algorithm.
 - <img src="https://github.com/user-attachments/assets/4aace7b2-024b-4dad-9b83-419d5ebaabf4" width=1000 alt="">
 - For conclusion, the `routing-api` has met the requirements according to the preceding test evidence.
