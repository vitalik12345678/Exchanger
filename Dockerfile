# ---- Stage 1: Build ----
FROM eclipse-temurin:25-jdk AS builder
WORKDIR /app

# Copy Gradle wrapper and build descriptor first to cache the dependency layer.
# This layer is only invalidated when build files change, not source code.
COPY gradlew .
COPY gradle/ gradle/
COPY build.gradle settings.gradle ./

RUN chmod +x gradlew && ./gradlew dependencies --no-daemon --quiet

# Copy source and produce the fat JAR, skipping tests (they run in CI).
COPY src/ src/
RUN ./gradlew bootJar --no-daemon -x test

# ---- Stage 2: Runtime ----
FROM eclipse-temurin:25-jre AS runtime
WORKDIR /app

# Run as a non-root user.
RUN groupadd --system appgroup && useradd --system --gid appgroup appuser

COPY --from=builder --chown=appuser:appgroup /app/build/libs/*.jar app.jar

USER appuser

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
