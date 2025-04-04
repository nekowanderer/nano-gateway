FROM grafana/k6:latest

WORKDIR /app

USER root

COPY ./spec/gateway-api .

CMD ["run", "--vus", "1", "--duration", "5s", "-e", "ECHO_URL=http://gateway_api:8080/gateway-api/route/simple_api/echo", "echo.js"]
