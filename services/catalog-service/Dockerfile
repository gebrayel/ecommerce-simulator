FROM gradle:8.10.2-jdk17-alpine AS builder
WORKDIR /workspace

COPY settings.gradle.kts build.gradle.kts gradlew gradlew.bat ./
COPY gradle gradle
COPY libs libs
COPY services/catalog-service services/catalog-service

RUN ./gradlew :services:catalog-service:bootJar --no-daemon --stacktrace -x test

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

ENV JAVA_OPTS="" \
    PORT=8080 \
    SPRING_PROFILES_ACTIVE=prod

COPY --from=builder /workspace/services/catalog-service/build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["/bin/sh","-c","exec java ${JAVA_OPTS} -Dserver.port=${PORT:-8080} -jar /app/app.jar"]

