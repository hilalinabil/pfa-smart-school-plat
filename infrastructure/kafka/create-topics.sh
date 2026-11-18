#!/bin/sh
# Creates the Kafka topics from README §5 "Event contracts".
# Idempotent: safe to run on every `docker compose up` (--if-not-exists).
set -eu

BOOTSTRAP="${KAFKA_BOOTSTRAP_SERVERS:-kafka:9092}"
PARTITIONS="${KAFKA_TOPIC_PARTITIONS:-3}"
REPLICATION="${KAFKA_TOPIC_REPLICATION_FACTOR:-1}"

for topic in \
  identity.events \
  user.events \
  document.events \
  document.processing.results \
  request.events \
  assistant.events
do
  /opt/kafka/bin/kafka-topics.sh --bootstrap-server "$BOOTSTRAP" --create --if-not-exists \
    --topic "$topic" --partitions "$PARTITIONS" --replication-factor "$REPLICATION"
done

echo "Topics:"
/opt/kafka/bin/kafka-topics.sh --bootstrap-server "$BOOTSTRAP" --list
