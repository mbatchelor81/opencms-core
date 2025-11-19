# Multi-stage build for Identity Service
FROM maven:3.9-eclipse-temurin-17 AS build

WORKDIR /app

# Copy parent POM and shared modules first for better caching
COPY ../pom.xml ./pom.xml
COPY pom.xml ./identity-service-pom.xml

# Copy source code
COPY identity-api ./identity-api
COPY identity-core ./identity-core
COPY identity-persistence ./identity-persistence
COPY identity-application ./identity-application

# Build the application
RUN mvn clean package -DskipTests -f identity-service-pom.xml

# Runtime stage
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Install curl for healthcheck
RUN apk add --no-cache curl

# Copy the built JAR
COPY --from=build /app/identity-application/target/*.jar app.jar

# Create non-root user
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Expose port
EXPOSE 8081

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD curl -f http://localhost:8081/actuator/health || exit 1

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
