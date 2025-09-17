#!/bin/bash
# Script de configuración para Kafka - Transforma Ecuador
# Autor: Global HITSS
# Cliente: Claro Ecuador

set -e

echo "🚀 Iniciando configuración de Kafka para Transforma Ecuador..."
echo "📋 Cliente: Claro Ecuador"
echo "🏢 Proyecto: Integración Salesforce - Sistemas Legacy"

# Función para esperar que Kafka esté disponible
wait_for_kafka() {
    echo "⏳ Esperando que Kafka esté disponible..."
    while ! kafka-topics --bootstrap-server localhost:9092 --list &>/dev/null; do
        echo "   Kafka aún no está listo, esperando 5 segundos..."
        sleep 5
    done
    echo "✅ Kafka está disponible!"
}

# Función para crear tópicos si no existen
create_topic_if_not_exists() {
    local topic_name=$1
    local partitions=$2
    local replication_factor=$3
    local description=$4

    echo "📝 Verificando tópico: $topic_name"

    if kafka-topics --bootstrap-server localhost:9092 --list | grep -q "^$topic_name$"; then
        echo "   ℹ️  Tópico '$topic_name' ya existe"
    else
        echo "   🆕 Creando tópico '$topic_name' ($description)"
        kafka-topics --create \
            --bootstrap-server localhost:9092 \
            --topic "$topic_name" \
            --partitions "$partitions" \
            --replication-factor "$replication_factor"
        echo "   ✅ Tópico '$topic_name' creado exitosamente"
    fi
}

# Iniciar Kafka en segundo plano
echo "🔧 Iniciando servidor Kafka..."
/etc/confluent/docker/run &
KAFKA_PID=$!

# Esperar que Kafka esté disponible
wait_for_kafka

# Crear tópico para el microservicio
echo "🏗️  Creando tópico para el microservicio..."

create_topic_if_not_exists "microservicio-eventos" 3 1 "Tópico principal del microservicio de eventos"

# Mostrar resumen de tópicos creados
echo ""
echo "📊 Resumen de tópicos disponibles:"
kafka-topics --bootstrap-server localhost:9092 --list | sort | while read topic; do
    if [[ "$topic" != "__"* ]]; then
        partitions=$(kafka-topics --bootstrap-server localhost:9092 --describe --topic "$topic" | grep "PartitionCount" | awk '{print $4}')
        echo "   📌 $topic (${partitions} particiones)"
    fi
done

echo ""
echo "🎉 Configuración de Kafka completada!"
echo "🌐 Kafka disponible en:"
echo "   - Puerto interno: kafka:29092"
echo "   - Puerto externo: localhost:9092"
echo "   - JMX Monitoring: localhost:9999"
echo ""
echo "📈 Listo para recibir eventos de Salesforce para Claro Ecuador"

# Mantener Kafka corriendo
wait $KAFKA_PID