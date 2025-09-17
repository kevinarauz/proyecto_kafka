# Integración Elasticsearch - Transforma Ecuador
**Proyecto:** Microservicio de Eventos Salesforce para Claro Ecuador
**Cliente:** Claro Ecuador
**Equipo:** Global HITSS
**Fecha:** 2025-09-17
**Agente:** Claude Code - General Purpose Agent

## 🔍 Configuración Actual de Elasticsearch

### ✅ **Estado de Elasticsearch:**
- **Versión:** 7.17.0
- **Puerto:** 9200 (funcionando correctamente)
- **Cluster:** `docker-cluster`
- **Nodo:** Único (single-node)
- **Seguridad:** ❌ **DESHABILITADA**
- **Usuario/Password:** ❌ **NO configurado** (acceso abierto)

### 🔒 **Configuración de Seguridad:**
```yaml
# En docker-compose.yml
environment:
  - xpack.security.enabled=false  # Seguridad deshabilitada
```

### 📊 **Índices Actuales:**
```
.geoip_databases                (Kibana - GeoIP)
.kibana_7.17.0_001             (Kibana - Configuración)
.apm-custom-link               (APM - Enlaces personalizados)
.apm-agent-configuration       (APM - Configuración de agentes)
.kibana_task_manager_7.17.0_001 (Kibana - Gestor de tareas)
```

**✅ Estado:** Todos los índices están en **green** (saludables)

## 🚀 **Integración con Microservicio Spring Boot**

### 1. **Dependencias Agregadas:**
```xml
<!-- pom.xml -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-elasticsearch</artifactId>
</dependency>
<dependency>
    <groupId>org.elasticsearch.client</groupId>
    <artifactId>elasticsearch-rest-high-level-client</artifactId>
</dependency>
```

### 2. **Configuración de Conexión:**
```yaml
# application.yml
spring:
  elasticsearch:
    rest:
      uris: http://elasticsearch:9200
      username: # No configurado (acceso abierto)
      password: # No configurado (acceso abierto)
      connection-timeout: 10s
      read-timeout: 10s

elasticsearch:
  host: elasticsearch
  port: 9200
  index:
    name: transforma-eventos-logs
```

### 3. **Clases Implementadas:**

#### 📝 **EventLog.java** - Modelo de datos
```java
@Document(indexName = "transforma-eventos-logs")
public class EventLog {
    private String id;
    private String eventId;
    private String eventType;
    private String level; // INFO, WARN, ERROR
    private String kafkaTopic;
    private Integer kafkaPartition;
    private Long kafkaOffset;
    private String processingStatus; // SUCCESS, ERROR, RETRY
    // ... más campos
}
```

#### 🔍 **EventLogRepository.java** - Repository
```java
@Repository
public interface EventLogRepository extends ElasticsearchRepository<EventLog, String> {
    List<EventLog> findByLevel(String level);
    List<EventLog> findByProcessingStatus(String processingStatus);
    List<EventLog> findByEventType(String eventType);
    // ... más métodos de búsqueda
}
```

#### ⚙️ **ElasticsearchLogService.java** - Servicio
```java
@Service
public class ElasticsearchLogService {
    public void logEventSuccess(...) // Logs de éxito
    public void logEventError(...)   // Logs de errores
    public List<EventLog> getRecentErrors(int hoursBack) // Errores recientes
    // ... más funcionalidades
}
```

## 📋 **Flujo de Logging Implementado**

### Procesamiento Exitoso:
```
Kafka Message → Consumer → Process Event → ✅ Elasticsearch Log (SUCCESS)
```

### Procesamiento con Error:
```
Kafka Message → Consumer → ❌ Error → 🔴 Elasticsearch Log (ERROR)
```

## 🎯 **Índice que se Creará:**

Cuando el microservicio se ejecute, automáticamente creará:

**`transforma-eventos-logs`**
- **Propósito:** Almacenar todos los logs de procesamiento
- **Campos principales:**
  - `eventId`: ID único del evento
  - `eventType`: Tipo de evento Salesforce
  - `level`: INFO, WARN, ERROR
  - `kafkaTopic`: Tópico origen
  - `kafkaPartition`: Partición del mensaje
  - `kafkaOffset`: Offset del mensaje
  - `processingStatus`: SUCCESS, ERROR, RETRY
  - `timestamp`: Marca temporal
  - `originalMessage`: Mensaje JSON original
  - `errorMessage`: Descripción del error (si aplica)

## 🔧 **Comandos Útiles**

### Verificar conectividad:
```bash
curl http://localhost:9200
```

### Listar índices:
```bash
curl http://localhost:9200/_cat/indices?v
```

### Ver logs del microservicio:
```bash
curl "http://localhost:9200/transforma-eventos-logs/_search?pretty"
```

### Buscar logs de errores:
```bash
curl "http://localhost:9200/transforma-eventos-logs/_search?q=level:ERROR&pretty"
```

### Estadísticas del índice:
```bash
curl "http://localhost:9200/transforma-eventos-logs/_stats?pretty"
```

## 🌐 **Acceso via Kibana**

Una vez que el microservicio genere logs, puedes visualizarlos en:

**http://localhost:5601**

1. Ir a **Management** → **Index Patterns**
2. Crear patrón: `transforma-eventos-logs*`
3. Configurar campo de tiempo: `timestamp`
4. Ir a **Discover** para ver los logs en tiempo real

## 📊 **Dashboards Recomendados en Kibana:**

1. **Eventos por Segundo** - Timeline de procesamiento
2. **Errores vs Éxitos** - Ratio de éxito/error
3. **Tipos de Eventos** - Distribución por eventType
4. **Performance por Partición** - Análisis de distribución
5. **Alertas de Errores** - Notificaciones automáticas

## ⚡ **Beneficios de Esta Integración:**

### Para Desarrollo:
- ✅ **Debugging:** Ver exactamente qué eventos fallan
- ✅ **Performance:** Identificar cuellos de botella
- ✅ **Monitoring:** Estadísticas en tiempo real

### Para Producción:
- ✅ **Auditoría:** Rastro completo de procesamiento
- ✅ **Alertas:** Notificación automática de errores
- ✅ **Analytics:** Patrones de comportamiento de eventos

## 🔐 **Recomendaciones de Seguridad:**

**Para Producción:**
1. Habilitar `xpack.security.enabled=true`
2. Configurar usuarios y passwords
3. Usar HTTPS para conexiones
4. Implementar roles y permisos
5. Configurar autenticación en el microservicio

## 🚨 **Estado Actual:**

- ✅ **Elasticsearch:** Funcionando (puerto 9200)
- ✅ **Kibana:** Disponible (puerto 5601)
- ✅ **Microservicio:** Configurado con integración
- ⏳ **Índice de logs:** Se creará al ejecutar microservicio
- ❌ **Seguridad:** Deshabilitada (solo desarrollo)

## 📈 **Próximos Pasos:**

1. Compilar y ejecutar el microservicio
2. Enviar eventos de prueba al tópico `microservicio-eventos`
3. Verificar creación del índice `transforma-eventos-logs`
4. Configurar dashboards en Kibana
5. Implementar alertas para errores críticos