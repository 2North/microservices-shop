FROM gradle:8.5-jdk17 AS builder

ARG SERVICE_NAME

WORKDIR /app
COPY . .

RUN gradle :${SERVICE_NAME}:bootJar --no-daemon

FROM eclipse-temurin:17-jre-alpine

ARG SERVICE_NAME

WORKDIR /app

COPY --from=builder /app/${SERVICE_NAME}/build/libs/${SERVICE_NAME}.jar app.jar

EXPOSE 8080 8081 8082 8083 8084 8085

ENTRYPOINT ["java", "-jar", "app.jar"]
