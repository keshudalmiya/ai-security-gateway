# Stage 1: Build application with Gradle
FROM gradle:8.8-jdk21-alpine AS builder
WORKDIR /workspace

# Copy build configuration and sources
COPY gateway/settings.gradle gateway/build.gradle ./
COPY gateway/src ./src

# Build production jar
RUN gradle bootJar --no-daemon -x test

# Stage 2: Minimal hardened runtime image
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

RUN apk add --no-cache curl

# Run as non-root user for security best practices
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser:appgroup

COPY --from=builder /workspace/build/libs/*.jar app.jar

EXPOSE 8080

HEALTHCHECK --interval=10s --timeout=5s --retries=3 \
  CMD curl -f http://localhost:8080/api/v1/health || exit 1

ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]
