# Tutorial Kafka UI - Equipo TIC Claro Ecuador

**Proyecto:** TRANSFORMA ECUADOR - Interfaz de Eventos
**Audiencia:** Equipo TIC Claro Ecuador
**Preparado por:** Global HITSS
**Fecha:** 2025-09-17

## Introducción

Kafka UI es la interfaz web para administrar y monitorear Apache Kafka en el proyecto "Transforma Ecuador". Esta herramienta permite visualizar el flujo de eventos entre Salesforce y los sistemas legados sin necesidad de comandos técnicos.

## Acceso a Kafka UI

### URL de Acceso
**Desarrollo:** http://localhost:8081

*Nota: En producción se configurará la URL corporativa correspondiente*

### Credenciales
- **Usuario:** No requiere autenticación en desarrollo
- **Producción:** Se configurará autenticación corporativa

## Navegación Principal

### 1. Dashboard - Vista General
![Dashboard](./docs/dashboard.png)

**Información mostrada:**
- Estado general del cluster Kafka
- Número de brokers activos
- Total de topics configurados
- Métricas de rendimiento en tiempo real

**Para TIC Claro:** Esta vista permite monitorear la salud general del sistema de eventos.

### 2. Topics - Gestión de Canales de Eventos

#### 2.1 Lista de Topics
**Ubicación:** Menú lateral → Topics

**Topics del Proyecto Transforma Ecuador:**
- `Events` - Eventos principales de Salesforce
- `errors.events` - Eventos con errores para logging
- `salesforce.customer` - Datos de clientes
- `salesforce.orders` - Información de órdenes

#### 2.2 Crear Nuevo Topic
1. Click en "Create Topic"
2. **Nombre:** Usar convención `salesforce.{entidad}`
3. **Particiones:** Recomendado 3-6 para balanceo
4. **Factor de Replicación:** 1 (desarrollo), 3 (producción)
5. **Configuración de Retención:**
   - `retention.ms`: 604800000 (7 días)
   - `cleanup.policy`: delete

**Ejemplo de configuración:**
```
Topic Name: salesforce.contracts
Partitions: 3
Replication Factor: 1
retention.ms: 604800000
max.message.bytes: 1048576
```

#### 2.3 Visualizar Mensajes
1. Click en el topic deseado
2. Pestaña "Messages"
3. Configurar filtros:
   - **Offset:** From beginning (para ver todos)
   - **Partition:** All (para ver todas las particiones)
   - **Timestamp:** Rango de fechas específico

**Casos de uso para TIC:**
- Verificar que eventos de Salesforce lleguen correctamente
- Revisar formato de datos en tiempo real
- Debugging de integraciones

### 3. Consumers - Monitoreo de Aplicaciones

#### 3.1 Grupos de Consumidores
**Ubicación:** Menú lateral → Consumers

**Grupos esperados en Transforma Ecuador:**
- `db-sink-group` - Conector hacia base de datos
- `elasticsearch-group` - Conector hacia Elasticsearch
- `legacy-integration-group` - Integración con sistemas legados

#### 3.2 Métricas Importantes
- **Lag:** Retraso en procesamiento de mensajes
- **Offset:** Posición actual de lectura
- **Status:** Estado de conexión del consumidor

**Alertas para TIC:**
- Lag > 1000 mensajes = Revisar conectividad
- Status "DISCONNECTED" = Problema en aplicación
- Offset no avanza = Aplicación detenida

### 4. Brokers - Estado de Servidores

#### 4.1 Información de Brokers
**Ubicación:** Menú lateral → Brokers

**Datos relevantes:**
- **Broker ID:** Identificador único
- **Host:** Dirección IP del servidor
- **Status:** Online/Offline
- **Disk Usage:** Uso de almacenamiento

#### 4.2 Configuración de Cluster
- **Cluster ID:** Identificador único del ambiente
- **Controller:** Broker líder del cluster
- **Versión:** Versión de Kafka instalada

## Operaciones Comunes para TIC

### Verificar Flujo de Eventos Salesforce

#### Paso 1: Monitorear Topic Principal
1. Ir a Topics → `Events`
2. Click en "Messages"
3. Verificar que lleguen mensajes nuevos
4. Revisar estructura JSON de eventos

#### Paso 2: Verificar Procesamiento
1. Ir a Consumers
2. Buscar grupo `db-sink-group`
3. Verificar que Lag sea bajo (< 100)
4. Confirmar que Offset avance

#### Paso 3: Revisar Errores
1. Ir a Topics → `errors.events`
2. Si hay mensajes = Investigar errores
3. Revisar logs de aplicaciones

### Troubleshooting Común

#### Problema: No llegan eventos de Salesforce
**Verificaciones:**
1. Topic `Events` sin mensajes nuevos
2. Verificar conectividad REST Proxy
3. Revisar configuración Salesforce

#### Problema: Lag alto en consumidores
**Verificaciones:**
1. Consumers → Revisar grupo específico
2. Verificar que aplicación esté ejecutándose
3. Revisar recursos del servidor (CPU/Memoria)

#### Problema: Errores en integración
**Verificaciones:**
1. Topic `errors.events` → Revisar mensajes
2. Analizar estructura de error
3. Verificar conectividad base de datos

## Configuración para Producción

### Consideraciones de Seguridad
- Configurar autenticación LDAP corporativa
- Habilitar SSL/TLS
- Restringir acceso por roles:
  - **Lectura:** Monitoreo y consultas
  - **Escritura:** Administración de topics
  - **Admin:** Configuración de cluster

### Alertas Recomendadas
1. **Lag de consumidores > 1000**
2. **Broker offline**
3. **Disk usage > 80%**
4. **Errores en topic errors.events**

### Métricas de SLA
- **Availability:** 99.9% uptime
- **Latency:** < 100ms per message
- **Throughput:** 10,000 msgs/sec
- **Recovery Time:** < 5 minutos

## Contactos de Soporte

### Equipo Global HITSS
- **Kevin Arauz:** arauzk@hitss.com - Arquitectura Kafka
- **Jose Baidal:** baidalj@hitss.com - Integración Salesforce
- **Carlos Rodriguez:** rodriguezcs@hitss.com - Conectores

### Escalamiento
1. **Nivel 1:** Equipo HITSS
2. **Nivel 2:** TIC William Alejandro
3. **Nivel 3:** Soporte Confluent (si aplica)

## Anexos

### Comandos de Respaldo (CLI)
Si Kafka UI no está disponible:

```bash
# Listar topics
docker exec kafka kafka-topics --list --bootstrap-server localhost:9092

# Ver mensajes
docker exec kafka kafka-console-consumer --topic Events --from-beginning --bootstrap-server localhost:9092

# Verificar grupos de consumidores
docker exec kafka kafka-consumer-groups --list --bootstrap-server localhost:9092
```

### URLs de Referencia
- **Documentación Kafka:** https://kafka.apache.org/documentation/
- **Kafka UI GitHub:** https://github.com/provectus/kafka-ui
- **Proyecto en GitHub:** https://github.com/kevinarauz/proyecto_kafka

---

**Documento preparado por Global HITSS para TIC Claro Ecuador**
**Proyecto: Transforma Ecuador - Interfaz de Eventos**