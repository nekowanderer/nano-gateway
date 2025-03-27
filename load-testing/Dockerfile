FROM grafana/k6:latest

WORKDIR /app

USER root

COPY ./spec/routing-api .

CMD ["run", "--vus", "1", "--duration", "5s", "-e", "ECHO_URL=http://routing_api:8080/routing-api/route/simple_api/echo", "echo.js"]
