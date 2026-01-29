# STAGE 1: BUILD
# ==============================================================================
FROM maven:3.9.6-eclipse-temurin-21-alpine AS build

# Set the working directory inside the container
WORKDIR /app

# 1. Copy only pom.xml first to leverage Docker layer caching for dependencies
COPY pom.xml .

# 2. Download dependencies (This layer will be cached if pom.xml doesn't change)
# 'go-offline' ensures all dependencies are available for offline build
RUN mvn dependency:go-offline -B

# 3. Copy the actual source code
COPY src ./src

# 4. Build the application, skipping tests to speed up the process
# The final artifact will be in /app/target/
RUN mvn clean package -DskipTests

# STAGE 2: RUN
# Use a smaller JRE image for the final runtime (Security & Size optimization)
# ==============================================================================
FROM eclipse-temurin:21-jre-alpine

# Create a non-root user group and user for security
RUN addgroup -S fintrack && adduser -S fintrack -G fintrack

# Set working directory
WORKDIR /app

# Copy the built JAR file from the 'build' stage
# use a wildcard *.jar to handle version changes in pom.xml automatically
COPY --from=build /app/target/*.jar app.jar

# Change ownership of the application file to the non-root user
RUN chown fintrack:fintrack app.jar

# Switch to the non-root user
USER fintrack

# Expose the application port
EXPOSE 8080

# Command to run the application
ENTRYPOINT ["java", "-jar", "app.jar"]