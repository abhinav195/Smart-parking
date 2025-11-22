# Build stage
FROM eclipse-temurin:21-jdk AS builder
WORKDIR /app

# Copy Maven descriptor and download dependencies (cache-friendly)
COPY pom.xml .
RUN mvn -q -B dependency:go-offline

# Copy sources and build the jar
COPY src ./src
RUN mvn -q -B package -DskipTests

# Run stage
FROM eclipse-temurin:21-jre
WORKDIR /app

# Expose the Spring Boot port
EXPOSE 8081

# Copy the built jar from the builder image
COPY --from=builder /app/target/*.jar app.jar

# Set active profile to 'local' (or 'docker' if you add one)
ENV SPRING_PROFILES_ACTIVE=local

ENTRYPOINT ["java", "-jar", "app.jar"]