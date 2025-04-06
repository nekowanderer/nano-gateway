#!/bin/bash

cd ../spec/gateway-api || { echo "Error: Can not navigate to the target directory: ../spec/gateway-api"; exit 1; }

k6 run --vus 1 --duration 5s -e ECHO_URL=http://localhost:8080/gateway-api/route/simple_api/echo echo.js
