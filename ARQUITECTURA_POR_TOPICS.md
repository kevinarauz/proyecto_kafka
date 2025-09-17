# Arquitectura por Topics - Transforma Ecuador
**Proyecto:** Transforma Ecuador
**Cliente:** Claro Ecuador
**Equipo:** Global HITSS
**Fecha:** 2025-09-17
**Agente:** Claude Code - General Purpose Agent

## 🏗️ **Nueva Arquitectura Separada por Topics**

### **Principio de Diseño:**
Cada topic tiene su propio microservicio especializado con carpeta, Dockerfile y configuración independiente.

## 📁 **Estructura del Proyecto**

```
Kafka/
├── docker-compose.yml                          # Orquestación de 3 microservicios + infra
├── back-logstash-connect/                      # 🔗 Microservicio 1
│   ├── Dockerfile                              # Puerto 8082
│   ├── pom.xml                                 # back-logstash-connect-1.0.0.jar
│   └── src/main/java/com/claro/ecuador/logstash/
│       ├── BackLogstashConnectApplication.java
│       └── consumer/LogstashConnectConsumer.java
├── events-processor/                           # 📊 Microservicio 2
│   ├── Dockerfile                              # Puerto 8083
│   ├── pom.xml                                 # events-processor-1.0.0.jar
│   └── src/main/java/com/claro/ecuador/events/
│       ├── EventsProcessorApplication.java
│       └── consumer/GeneralEventsConsumer.java
├── errors-handler/                             # 🚨 Microservicio 3
│   ├── Dockerfile                              # Puerto 8084
│   ├── pom.xml                                 # errors-handler-1.0.0.jar
│   └── src/main/java/com/claro/ecuador/errors/
│       ├── ErrorsHandlerApplication.java
│       └── consumer/ErrorEventsConsumer.java
└── [archivos de documentación y configuración]
```

## 🎯 **Microservicios por Topic**

### **1. 🔗 back-logstash-connect**
- **Topic:** `back-logstash-connect`
- **Puerto:** 8082
- **Propósito:** Conexión específica entre Kafka y Logstash
- **Consumer Group:** `back-logstash-connect-group`
- **Responsabilidades:**
  - Procesar eventos para pipeline Logstash
  - Formatear datos para compatibilidad Logstash
  - Validar estructura para Elasticsearch vía Logstash
  - Aplicar filtros específicos de Logstash

### **2. 📊 events-processor**
- **Topic:** `Events`
- **Puerto:** 8083
- **Propósito:** Procesamiento de eventos generales del sistema
- **Consumer Group:** `events-processor-group`
- **Responsabilidades:**
  - Procesar eventos JSON genéricos
  - Clasificar por tipo (SYSTEM_ALERT, USER_ACTION, etc.)
  - Aplicar transformaciones genéricas
  - Integrar con sistemas de métricas
  - Procesar notificaciones del sistema

### **3. 🚨 errors-handler**
- **Topic:** `errors.events`
- **Puerto:** 8084
- **Propósito:** Dead Letter Queue y manejo de errores
- **Consumer Group:** `errors-handler-group`
- **Responsabilidades:**
  - Analizar eventos de error para alertas
  - Clasificar severidad (CRITICAL, HIGH, MEDIUM, LOW)
  - Generar alertas por email automáticas
  - Aplicar lógica de reintentos inteligentes
  - Métricas de errores y monitoreo de salud

## 🔄 **Flujos de Datos**

### **Flujo 1: Conexión Logstash**
```
Producer → back-logstash-connect (topic) → back-logstash-connect (service:8082) → Logstash Pipeline → Elasticsearch
```

### **Flujo 2: Eventos Generales**
```
Producer → Events (topic) → events-processor (service:8083) → Procesamiento Genérico → Elasticsearch
```

### **Flujo 3: Dead Letter Queue**
```
Error en procesamiento → errors.events (topic) → errors-handler (service:8084) → Análisis + Alertas + Reintentos
```

## 🐳 **Configuración Docker Compose**

### **Red Kafka:**
- Red dedicada: `kafka-network`
- Comunicación interna entre servicios
- Aislamiento de tráfico Kafka

### **Puertos Expuestos:**
| Servicio | Puerto | Propósito |
|----------|---------|----------|
| **Kafka** | 9092 | Bootstrap servers (externo) |
| **Kafka UI** | 8081 | Administración web |
| **Elasticsearch** | 9200 | API REST |
| **Kibana** | 5601 | Visualización logs |
| **back-logstash-connect** | 8082 | API microservicio 1 |
| **events-processor** | 8083 | API microservicio 2 |
| **errors-handler** | 8084 | API microservicio 3 |

### **Variables de Entorno por Microservicio:**

#### **back-logstash-connect:**
```yaml
SPRING_KAFKA_BOOTSTRAP_SERVERS: kafka:29092
SPRING_KAFKA_CONSUMER_GROUP_ID: back-logstash-connect-group
SPRING_APPLICATION_NAME: back-logstash-connect
SPRING_ELASTICSEARCH_REST_URIS: http://elasticsearch:9200
```

#### **events-processor:**
```yaml
SPRING_KAFKA_BOOTSTRAP_SERVERS: kafka:29092
SPRING_KAFKA_CONSUMER_GROUP_ID: events-processor-group
SPRING_APPLICATION_NAME: events-processor
SPRING_ELASTICSEARCH_REST_URIS: http://elasticsearch:9200
```

#### **errors-handler:**
```yaml
SPRING_KAFKA_BOOTSTRAP_SERVERS: kafka:29092
SPRING_KAFKA_CONSUMER_GROUP_ID: errors-handler-group
SPRING_APPLICATION_NAME: errors-handler
SPRING_ELASTICSEARCH_REST_URIS: http://elasticsearch:9200
SPRING_MAIL_HOST: smtp.gmail.com
ERROR_HANDLER_ALERT_CRITICAL_THRESHOLD: 10
```

## ⚙️ **Comandos de Levantamiento**

### **Infraestructura Base:**
```bash
docker-compose up -d zookeeper kafka elasticsearch kibana kafka-ui
```

### **Microservicios Individuales:**
```bash
# Microservicio 1
docker-compose up -d back-logstash-connect

# Microservicio 2
docker-compose up -d events-processor

# Microservicio 3
docker-compose up -d errors-handler
```

### **Todos los Microservicios:**
```bash
docker-compose up -d back-logstash-connect events-processor errors-handler
```

## 🔧 **Compilación Individual**

### **back-logstash-connect:**
```bash
cd back-logstash-connect
./mvnw clean package -DskipTests
docker build -t back-logstash-connect:1.0.0 .
```

### **events-processor:**
```bash
cd events-processor
./mvnw clean package -DskipTests
docker build -t events-processor:1.0.0 .
```

### **errors-handler:**
```bash
cd errors-handler
./mvnw clean package -DskipTests
docker build -t errors-handler:1.0.0 .
```

## 📊 **Verificación en Kafka UI**

### **Consumer Groups Esperados:**
- `back-logstash-connect-group` → Topic: `back-logstash-connect`
- `events-processor-group` → Topic: `Events`
- `errors-handler-group` → Topic: `errors.events`

### **URL de Verificación:**
http://localhost:8081/ui/clusters/local/consumer-groups

## 🚨 **Beneficios de la Nueva Arquitectura**

### **1. Separación de Responsabilidades:**
- Cada microservicio tiene una función específica
- Fácil mantenimiento y debugging
- Escalabilidad independiente

### **2. Desarrollo Paralelo:**
- Equipos pueden trabajar en topics diferentes simultáneamente
- Releases independientes por microservicio
- Testing aislado por funcionalidad

### **3. Tolerancia a Fallos:**
- Falla de un microservicio no afecta otros
- Reinicio independiente
- Monitoreo granular

### **4. Configuración Especializada:**
- Configuraciones específicas por tipo de evento
- Recursos (CPU/memoria) ajustados por carga
- Alertas personalizadas por criticidad

## 🎪 **Casos de Uso por Microservicio**

### **back-logstash-connect - Casos de Uso:**
- Eventos Salesforce → Logstash → Elasticsearch
- Logs de aplicación → Formateo Logstash → Índices ELK
- Datos transaccionales → Pipeline estructurado

### **events-processor - Casos de Uso:**
- Alertas del sistema → Clasificación → Notificaciones
- Métricas de aplicación → Agregación → Dashboard
- Eventos de usuario → Auditoría → Reportes

### **errors-handler - Casos de Uso:**
- JSON malformado → Análisis → Reintento/Descarte
- Errores críticos → Alerta inmediata → Escalamiento
- Errores de conectividad → Backoff → Reintento automático

## ✅ **Migración Completada**

### **Estado Anterior:**
- 1 microservicio monolítico
- Todos los topics en mismo consumer
- Configuración centralizada

### **Estado Actual:**
- 3 microservicios especializados
- 1 topic por microservicio
- Configuraciones independientes
- Escalabilidad granular

### **Próximos Pasos:**
1. Crear topics en Kafka
2. Compilar y levantar microservicios
3. Verificar consumer groups en Kafka UI
4. Realizar pruebas de funcionalidad por topic
5. Configurar alertas de email en errors-handler

La arquitectura está lista para despliegue y operación distribuida por topics.