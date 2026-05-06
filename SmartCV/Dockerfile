# ===================== BUILD STAGE =====================
FROM maven:3.9.6-eclipse-temurin-21-alpine AS build

WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline -B

COPY src ./src
RUN mvn clean package -DskipTests

# ===================== RUNTIME STAGE =====================
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# tạo user không root (best practice)
RUN addgroup -S spring && adduser -S spring -G spring
USER spring

# copy jar
COPY --from=build /app/target/SmartCV-0.0.1-SNAPSHOT.jar app.jar

# Render port
ENV PORT=8080
ENV SPRING_PROFILES_ACTIVE=prod

EXPOSE 8080

# chạy app với dynamic port
ENTRYPOINT ["sh", "-c", "java -Dserver.port=$PORT -jar app.jar"]