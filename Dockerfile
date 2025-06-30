# Dockerfile for EasyTrade Spring Boot Backend

# --- Stage 1: Build Stage ---
# Use an official Gradle image that includes JDK 21. This provides a clean build environment.
FROM gradle:8.8-jdk21 AS build

# Set the working directory inside the container
WORKDIR /home/gradle/src

# Copy the entire project into the container.
# The --chown flag sets the owner to the 'gradle' user, which is a good security practice.
COPY --chown=gradle:gradle . .

# Run the Gradle build command to create the executable JAR.
# This will compile the code and package it. The JAR will be in build/libs/.
# --no-daemon is best practice for CI/CD environments.
# -x test skips running the tests during the build.
RUN ./gradlew build --no-daemon -x test

# --- Stage 2: Final Image Stage ---
# Use a minimal Java Runtime Environment (JRE) image for a smaller, more secure final image.
# 'eclipse-temurin:21-jre-jammy' is a good, standard choice for Java 21.
FROM eclipse-temurin:21-jre-jammy

# Set the working directory for the final application
WORKDIR /app

# Copy ONLY the built JAR file from the 'build' stage into the final image.
# This keeps the final image small, without all the source code and build tools.
# The wildcard * handles the version number in the JAR name automatically.
COPY --from=build /home/gradle/src/build/libs/*.jar app.jar

# Expose the port that Railway will provide via the PORT environment variable.
# Spring Boot will listen on this port.
EXPOSE 8080

# The command to run when the container starts.
# It uses the PORT variable provided by Railway.
ENTRYPOINT ["java", "-Dserver.port=${PORT}", "-jar", "app.jar"]
