#!/bin/bash
set -e

echo "Starting initialization script..."

# 1. Format Kafka storage for KRaft mode if not already done
if [ ! -f "/app/kafka-formatted" ]; then
    echo "Formatting Kafka storage (KRaft mode)..."
    # Generate a static-but-valid Cluster ID for demo stability
    CLUSTER_ID="4L6GUMxtQZ-A49ZnIDP3vA"
    /opt/kafka/bin/kafka-storage.sh format -t "$CLUSTER_ID" -c /app/kafka-server.properties
    touch /app/kafka-formatted
    echo "Kafka storage formatted."
fi

# 2. Ensure logs directory exists
mkdir -p /app/kafka-logs

# 3. Start supervisord (foreground)
echo "Starting Supervisord..."
exec /usr/bin/supervisord -c /app/supervisord.conf
