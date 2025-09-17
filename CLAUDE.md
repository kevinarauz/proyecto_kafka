# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Architecture Overview

Este es un proyecto de configuración de Apache Kafka usando Docker Compose, diseñado para desarrollo y pruebas. La arquitectura incluye:

- **Kafka Broker** (puerto 9092): Servidor principal de mensajería
- **Zookeeper** (puerto 2181): Coordinador del cluster Kafka (arquitectura tradicional)
- **Kafka UI** (puerto 8081): Interfaz web para administración visual

Los servicios están conectados en una red Docker interna (`kafka_default`) con dependencias configuradas (Kafka depende de Zookeeper, UI depende de Kafka).

## Common Commands

### Gestión de Servicios
```bash
# Levantar todos los servicios
docker-compose up -d

# Ver estado de contenedores
docker-compose ps

# Ver logs en tiempo real
docker-compose logs -f

# Parar servicios (mantiene datos)
docker-compose down

# Parar y eliminar volúmenes (borra datos)
docker-compose down -v
```

### Operaciones con Topics
```bash
# Crear topic
docker exec kafka kafka-topics --create --topic <nombre> --bootstrap-server localhost:9092 --partitions 3 --replication-factor 1

# Listar topics existentes
docker exec kafka kafka-topics --list --bootstrap-server localhost:9092

# Describir topic específico
docker exec kafka kafka-topics --describe --topic <nombre> --bootstrap-server localhost:9092

# Eliminar topic
docker exec kafka kafka-topics --delete --topic <nombre> --bootstrap-server localhost:9092
```

### Testing y Debugging
```bash
# Productor interactivo (escribir mensajes)
docker exec -it kafka kafka-console-producer --topic <nombre> --bootstrap-server localhost:9092

# Consumidor interactivo (leer mensajes)
docker exec -it kafka kafka-console-consumer --topic <nombre> --from-beginning --bootstrap-server localhost:9092

# Ver logs específicos de un servicio
docker-compose logs <servicio>  # servicio: kafka, zookeeper, kafka-ui
```

## Configuration Notes

### Puertos y URLs
- Kafka Broker: `localhost:9092`
- Zookeeper: `localhost:2181`
- Kafka UI: `http://localhost:8081`

### Datos Persistentes
Los volúmenes Docker mantienen datos entre reinicios:
- `kafka_kafka-data`: Datos y logs de Kafka
- `kafka_zookeeper-data`: Datos de Zookeeper
- `kafka_zookeeper-logs`: Logs de Zookeeper

### Configuración de Red
Kafka está configurado con `KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://localhost:9092` para conexiones desde el host. Para conexiones desde otros contenedores usar `kafka:9092`.

## Troubleshooting

### Puerto Ocupado
Si el puerto 8081 está en uso, modificar en `docker-compose.yml`:
```yaml
kafka-ui:
  ports:
    - "8082:8080"  # Cambiar puerto externo
```

### Problemas de Conectividad
1. Verificar que todos los contenedores estén UP: `docker-compose ps`
2. Revisar logs para errores: `docker-compose logs <servicio>`
3. Confirmar que Zookeeper esté listo antes que Kafka inicie

### Reset Completo
Para eliminar todos los datos y empezar desde cero:
```bash
docker-compose down -v
docker-compose up -d
```