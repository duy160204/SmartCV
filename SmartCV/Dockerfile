# Stage 1: Build Frontend (Optional if already built, but keeping it as a full process)
FROM node:20-slim AS frontend-builder
WORKDIR /app/frontend
# Assuming frontend code is in a directory named 'frontend'
# COPY frontend/package*.json ./
# RUN npm install
# COPY frontend/ ./
# RUN npm run build

# Stage 2: Build Backend
FROM maven:3.9-eclipse-temurin-21 AS backend-builder
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src ./src
RUN mvn package -DskipTests

# Stage 3: Runtime
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

# Copy artifacts
COPY --from=backend-builder /app/target/*.jar app.jar

# Environment variables for Low RAM
ENV MALLOC_ARENA_MAX=2
ENV PORT=8080

EXPOSE ${PORT}

CMD ["java", "-Xms128m", "-Xmx256m", "-XX:MaxMetaspaceSize=96m", "-XX:+UseG1GC", "-Dserver.port=${PORT}", "-Dspring.profiles.active=prod", "-jar", "app.jar"]