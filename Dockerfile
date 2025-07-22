# Use official lightweight Java image
FROM openjdk:17-jdk-slim

# Set working directory
WORKDIR /app

# Copy the jar file from build context into the image
COPY target/ao-spell-checker-0.0.1-SNAPSHOT.jar app.jar

# Expose port (optional, helpful for local testing)
EXPOSE 8080

# Run the jar file
ENTRYPOINT ["java", "-jar", "app.jar"]
