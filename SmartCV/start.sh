#!/bin/bash
set -e

echo "--- STARTING SMARTCV Sidecar Container (Redis + Spring Boot) ---"

# 1. Start Redis in background for a moment to ensure it's ready
echo "Pre-warming Redis..."
redis-server --daemonize yes --maxmemory 64mb --maxmemory-policy allkeys-lru

# 2. Redis is enough for now, supervisord will take over managing it
redis-cli shutdown

# 3. Start Supervisord (which starts Redis and Spring Boot)
echo "Launching Supervisord..."
exec /usr/bin/supervisord -c /app/supervisord.conf
