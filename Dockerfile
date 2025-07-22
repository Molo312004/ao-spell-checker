# --- Use a custom OpenJDK 24 image for building ---
FROM openjdk:24-ea-slim AS build

WORKDIR /app

# Install Maven manually since we’re not using a Maven base image
RUN apt-get update && apt-get install -y maven

# Copy project files
COPY pom.xml .
COPY src ./src

# Build the project (skip tests)
RUN mvn clean package -DskipTests

# --- Use same image for final container ---
FROM openjdk:24-ea-slim

WORKDIR /app

# Copy built JAR from build stage
COPY --from=build /app/target/ao-spell-checker-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
