# HTTP Round Robing API
#### This project is a simple demonstration for the round robing load balancer, it will contains the following components:
- Routing API
  - The load balancer which will face to the end user directly and will dispatch the requests to the backend service behind it.
  - By default, it will adopt the round-robin algorithm for routing the requests.
- Simple API
  - The backend service behind the scene, it will provide some simple endpoints based on HTTP protocol and RESTful style.

### Technical stack
- Java (JDK 17, amazoncorretto-17)
- Quarkus framework
- OkHttp
- JUnit 5
- Mockito
- Maven 3.9.9
- Docker compose
- Bruno (For end-to-end testing)

### How to build/debug/test the project (same for all modules)
- For build/test/packaging:
```commandline
./mvnw clean test verify package -Dquarkus.package.type=fast-jar -DskipTests=false
```
- For local testing:
```commandline
./mvnw quarkus:dev -DAVAILABLE_API_INSTANCES=http://localhost:8081,http://localhost:8082,http://localhost:8083
```

### How to run the project
- Clone the repo from GitHub directly.
- Ensure Docker and Docker Compose have already been installed on your machine.
- Then, run the following command right under the round_robing_api folder:
```commandline
docker compose up --build
```
- Which should start the system successfully, it might cost some time to build the container images.

### Requirements
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

### Solutions
- Implement the app infrastructure with Java + Quarkus Framework.
- Implement the RESTful endpoints with JAX-RS (Jakarta REST).
- Implement the RESTful client with OkHttpClient.
- For launching multiple instances of simple-api, we can directly duplicate the `simple_api_n` service block in the `docker-compose.yml`, make sure the suffix number is updated to the correct one and add the new port number in the `.env` file accordingly.
- For configuring the round-robin API list, set the environment variable `AVAILABLE_API_INSTANCES` with comma-separated values in the `.env` file, this variable represents each host of the app, like the following screenshot (Please ensure the port here align with the container port inside the docker-compose.yml file): 
  - <img src="https://github.com/user-attachments/assets/d71c3515-a62a-4d6e-8660-07d5124682da" width=100% height=100%> 
- With the instance list configured, make sure it's set in the `routing-api` block of the `docker-compose.yml`:    
  - <img src="https://github.com/user-attachments/assets/7d20359c-2baa-4f6d-af10-68decbb79eeb" width=60% height=100%>
- Implement a retry mechanism for handling endpoint temporary unavailability/too busy/low-response rate. 
- Implement a circuit breaker for traffic control and dispatch handling.
- For timeout detection threshold, please adjust the property values of `client.timeout.*` in the application.properties of routing-api. By default, the `client.timeout.read` is `5` seconds (`5000` milliseconds). 

### Unit test report
- Simple API
  - <img src="https://github.com/user-attachments/assets/21d7cb67-36e1-40ca-815c-e81be58bd58e" width=100% height=100%> 
- Routing API
  - <img src="https://github.com/user-attachments/assets/e0fae707-e2fa-4e81-bfd8-bd342f19fe9b" width=100% height=100%> 
  
### Default endpoint setting
| Service     | Hostname              | Path     | Description                                                                                                                                      |
|-------------|-----------------------|-----------|--------------------------------------------------------------------------------------------------------------------------------------------------|
| routing-api | http://localhost:8080 | /routing-api/route/simple_api/echo     | Redirect to the echo endpoint of simple-api.                                                                                                     |
| simple-api  | http://localhost:xxxx | /simple-api/rest_resource/echo     | Will return the response content which identical to the request body. |
| simple-api | http://localhost:xxxx | /simple-api/rest_resource/delay | Use this endpoint to set the respnse latency in milliseconds.                                                                                    |
- Please configure the port xxxx via `docker-compose.yml` and `.env` file.

### How to perform the end-to-end test? 
- Make sure you have launched the whole docker-compose.yml successfully on your local machine.
- Install [Bruno](https://www.usebruno.com/) API client, it's similar to Postman, but it's free.
  - If you prefer the curl command, feel free to read the content of client-scripts and transform it to curl commands as you need.
- Import the round_robing_api folder under the client-scripts/bruno folder into Bruno on your local machine.
- In the client-scripts folder, you should be able to see the following two folders
  - `bruno/round_robin_api/Routing_API`
    - This is for invoking the endpoints of the routing-api, which means you can interact with the load balancer via the script here. 
  - `bruno/round_robin_api/Simple_API`
    - This is for invoking the dnpoints of simple-api, which is the real backend service we want to access.
  - For example, we can open `Route for simple API echo` request in Bruno and send out the post requests, it will send the requests to the simple-api in a round-robin way.
    - <img src="https://github.com/user-attachments/assets/0946602a-9fb2-48a0-8d77-cbbc7676f519" width=100% height=100%>
    - In the docker container console of routing-api, you'll see something like this:
    - <img src="https://github.com/user-attachments/assets/aad33bf2-88fe-49a4-9c4e-6f03157cbe7b" width=100% height=100%>
  - Also, you can invoke the `Set delay` endpoint (simple-api endpoint) with a specified delay number (in millisecond) to specific instance and make it delay according to your setting.
    - <img src="https://github.com/user-attachments/assets/9ab18640-e5a6-4ded-a53a-267dd3b93e44" width=100% height=100%>
  - After setting the delay, we can try to test the circuit breaker in the routing-api which will help the routing-api to access some other available endpoints instead of keep accessing the instance that become too slow or even timeout/temporarily unavailable.
    - In the following screenshot, you can observe that the second instance (`port=8082`) takes a longer time for response and thus be recognized as timeout (by default, the timeout limit of routing-api is 5 seconds), once an instance was timeout, the circuit breaker will record the state of the instance as `OPEN` and will temporarily redirect all the traffic to it to another available instance. In the following example, it'll redirect to the next instance in a round-robin manner.
    - <img src="https://github.com/user-attachments/assets/7b7cc79b-cf91-44ba-af76-c08b09ebc183" width=100% height=100%>
  - As long as the circuit breaker is in the `OPEN` state for specific endpoint, you'll see the related log in the routing-api docker console, please refer to the following screenshots:
    - <img src="https://github.com/user-attachments/assets/d38c8901-1a45-4cba-91fa-41d823473eed" width=100% height=100%> 
  - For conclusion, the routing-api has met the requirements according to the preceding test evidence. 

