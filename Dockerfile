# Start with a Maven image that includes JDK 24 (must be a custom or bleeding-edge image if not yet officially supported)
FROM eclipse-temurin:24-jdk AS builder

WORKDIR /app

# Copy everything
COPY . .

# Make mvnw executable if you're using the Maven wrapper
RUN chmod +x ./mvnw

# Build the app (skip tests to save time in Docker builds)
RUN ./mvnw clean package -DskipTests

# Second stage - run the app in a smaller JDK runtime image
FROM eclipse-temurin:24-jdk

WORKDIR /app

# Copy built JAR from builder stage
COPY --from=builder /app/target/*.jar app.jar

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
