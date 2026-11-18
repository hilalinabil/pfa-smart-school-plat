# syntax=docker/dockerfile:1

# ---------- Build stage ----------
FROM eclipse-temurin:21.0.13_9-jdk-alpine AS build
WORKDIR /workspace

# Dependencies first, for layer caching
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw -B -q dependency:go-offline

# Application (tests run in CI, not in the image build)
COPY src/ src/
RUN ./mvnw -B -q package -DskipTests \
 && java -Djarmode=tools -jar target/*.jar extract --layers --launcher --destination target/extracted

# ---------- Runtime stage ----------
FROM eclipse-temurin:21.0.13_9-jre-alpine
WORKDIR /app

RUN addgroup -S app && adduser -S app -G app

# Least to most frequently changing
COPY --from=build /workspace/target/extracted/dependencies/ ./
COPY --from=build /workspace/target/extracted/spring-boot-loader/ ./
COPY --from=build /workspace/target/extracted/snapshot-dependencies/ ./
COPY --from=build /workspace/target/extracted/application/ ./

USER app

ENV SERVER_PORT=8888 \
    JAVA_OPTS="-XX:MaxRAMPercentage=75 -XX:+ExitOnOutOfMemoryError"
EXPOSE 8888

HEALTHCHECK --interval=15s --timeout=3s --start-period=60s --retries=5 \
  CMD wget -qO- http://localhost:${SERVER_PORT}/actuator/health/readiness || exit 1

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS org.springframework.boot.loader.launch.JarLauncher"]
