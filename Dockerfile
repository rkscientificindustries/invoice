# Stage 1: Build the application
FROM eclipse-temurin:25-jdk AS build
WORKDIR /app

# Copy the Gradle wrapper and settings
COPY gradlew .
COPY gradle gradle
COPY build.gradle.kts .
COPY settings.gradle.kts .

# Download dependencies to cache them
RUN chmod +x gradlew && ./gradlew dependencies --no-daemon || true

# Copy the source code
COPY src src

# Build the application
# Use -Pvaadin.productionMode to ensure production frontend bundle is built
RUN ./gradlew bootJar -Pvaadin.productionMode --no-daemon -x test


# Stage 2: Create the runtime image
FROM eclipse-temurin:25-jre
WORKDIR /app

# Create a non-root user for security
RUN groupadd -r appuser && useradd -r -g appuser appuser
USER appuser

# Copy the JAR from the build stage
COPY --from=build /app/build/libs/*.jar app.jar

# Expose the application port
EXPOSE 8080

# Configure the entrypoint
ENTRYPOINT ["java", "-jar", "app.jar"]
