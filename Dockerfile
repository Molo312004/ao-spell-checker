# Stage 1: Build using Maven and JDK 21
FROM maven:3.9.4-eclipse-temurin-21 AS build

WORKDIR /app
COPY pom.xml .
COPY src ./src
COPY data ./data

RUN mvn clean package -DskipTests

# Stage 2: Run using Java 21 (Render supports Java 21)
FROM eclipse-temurin:21-jdk

WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
COPY --from=build /app/data ./data

# Let the Render platform set the port dynamically
ENV PORT=8080
EXPOSE 8080

# Use environment variables from application.properties or Render dashboard
CMD ["java", "-Dserver.port=${PORT}", "-jar", "app.jar"]
