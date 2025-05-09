FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app
COPY ../planer-service /planer-service
RUN cd /planer-service && mvn install -DskipTests

FROM openjdk:17-jdk-slim
COPY --from=build /app/target/project-service-0.0.1-SNAPSHOT.jar app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]
