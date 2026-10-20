# Multi-stage Dockerfile for LearnLoop Spring Boot Application
# Stage 1: Build the application with Maven and Eclipse Temurin 21
FROM maven:3.9.9-eclipse-temurin-21-alpine AS builder

WORKDIR /build

# Cache dependencies
COPY pom.xml .
COPY .mvn .mvn
COPY mvnw .
RUN chmod +x mvnw && ./mvnw dependency:go-offline -B || true

# Copy source code and build production jar
COPY src ./src
RUN ./mvnw clean package -DskipTests -B

# Stage 2: Production Runtime with Eclipse Temurin 21 JRE
FROM eclipse-temurin:21-jre-alpine

LABEL maintainer="LearnLoop Team"
LABEL description="LearnLoop - Production-Ready Learning Management & Doubt-Solving Platform"

# Install curl for container health checks
RUN apk add --no-cache curl tzdata

# Create dedicated non-root application user and group
RUN addgroup -S learnloop && adduser -S -G learnloop learnloop

WORKDIR /app

# Copy executable jar from builder stage
COPY --from=builder /build/target/learnloop-*.jar /app/learnloop.jar

# Set ownership
RUN chown -R learnloop:learnloop /app

# Switch to non-root user
USER learnloop:learnloop

# Configure runtime environment
ENV TZ=UTC \
    JAVA_OPTS="-XX:+UseG1GC -XX:MaxRAMPercentage=75.0 -XX:InitialRAMPercentage=50.0 -Dfile.encoding=UTF-8" \
    PORT=8080

EXPOSE 8080

# Health check against Spring Boot Actuator
HEALTHCHECK --interval=15s --timeout=5s --start-period=30s --retries=3 \
  CMD curl -f http://localhost:${PORT}/actuator/health || exit 1

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -Dserver.port=${PORT:-8080} -jar /app/learnloop.jar"]
