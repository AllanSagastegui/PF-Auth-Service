# ==============================================================================
# Multi-Stage Dockerfile for Auth Service (Spring Boot 4.x / Java 21)
# Optimized for caching, layer separation, non-root security, and minimal footprint
# ==============================================================================

# ------------------------------------------------------------------------------
# Stage 1: Build & Compile
# ------------------------------------------------------------------------------
FROM eclipse-temurin:21-jdk-jammy AS builder

WORKDIR /workspace

# Step 1.1: Copy Gradle build configuration and wrapper
COPY gradlew gradlew.bat settings.gradle.kts build.gradle.kts gradle.properties lombok.config sonar.properties ./
COPY gradle ./gradle

# Step 1.2: Grant execute permissions to the Gradle wrapper
RUN chmod +x gradlew

# Step 1.3: Copy source code for all modules
COPY auth-service-app ./auth-service-app
COPY auth-service-core ./auth-service-core
COPY auth-service-input ./auth-service-input
COPY auth-service-output ./auth-service-output

# Step 1.4: Compile and generate the executable BootJar
# Unit/Architecture tests run in the CI/CD quality gate; skipping here optimizes build time
ARG SKIP_TESTS=true
RUN if [ "$SKIP_TESTS" = "true" ]; then \
        ./gradlew :app:bootJar --no-daemon --stacktrace -x test; \
    else \
        ./gradlew :app:bootJar --no-daemon --stacktrace; \
    fi

# ------------------------------------------------------------------------------
# Stage 2: Layer Extraction (Spring Boot Tools Mode)
# ------------------------------------------------------------------------------
FROM eclipse-temurin:21-jre-jammy AS extractor

WORKDIR /build-tools

# Copy the built uber-jar from the builder stage
COPY --from=builder /workspace/auth-service-app/build/libs/auth-service.jar app.jar

# Extract Spring Boot layers (dependencies, loader, snapshot-dependencies, application) into /extracted
RUN java -Djarmode=tools -jar app.jar extract --layers --launcher --destination /extracted --force

# ------------------------------------------------------------------------------
# Stage 3: Runtime
# ------------------------------------------------------------------------------
FROM eclipse-temurin:21-jre-jammy AS runtime

# Security: Install curl for container healthcheck, remove package lists, and create non-root user
RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/* \
    && addgroup --system --gid 10001 authgroup \
    && adduser --system --uid 10001 --ingroup authgroup --home /app --no-create-home authuser

WORKDIR /app

# Copy extracted layers in order of change frequency (Dependencies change rarely, App often)
COPY --from=extractor --chown=10001:10001 /extracted/dependencies/ ./
COPY --from=extractor --chown=10001:10001 /extracted/spring-boot-loader/ ./
COPY --from=extractor --chown=10001:10001 /extracted/snapshot-dependencies/ ./
COPY --from=extractor --chown=10001:10001 /extracted/application/ ./

# Switch to the non-privileged user
USER 10001:10001

# Standard HTTP & OpenAPI port
EXPOSE 9090

# JVM Container-Aware Best Practices
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75.0 \
-XX:InitialRAMPercentage=50.0 \
-XX:+ExitOnOutOfMemoryError \
-XX:+UseG1GC \
-Djava.security.egd=file:/dev/./urandom"

# Container Healthcheck using SpringDoc OpenAPI documentation endpoint
HEALTHCHECK --interval=15s --timeout=5s --start-period=25s --retries=3 \
    CMD curl -f http://localhost:${PORT:-9090}/v3/api-docs || exit 1

# Launch with Spring Boot's modern JarLauncher
ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]
