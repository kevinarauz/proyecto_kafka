# Configuración Final de Tópicos - Back Logstash Connect
**Proyecto:** Transforma Ecuador
**Cliente:** Claro Ecuador
**Equipo:** Global HITSS
**Fecha:** 2025-09-17
**Agente:** Claude Code - General Purpose Agent

## 📊 **Tópicos Configurados**

### ✅ **Tópicos Activos:**

#### 1. 🚀 **back-logstash-connect**
- **Propósito:** Tópico principal del microservicio
- **Particiones:** 3
- **Uso:** Eventos específicos del backend de conexión Logstash
- **Consumer:** `SalesforceEventConsumer.consumeMicroserviceEvents()`
- **Tipo de datos:** SalesforceEvent (JSON estructurado)

#### 2. 📊 **Events**
- **Propósito:** Eventos generales del sistema
- **Particiones:** 3
- **Uso:** Eventos genéricos que no requieren estructura específica
- **Consumer:** `SalesforceEventConsumer.consumeGeneralEvents()`
- **Tipo de datos:** JSON genérico (Map<String, Object>)
- **Importante:** ✅ **Fundamental para la arquitectura de eventos**

#### 3. 🚨 **errors.events**
- **Propósito:** Dead Letter Queue (DLQ) - Cola de eventos con errores
- **Particiones:** 2
- **Uso:** Almacenar eventos que fallaron al procesarse
- **Consumer:** `SalesforceEventConsumer.consumeErrorEvents()`
- **Tipo de datos:** JSON de error con metadata
- **Importante:** ✅ **Crítico para manejo de errores y monitoreo**

#### 4. 🔧 **__consumer_offsets**
- **Propósito:** Tópico sistema de Kafka
- **Uso:** Control de offsets de consumer groups
- **Estado:** Manejado automáticamente por Kafka

## 🎯 **Arquitectura de Procesamiento**

### **Flujo Principal:**
```
Producer → back-logstash-connect → SalesforceEvent Processing → Elasticsearch
```

### **Flujo de Eventos Generales:**
```
Producer → Events → Generic Event Processing → Elasticsearch
```

### **Flujo de Manejo de Errores:**
```
Processing Error → errors.events → Error Analysis → Alerts/Metrics
```

## 🔧 **Configuración del Consumer**

### **Consumer Group:** `back-logstash-connect-group`
### **Listeners Configurados:**

```java
@KafkaListener(topics = "back-logstash-connect", groupId = "back-logstash-connect-group")
public void consumeMicroserviceEvents(...)

@KafkaListener(topics = "Events", groupId = "back-logstash-connect-group")
public void consumeGeneralEvents(...)

@KafkaListener(topics = "errors.events", groupId = "back-logstash-connect-group")
public void consumeErrorEvents(...)
```

## 📋 **Funcionalidades por Tópico**

### **back-logstash-connect:**
- ✅ Procesamiento de eventos Salesforce estructurados
- ✅ Validación de datos
- ✅ Logging a Elasticsearch
- ✅ Integración con sistemas legados
- ✅ Manejo de errores con redirección a DLQ

### **Events:**
- ✅ Procesamiento de eventos genéricos JSON
- ✅ Extracción flexible de campos (eventId, eventType, etc.)
- ✅ Transformaciones genéricas
- ✅ Detección de eventos relacionados con clientes/órdenes
- ✅ Manejo de errores con redirección a DLQ

### **errors.events:**
- ✅ Análisis de errores para alertas
- ✅ Métricas de errores por tipo y tópico
- ✅ Detección de errores críticos
- ✅ Lógica de reintento inteligente
- ✅ Generación de alertas automáticas

## 🚨 **¿Por qué son Importantes Events y errors.events?**

### **Events - Flexibilidad:**
- **Eventos diversos:** Puede recibir cualquier tipo de evento JSON
- **Interoperabilidad:** Compatible con diferentes sistemas productores
- **Escalabilidad:** Permite agregar nuevos tipos de eventos sin cambiar código
- **Integración:** Punto de entrada para eventos de múltiples fuentes

### **errors.events - Resiliencia:**
- **Dead Letter Queue:** Evita pérdida de mensajes con errores
- **Análisis de fallos:** Permite identificar patrones de error
- **Alertas proactivas:** Notificación de problemas críticos
- **Reintento inteligente:** Procesamiento de eventos recuperables
- **Monitoreo:** Métricas de salud del sistema

## 🎪 **Casos de Uso Reales:**

### **Scenario 1 - Evento Salesforce Normal:**
```json
Producer → back-logstash-connect
{
  "eventId": "SF-001",
  "eventType": "CUSTOMER_CREATED",
  "customerId": "12345",
  "data": {...}
}
→ Procesamiento exitoso → Elasticsearch
```

### **Scenario 2 - Evento Genérico de Sistema:**
```json
Producer → Events
{
  "id": "SYS-001",
  "type": "SYSTEM_ALERT",
  "message": "Database backup completed",
  "timestamp": "2025-09-17T15:30:00Z"
}
→ Procesamiento genérico → Elasticsearch
```

### **Scenario 3 - Manejo de Error:**
```json
Evento malformado → Procesamiento fallido → errors.events
{
  "timestamp": "2025-09-17T15:30:00",
  "originalMessage": "{malformed json}",
  "topic": "back-logstash-connect",
  "error": "JsonProcessingException",
  "errorType": "JsonProcessingException"
}
→ Análisis de error → Alerta si es crítico
```

## 📊 **Verificación en Kafka UI**

### **URL:** http://localhost:8081/ui/clusters/local/all-topics?perPage=25

### **Tópicos Visibles:**
- ✅ **back-logstash-connect** (3 particiones)
- ✅ **Events** (3 particiones)
- ✅ **errors.events** (2 particiones)
- 🔧 **__consumer_offsets** (sistema)

### **Consumer Groups:**
- ✅ **back-logstash-connect-group** (consumiendo de 3 tópicos)

## 🔧 **Comandos de Verificación**

```bash
# Listar todos los tópicos
docker exec kafka-transforma-ecuador kafka-topics --list --bootstrap-server localhost:9092

# Ver detalles de cada tópico
docker exec kafka-transforma-ecuador kafka-topics --describe --topic back-logstash-connect --bootstrap-server localhost:9092
docker exec kafka-transforma-ecuador kafka-topics --describe --topic Events --bootstrap-server localhost:9092
docker exec kafka-transforma-ecuador kafka-topics --describe --topic errors.events --bootstrap-server localhost:9092

# Ver consumer groups
docker exec kafka-transforma-ecuador kafka-consumer-groups --bootstrap-server localhost:9092 --list

# Ver lag del consumer group
docker exec kafka-transforma-ecuador kafka-consumer-groups --bootstrap-server localhost:9092 --describe --group back-logstash-connect-group
```

## 🚀 **Tests de Funcionalidad**

### **Test 1 - Enviar evento a back-logstash-connect:**
```bash
docker exec kafka-transforma-ecuador kafka-console-producer --topic back-logstash-connect --bootstrap-server localhost:9092
# Enviar: {"eventId":"TEST-001","eventType":"TEST_EVENT","customerId":"12345"}
```

### **Test 2 - Enviar evento genérico a Events:**
```bash
docker exec kafka-transforma-ecuador kafka-console-producer --topic Events --bootstrap-server localhost:9092
# Enviar: {"id":"GEN-001","type":"GENERAL_TEST","message":"Test general event"}
```

### **Test 3 - Verificar DLQ errors.events:**
```bash
docker exec kafka-transforma-ecuador kafka-console-consumer --topic errors.events --bootstrap-server localhost:9092 --from-beginning
# Ver eventos de error que lleguen automáticamente
```

## ✅ **Estado Final**

| Tópico | Particiones | Replicación | Estado | Propósito |
|--------|-------------|-------------|---------|-----------|
| **back-logstash-connect** | 3 | 1 | ✅ Activo | Principal |
| **Events** | 3 | 1 | ✅ Activo | Genérico |
| **errors.events** | 2 | 1 | ✅ Activo | DLQ |
| **__consumer_offsets** | 50 | 1 | ✅ Sistema | Control |

La configuración está completa y robusta para manejar tanto eventos específicos como generales, con un sistema de manejo de errores resiliente.