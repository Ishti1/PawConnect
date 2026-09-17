# Build stage
FROM gradle:8-jdk17 AS build
WORKDIR /app
# Copy the gradle files first for better caching
COPY build.gradle.kts settings.gradle.kts gradlew ./
COPY gradle ./gradle
# Copy the source code
COPY src ./src
# Build the application (skipping tests for faster deployment)
RUN gradle build --no-daemon -x test

# Run stage
FROM eclipse-temurin:17-jre
WORKDIR /app
# Copy the built jar from the build stage
COPY --from=build /app/build/libs/*.jar app.jar
# Expose port 8080
EXPOSE 8080
# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
