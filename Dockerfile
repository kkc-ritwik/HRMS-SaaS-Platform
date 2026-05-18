# ─── Universal Dockerfile for any HRMS service ──────────────────────────────
# Multi-stage build. Usage:
#   docker build --build-arg SERVICE=service-auth -t hrms/service-auth:1.0 .
# The build context must be the repo root (so the multi-module reactor works).

# ----- Build stage -----
FROM maven:3.9-eclipse-temurin-17 AS builder

WORKDIR /workspace
COPY . .

ARG SERVICE
RUN test -n "$SERVICE" || (echo "ERROR: --build-arg SERVICE=<module-name> is required" && exit 1)

# Build only the requested service + its required commons in dependency order.
RUN mvn -B -pl ${SERVICE} -am -DskipTests=true clean package

# ----- Runtime stage -----
FROM eclipse-temurin:17-jre-alpine

ARG SERVICE
ENV SERVICE_NAME=${SERVICE}

RUN addgroup -S hrms && adduser -S hrms -G hrms

WORKDIR /app
COPY --from=builder /workspace/${SERVICE}/target/*.jar /app/app.jar

# Default JVM tuning — overrideable via env
ENV JAVA_OPTS="-XX:+UseG1GC -XX:MaxRAMPercentage=75.0 -Djava.security.egd=file:/dev/./urandom"
ENV SPRING_PROFILES_ACTIVE=prod

USER hrms

EXPOSE 8080
HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
    CMD wget -q -O - http://localhost:${PORT:-8080}/actuator/health || exit 1

ENTRYPOINT ["sh","-c","java $JAVA_OPTS -jar /app/app.jar"]
