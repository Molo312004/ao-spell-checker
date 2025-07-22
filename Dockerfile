# Stage 1: Build using Maven and JDK 21
FROM maven:3.9.4-eclipse-temurin-21 AS build

WORKDIR /app
COPY pom.xml .
COPY src ./src

RUN mvn clean package -DskipTests

# Stage 2: Run using Java 24
FROM eclipse-temurin:24-jdk

WORKDIR /app
COPY --from=build /app/target/Ao-SpellChecker-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080
CMD ["java", "-jar", "app.jar"]
