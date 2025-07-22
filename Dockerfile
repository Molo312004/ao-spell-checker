# Stage 1: Build using Maven and JDK 21
FROM maven:3.9.4-eclipse-temurin-21 AS build

WORKDIR /app
COPY pom.xml .
COPY src ./src
COPY data ./data   

RUN mvn clean package -DskipTests

# Stage 2: Run using Java 24
FROM eclipse-temurin:24-jdk

WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
COPY --from=build /app/data ./data   

EXPOSE 8080
CMD ["java", "-jar", "app.jar"]
