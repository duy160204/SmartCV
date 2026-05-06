# ===================== BUILD STAGE =====================
FROM maven:3.9.6-eclipse-temurin-21-alpine AS build

WORKDIR /app

# Cache dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Build application
COPY src ./src
RUN mvn clean package -DskipTests -B

# ===================== RUNTIME STAGE =====================
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Install curl for healthchecks (optional but recommended)
RUN apk add --no-cache curl

# Create non-root user
RUN addgroup -S spring && adduser -S spring -G spring
USER spring

# Copy jar from build stage
COPY --from=build /app/target/SmartCV-0.0.1-SNAPSHOT.jar app.jar

# Standard Render environment variables defaults
ENV PORT=8080
ENV SPRING_PROFILES_ACTIVE=prod
ENV JAVA_OPTS="-Xmx512m -Xms256m"

# Expose port (informative for Render)
EXPOSE 8080

# Run with optimized JVM settings for container environments
# Using sh -c to allow environment variable expansion for $PORT
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Dserver.port=${PORT} -Djava.security.egd=file:/dev/./urandom -jar app.jar"]