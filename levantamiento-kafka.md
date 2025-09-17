# Documentación de Levantamiento Kafka

**Fecha:** 2025-09-17
**Agente:** Claude Code - Sonnet 4
**Ubicación:** `C:\Users\arauzk\Documents\Proyecto Global Hitss\Kafka`

## Estado del Levantamiento

### ✅ Servicios Exitosos
1. **Zookeeper** - Contenedor: `zookeeper`
   - Estado: UP ✅
   - Puerto: 2181
   - Imagen: confluentinc/cp-zookeeper:7.5.0

2. **Kafka** - Contenedor: `kafka`
   - Estado: UP ✅
   - Puerto: 9092
   - Imagen: confluentinc/cp-kafka:7.5.0
   - Cluster ID: 51KsWnApQuC3zY27Wphjmg

### ✅ Servicios Exitosos (Actualizado)
3. **Kafka UI** - Contenedor: `kafka-ui`
   - Estado: UP ✅
   - Puerto: 8081 (reconfigurado)
   - URL: http://localhost:8081
   - Imagen: provectuslabs/kafka-ui:latest

## Verificación de Conectividad

### Kafka Broker
```bash
# Verificar que Kafka está escuchando
docker-compose logs kafka | grep "started (kafka.server.KafkaServer)"
```

### Logs Importantes
- Kafka se conectó exitosamente a Zookeeper
- Broker ID: 1 configurado correctamente
- Advertised listeners: PLAINTEXT://localhost:9092

## Resolución de Problemas

### Puerto 8080 Ocupado
**Problema:** Kafka UI no puede iniciarse debido a puerto ocupado.

**Soluciones:**
1. **Cambiar puerto en docker-compose.yml:**
   ```yaml
   kafka-ui:
     ports:
       - "8081:8080"  # Cambiar de 8080 a 8081
   ```

2. **Liberar puerto 8080:**
   ```bash
   # Identificar proceso
   netstat -ano | findstr :8080
   # Terminar proceso (reemplazar PID)
   taskkill /PID 5296 /F
   ```

## Comandos de Gestión

### Operaciones Básicas
```bash
# Ver estado
docker-compose ps

# Ver logs en tiempo real
docker-compose logs -f

# Reiniciar servicios
docker-compose restart

# Parar todos los servicios
docker-compose down

# Parar y eliminar volúmenes
docker-compose down -v
```

### Testing de Conectividad
```bash
# Crear topic de prueba
docker exec kafka kafka-topics --create --topic test --bootstrap-server localhost:9092 --partitions 1 --replication-factor 1

# Listar topics
docker exec kafka kafka-topics --list --bootstrap-server localhost:9092
```

## Configuración Actual

### Volúmenes Persistentes
- `kafka_zookeeper-data`
- `kafka_zookeeper-logs`
- `kafka_kafka-data`

### Red
- Red: `kafka_default`
- Comunicación interna entre contenedores habilitada

## Estado Final (Actualizado)
- **Kafka**: ✅ Operativo en puerto 9092
- **Zookeeper**: ✅ Operativo en puerto 2181
- **Kafka UI**: ✅ Operativo en puerto 8081

### URLs de Acceso
- **Kafka Broker:** `localhost:9092`
- **Zookeeper:** `localhost:2181`
- **Kafka UI Web:** `http://localhost:8081`

## Configuración Completada
✅ Todos los servicios operativos
✅ Interfaz web disponible para administración
✅ Topics de prueba creados y validados