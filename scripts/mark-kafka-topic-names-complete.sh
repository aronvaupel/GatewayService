#!/bin/bash

# Wait for Redis to be available
echo "Waiting for Redis to be ready..."
until redis-cli -h redis ping | grep PONG; do
  sleep 1
done

# Write 'true' to kafka-topic-names-complete
echo "Writing kafka-topic-names-complete = true to Redis"
redis-cli -h redis set kafka-topic-names-complete true
echo "Kafka topic registration marked as complete."
