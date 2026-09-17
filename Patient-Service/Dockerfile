# ---------- Build Stage ----------
FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /app

# Copy pom.xml first to leverage Docker layer caching for dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source and build
COPY src ./src
RUN mvn clean package -DskipTests -B

# ---------- Runtime Stage ----------
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Create a non-root user for security
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Copy the built jar from the build stage
COPY --from=build /app/target/*.jar app.jar

# Change ownership and switch to non-root user
RUN chown -R appuser:appgroup /app
USER appuser

# Expose the service port (adjust if different)
EXPOSE 4000

# Healthcheck (requires Spring Boot Actuator)
HEALTHCHECK --interval=30s --timeout=5s --start-period=30s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:4000/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]