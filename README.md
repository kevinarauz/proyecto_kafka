# Proyecto Kafka

Configuración de Apache Kafka con Docker Compose para desarrollo y pruebas.

## Servicios Incluidos

- **Apache Kafka** - Broker de mensajería en puerto 9092
- **Zookeeper** - Coordinador de cluster en puerto 2181
- **Kafka UI** - Interfaz web de administración en puerto 8081

## Inicio Rápido

### 1. Levantar servicios
```bash
docker-compose up -d
```

### 2. Verificar estado
```bash
docker-compose ps
```

### 3. Acceder a Kafka UI
Abrir navegador en: http://localhost:8081

## Comandos Útiles

### Gestión de Topics
```bash
# Crear topic
docker exec kafka kafka-topics --create --topic mi-topic --bootstrap-server localhost:9092 --partitions 3 --replication-factor 1

# Listar topics
docker exec kafka kafka-topics --list --bootstrap-server localhost:9092

# Describir topic
docker exec kafka kafka-topics --describe --topic mi-topic --bootstrap-server localhost:9092
```

### Productor y Consumidor
```bash
# Productor interactivo
docker exec -it kafka kafka-console-producer --topic mi-topic --bootstrap-server localhost:9092

# Consumidor interactivo
docker exec -it kafka kafka-console-consumer --topic mi-topic --from-beginning --bootstrap-server localhost:9092
```

## URLs de Acceso

- **Kafka Broker:** `localhost:9092`
- **Zookeeper:** `localhost:2181`
- **Kafka UI:** http://localhost:8081

## Configuración

### Volúmenes Persistentes
- `kafka_kafka-data` - Datos de Kafka
- `kafka_zookeeper-data` - Datos de Zookeeper
- `kafka_zookeeper-logs` - Logs de Zookeeper

### Variables de Entorno
Ver archivo `docker-compose.yml` para configuración detallada.

## Troubleshooting

### Puerto ocupado
Si el puerto 8081 está ocupado, modificar en `docker-compose.yml`:
```yaml
kafka-ui:
  ports:
    - "8082:8080"  # Cambiar puerto
```

### Logs de servicios
```bash
# Ver logs de todos los servicios
docker-compose logs -f

# Ver logs específicos
docker-compose logs kafka
docker-compose logs zookeeper
docker-compose logs kafka-ui
```

## Limpieza

### Parar servicios
```bash
docker-compose down
```

### Eliminar volúmenes (datos)
```bash
docker-compose down -v
```

## Documentación

- [Documentación de levantamiento](levantamiento-kafka.md)
- [Docker Compose oficial](https://docs.docker.com/compose/)
- [Apache Kafka Docs](https://kafka.apache.org/documentation/)