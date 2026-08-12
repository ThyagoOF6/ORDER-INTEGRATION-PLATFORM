# Build stage
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /build

COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .
COPY gradlew .
COPY gradlew.bat .

# Download dependencies
RUN ./gradlew dependencies --no-daemon

# Copy source code
COPY . .

# Build application
RUN ./gradlew bootJar -p bootstrap --no-daemon

# Runtime stage
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Create non-root user
RUN addgroup -g 1000 appuser && adduser -D -u 1000 -G appuser appuser

COPY --from=builder /build/bootstrap/build/libs/order-integration-platform.jar app.jar

RUN chown appuser:appuser app.jar

USER appuser

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
