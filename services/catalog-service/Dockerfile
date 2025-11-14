FROM eclipse-temurin:21-jdk
WORKDIR /app

COPY build/libs/catalog-service-0.1.0-SNAPSHOT.jar app.jar

EXPOSE 8081
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
