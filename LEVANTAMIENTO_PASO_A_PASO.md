# Levantamiento Paso a Paso - Back Logstash Connect
**Proyecto:** Transforma Ecuador
**Cliente:** Claro Ecuador
**Equipo:** Global HITSS
**Fecha:** 2025-09-17
**Agente:** Claude Code - General Purpose Agent

## 🚀 **Comandos de Levantamiento Completo**

### **PASO 1: Preparación del Entorno**
```bash
# 1. Navegar al directorio del proyecto
cd "C:\Users\arauzk\Documents\Proyecto Global Hitss\Kafka"

# 2. Verificar que Docker esté ejecutándose
docker --version
docker-compose --version
```

### **PASO 2: Levantar Infraestructura Base**
```bash
# 3. Levantar servicios base (Kafka, Zookeeper, Elasticsearch)
docker-compose up -d zookeeper kafka elasticsearch

# 4. Esperar 30 segundos para que se estabilicen
timeout /t 30

# 5. Verificar que los servicios estén corriendo
docker-compose ps
```

### **PASO 3: Levantar Servicios de Monitoreo**
```bash
# 6. Levantar Kafka UI y Kibana
docker-compose up -d kafka-ui kibana

# 7. Esperar 20 segundos
timeout /t 20

# 8. Verificar acceso a Kafka UI
echo "Verificar Kafka UI: http://localhost:8081"
```

### **PASO 4: Crear Tópicos Kafka**
```bash
# 9. Crear tópico principal del microservicio
docker exec kafka-transforma-ecuador kafka-topics --create --topic back-logstash-connect --bootstrap-server localhost:9092 --partitions 3 --replication-factor 1

# 10. Crear tópico de eventos generales
docker exec kafka-transforma-ecuador kafka-topics --create --topic Events --bootstrap-server localhost:9092 --partitions 3 --replication-factor 1

# 11. Crear tópico de errores (DLQ)
docker exec kafka-transforma-ecuador kafka-topics --create --topic errors.events --bootstrap-server localhost:9092 --partitions 2 --replication-factor 1

# 12. Listar todos los tópicos creados
docker exec kafka-transforma-ecuador kafka-topics --list --bootstrap-server localhost:9092
```

### **PASO 5: Compilar Microservicio**
```bash
# 13. Entrar al directorio del microservicio
cd microservicio-eventos

# 14. Compilar con Maven (si tienes Maven local)
mvn clean package -DskipTests

# 15. O usar Maven Wrapper (recomendado)
.\mvnw.cmd clean package -DskipTests

# 16. Verificar que se generó el JAR
dir target\back-logstash-connect-1.0.0.jar

# 17. Volver al directorio raíz
cd ..
```

### **PASO 6: Levantar Microservicio**
```bash
# 18. Construir imagen Docker del microservicio
docker-compose build back-logstash-connect

# 19. Levantar el microservicio
docker-compose up -d back-logstash-connect

# 20. Verificar logs del microservicio
docker-compose logs -f back-logstash-connect
```

### **PASO 7: Verificaciones de Funcionamiento**

#### **7.1 Verificar Servicios:**
```bash
# 21. Estado general de todos los servicios
docker-compose ps

# 22. Verificar logs de Kafka
docker-compose logs kafka

# 23. Verificar conectividad de Elasticsearch
curl -X GET "localhost:9200/_cluster/health?pretty"
```

#### **7.2 Verificar Tópicos en Kafka UI:**
```bash
# 24. Abrir Kafka UI en navegador
start http://localhost:8081/ui/clusters/local/all-topics?perPage=25

# Deberías ver:
# ✅ back-logstash-connect (3 particiones)
# ✅ Events (3 particiones)
# ✅ errors.events (2 particiones)
# 🔧 __consumer_offsets (sistema)
```

#### **7.3 Verificar Consumer Groups:**
```bash
# 25. Listar consumer groups
docker exec kafka-transforma-ecuador kafka-consumer-groups --bootstrap-server localhost:9092 --list

# 26. Ver detalles del consumer group del microservicio
docker exec kafka-transforma-ecuador kafka-consumer-groups --bootstrap-server localhost:9092 --describe --group back-logstash-connect-group
```

### **PASO 8: Pruebas de Funcionalidad**

#### **8.1 Test Evento Principal:**
```bash
# 27. Abrir producer para tópico principal
docker exec -it kafka-transforma-ecuador kafka-console-producer --topic back-logstash-connect --bootstrap-server localhost:9092

# 28. Enviar evento JSON (copiar y pegar):
{"eventId":"TEST-001","eventType":"CUSTOMER_CREATED","customerId":"12345","timestamp":"2025-09-17T10:00:00Z","data":{"name":"Juan Perez","email":"juan@test.com"}}

# 29. Presionar Ctrl+C para salir del producer
```

#### **8.2 Test Evento General:**
```bash
# 30. Abrir producer para tópico Events
docker exec -it kafka-transforma-ecuador kafka-console-producer --topic Events --bootstrap-server localhost:9092

# 31. Enviar evento genérico:
{"id":"GEN-001","type":"SYSTEM_ALERT","message":"Prueba de evento general","timestamp":"2025-09-17T10:05:00Z"}

# 32. Presionar Ctrl+C para salir
```

#### **8.3 Verificar Procesamiento:**
```bash
# 33. Ver logs del microservicio en tiempo real
docker-compose logs -f back-logstash-connect

# Deberías ver mensajes como:
# ✅ Recibido evento en topic: back-logstash-connect
# ✅ Evento procesado exitosamente: TEST-001
# ✅ Evento general procesado exitosamente
```

### **PASO 9: Verificar Logs en Elasticsearch**

#### **9.1 Verificar Índices:**
```bash
# 34. Listar índices de Elasticsearch
curl -X GET "localhost:9200/_cat/indices?v"

# 35. Ver logs del microservicio
curl -X GET "localhost:9200/transforma-eventos-logs/_search?pretty&size=10"
```

#### **9.2 Acceder a Kibana:**
```bash
# 36. Abrir Kibana en navegador
start http://localhost:5601

# En Kibana:
# 1. Ir a "Stack Management" > "Index Patterns"
# 2. Crear pattern: transforma-eventos-logs*
# 3. Ir a "Discover" para ver logs
```

### **PASO 10: Monitoreo y Mantenimiento**

#### **10.1 Comandos de Monitoreo:**
```bash
# 37. Ver offsets de consumer group
docker exec kafka-transforma-ecuador kafka-consumer-groups --bootstrap-server localhost:9092 --describe --group back-logstash-connect-group

# 38. Ver mensajes en tópico de errores
docker exec kafka-transforma-ecuador kafka-console-consumer --topic errors.events --bootstrap-server localhost:9092 --from-beginning --timeout-ms 5000

# 39. Estadísticas de tópicos
docker exec kafka-transforma-ecuador kafka-topics --describe --bootstrap-server localhost:9092
```

#### **10.2 Comandos de Parada Controlada:**
```bash
# 40. Parar microservicio solamente
docker-compose stop back-logstash-connect

# 41. Parar todos los servicios
docker-compose down

# 42. Parar y limpiar volúmenes (CUIDADO - borra datos)
docker-compose down -v
```

## 🎯 **URLs de Acceso**

| Servicio | URL | Credenciales |
|----------|-----|--------------|
| **Kafka UI** | http://localhost:8081 | Sin autenticación |
| **Kibana** | http://localhost:5601 | Sin autenticación |
| **Elasticsearch** | http://localhost:9200 | Sin autenticación |
| **Microservicio** | http://localhost:8082 | API REST |

## ✅ **Checklist de Verificación**

- [ ] **Paso 1-2:** Docker funcionando y servicios base levantados
- [ ] **Paso 3:** Kafka UI accesible en puerto 8081
- [ ] **Paso 4:** 4 tópicos visibles (3 aplicación + 1 sistema)
- [ ] **Paso 5:** JAR compilado correctamente
- [ ] **Paso 6:** Microservicio levantado sin errores
- [ ] **Paso 7:** Consumer group "back-logstash-connect-group" activo
- [ ] **Paso 8:** Eventos de prueba procesados exitosamente
- [ ] **Paso 9:** Logs visibles en Elasticsearch/Kibana
- [ ] **Paso 10:** Monitoreo funcionando correctamente

## 🚨 **Solución de Problemas Comunes**

### **Error: "Topic already exists"**
```bash
# Eliminar tópico existente
docker exec kafka-transforma-ecuador kafka-topics --delete --topic NOMBRE_TOPIC --bootstrap-server localhost:9092
```

### **Error: "Connection refused" en Kafka UI**
```bash
# Reiniciar servicios de red
docker-compose restart kafka-ui
docker-compose restart kafka
```

### **Error: Maven build failed**
```bash
# Limpiar cache de Maven y reintentar
docker-compose exec back-logstash-connect rm -rf ~/.m2/repository/*
docker-compose build --no-cache back-logstash-connect
```

### **Error: Elasticsearch no disponible**
```bash
# Verificar salud de Elasticsearch
curl -X GET "localhost:9200/_cluster/health"
# Si falla, reiniciar:
docker-compose restart elasticsearch
```

## 🎪 **Flujo Completo de Datos**

```
Producer → back-logstash-connect (topic) → Consumer → Processing → Elasticsearch
Producer → Events (topic) → Consumer → Generic Processing → Elasticsearch
Error → errors.events (topic) → Consumer → Error Analysis → Alertas
```

## 📊 **Orden de Ejecución Recomendado**

1. **Comandos 1-8:** Preparación y servicios base
2. **Comandos 9-12:** Creación de tópicos
3. **Comandos 13-17:** Compilación
4. **Comandos 18-20:** Despliegue microservicio
5. **Comandos 21-26:** Verificaciones
6. **Comandos 27-33:** Pruebas funcionales
7. **Comandos 34-39:** Verificación final

**Tiempo estimado total:** 15-20 minutos

La documentación está completa y lista para el levantamiento del sistema completo.