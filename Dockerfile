# ==============================================================================
# VELMART - Multi-Stage Production Dockerfile
# ==============================================================================

# Stage 1: Build JAR with Maven and Java 21
FROM maven:3.9.6-eclipse-temurin-21 AS builder
WORKDIR /workspace

# Copy POM and download dependencies for cache layer
COPY pom.xml .
RUN mvn dependency:go-offline -B || true

# Copy source code and build production package
COPY src ./src
RUN mvn clean package -DskipTests -B

# Stage 2: Minimal Java 21 Runtime Container
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

# Create unprivileged application user
RUN useradd -m -u 1001 velmart
USER velmart

# Copy compiled artifact from build stage
COPY --from=builder /workspace/target/velmart-1.0.0.jar /app/velmart.jar

# Expose standard application port
EXPOSE 8080

# Configure production defaults
ENV SPRING_PROFILES_ACTIVE=prod \
    SERVER_PORT=8080 \
    JAVA_OPTS="-Xms256m -Xmx768m -XX:+UseG1GC"

# Launch Spring Boot Application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/velmart.jar"]
