FROM gradle:8.10.2-jdk17-alpine AS builder
WORKDIR /workspace

# Copiamos los archivos necesarios para que Gradle resuelva dependencias con caché
COPY settings.gradle.kts build.gradle.kts gradlew gradlew.bat ./
COPY gradle gradle
COPY libs libs

# Copiamos sólo el módulo del microservicio
COPY services/users-service services/users-service

# Construimos el artefacto runnable sin ejecutar tests (ya se ejecutan en CI)
RUN ./gradlew :services:users-service:bootJar --no-daemon --stacktrace -x test

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

ENV JAVA_OPTS="" \
    PORT=8080 \
    SPRING_PROFILES_ACTIVE=prod

COPY --from=builder /workspace/services/users-service/build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["/bin/sh","-c","exec java ${JAVA_OPTS} -Dserver.port=${PORT:-8080} -jar /app/app.jar"]

