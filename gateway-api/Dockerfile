## Stage 1: Build the application
FROM maven:3.9.9-amazoncorretto-17 AS build

WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline

COPY src ./src

RUN mvn clean test verify package -Dquarkus.package.type=fast-jar -DskipTests=false

## Stage 2: The minimal docker image with command to run the application
FROM amazoncorretto:17 AS app_execution

WORKDIR /app

COPY --from=build /app/target/quarkus-app/ /app/

EXPOSE 8080 5005

ENV JAVA_TOOL_OPTIONS "-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005"
ENV QUARKUS_PROFILE container

ENTRYPOINT ["java", "-jar", "/app/quarkus-run.jar"]
