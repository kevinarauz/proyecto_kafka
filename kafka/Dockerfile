# Dockerfile personalizado para Kafka - Proyecto Transforma Ecuador
FROM confluentinc/cp-kafka:7.5.0

# Metadata del contenedor
LABEL maintainer="Global HITSS <arauzk@hitss.com>"
LABEL project="Transforma Ecuador"
LABEL client="Claro Ecuador"
LABEL component="Apache Kafka"
LABEL version="7.5.0"

# Variables de entorno para configuración de Kafka
ENV KAFKA_BROKER_ID=1
ENV KAFKA_ZOOKEEPER_CONNECT=zookeeper:2181
ENV KAFKA_LISTENER_SECURITY_PROTOCOL_MAP=PLAINTEXT:PLAINTEXT,PLAINTEXT_INTERNAL:PLAINTEXT
ENV KAFKA_LISTENERS=PLAINTEXT://0.0.0.0:9092,PLAINTEXT_INTERNAL://0.0.0.0:29092
ENV KAFKA_ADVERTISED_LISTENERS=PLAINTEXT://localhost:9092,PLAINTEXT_INTERNAL://kafka:29092
ENV KAFKA_INTER_BROKER_LISTENER_NAME=PLAINTEXT_INTERNAL
ENV KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR=1
ENV KAFKA_TRANSACTION_STATE_LOG_MIN_ISR=1
ENV KAFKA_TRANSACTION_STATE_LOG_REPLICATION_FACTOR=1
ENV KAFKA_LOG_CLEANUP_POLICY=delete
ENV KAFKA_LOG_RETENTION_HOURS=168
ENV KAFKA_LOG_RETENTION_BYTES=1073741824
ENV KAFKA_LOG_SEGMENT_BYTES=1073741824
ENV KAFKA_AUTO_CREATE_TOPICS_ENABLE=true
ENV KAFKA_DEFAULT_REPLICATION_FACTOR=1
ENV KAFKA_NUM_PARTITIONS=3

# Configuraciones específicas para el proyecto
ENV KAFKA_GROUP_INITIAL_REBALANCE_DELAY_MS=0
ENV KAFKA_JMX_PORT=9999
ENV KAFKA_JMX_HOSTNAME=localhost

# Crear directorio para scripts personalizados en home del usuario
RUN mkdir -p /home/appuser/scripts

# Script para crear tópicos automáticamente
COPY kafka-setup.sh /home/appuser/scripts/
RUN chmod +x /home/appuser/scripts/kafka-setup.sh

# Crear directorio de datos
RUN mkdir -p /var/lib/kafka/data
VOLUME ["/var/lib/kafka/data"]

# Exponer puertos
EXPOSE 9092 29092 9999

# Healthcheck personalizado
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD kafka-topics --bootstrap-server localhost:9092 --list || exit 1

# Comando de inicio que ejecuta setup y luego kafka
CMD ["/home/appuser/scripts/kafka-setup.sh"]