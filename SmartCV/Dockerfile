# ===================== BUILD STAGE =====================
FROM maven:3.9.6-eclipse-temurin-21-alpine AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn clean package -DskipTests -B

# ===================== RUNTIME STAGE =====================
# Use JRE instead of JDK to save space/RAM
FROM eclipse-temurin:21-jre-jammy

# 1. Install dependencies: Redis, Supervisor
RUN apt-get update && apt-get install -y --no-install-recommends \
    redis-server \
    supervisor \
    wget \
    ca-certificates \
    && rm -rf /var/lib/apt/lists/*

# 2. Install Kafka (KRaft mode supported)
ENV KAFKA_VERSION=3.6.1
ENV SCALA_VERSION=2.13
RUN wget -q https://archive.apache.org/dist/kafka/${KAFKA_VERSION}/kafka_${SCALA_VERSION}-${KAFKA_VERSION}.tgz -O /tmp/kafka.tgz \
    && tar -xzf /tmp/kafka.tgz -C /opt \
    && mv /opt/kafka_${SCALA_VERSION}-${KAFKA_VERSION} /opt/kafka \
    && rm /tmp/kafka.tgz

WORKDIR /app

# 3. Copy application files
COPY --from=build /app/target/SmartCV-0.0.1-SNAPSHOT.jar app.jar
COPY supervisord.conf /app/supervisord.conf
COPY kafka-server.properties /app/kafka-server.properties
COPY start.sh /app/start.sh

# 4. Prepare environment
RUN chmod +x /app/start.sh \
    && mkdir -p /app/kafka-logs \
    && mkdir -p /var/log/supervisor

# 5. Runtime Environment Tuning
ENV PORT=8080
ENV SPRING_PROFILES_ACTIVE=prod
# MALLOC_ARENA_MAX=2 helps limit native memory fragmentation in GLIBC
ENV MALLOC_ARENA_MAX=2

EXPOSE ${PORT}

ENTRYPOINT ["/bin/bash", "/app/start.sh"]