# Documentación de Tópicos Kafka - Transforma Ecuador
**Proyecto:** Microservicio de Eventos Salesforce para Claro Ecuador
**Cliente:** Claro Ecuador
**Equipo:** Global HITSS
**Fecha:** 2025-09-17
**Agente:** Claude Code - General Purpose Agent

## 📋 Lista Completa de Tópicos

### 1. 🎯 **microservicio-eventos**
- **Tipo:** Principal del proyecto
- **Propósito:** Tópico único para el microservicio Java Spring Boot
- **Particiones:** 3
- **Uso:** Recibe todos los eventos que procesa nuestro microservicio
- **Consumer:** `microservicio-eventos-group`
- **Estado:** ✅ Activo y configurado

### 2. 📊 **Events**
- **Tipo:** Eventos generales del sistema
- **Propósito:** Tópico creado anteriormente para eventos diversos
- **Particiones:** Variable
- **Uso:** Eventos generales que no son específicos de Salesforce
- **Estado:** ✅ Disponible pero no en uso activo

### 3. 🔴 **__consumer_offsets** (Sistema)
- **Tipo:** Tópico interno de Kafka
- **Propósito:** Almacena las posiciones (offsets) de lectura de cada consumer group
- **Función:**
  - Guarda dónde se quedó cada consumidor
  - Permite reanudar desde la última posición tras reinicio
  - Control de duplicados y pérdida de mensajes
- **Particiones:** 50 (por defecto)
- **¿Se puede borrar?:** ❌ NO - Es crítico para Kafka
- **Ejemplo:**
  ```
  Consumer Group: microservicio-eventos-group
  Topic: microservicio-eventos, Partition: 0, Offset: 127
  ```

### 4. ⚠️ **errors.events**
- **Tipo:** Manejo de errores
- **Propósito:** Almacena eventos que fallaron al procesarse
- **Uso:** Dead Letter Queue (cola de cartas muertas)
- **Función:**
  - Mensajes que no se pudieron procesar
  - Errores de formato JSON
  - Fallos de validación
- **Estado:** ✅ Disponible para troubleshooting

### 5. 🧪 **test-topic**
- **Tipo:** Pruebas y desarrollo
- **Propósito:** Tópico para realizar pruebas sin afectar producción
- **Uso:**
  - Enviar mensajes de prueba
  - Validar configuraciones
  - Testing de productores/consumidores
- **Estado:** ✅ Disponible para testing

### 6. 👤 **salesforce.customer** (Legado)
- **Tipo:** Eventos específicos de clientes Salesforce
- **Propósito:** Eventos relacionados con datos de clientes
- **Particiones:** 2
- **Estado:** 🔶 Creado anteriormente, no se usa actualmente
- **Uso anterior:**
  ```json
  {
    "eventType": "CUSTOMER_EVENT",
    "customerId": "001XX000003DHP0",
    "customerData": {...}
  }
  ```

### 7. 📦 **salesforce.orders** (Legado)
- **Tipo:** Eventos de órdenes Salesforce
- **Propósito:** Eventos de órdenes de servicio y productos
- **Particiones:** 2
- **Estado:** 🔶 Creado anteriormente, no se usa actualmente

### 8. 🌟 **salesforce.events** (Legado)
- **Tipo:** Eventos principales de Salesforce
- **Propósito:** Eventos principales del CRM
- **Particiones:** 3
- **Estado:** 🔶 Creado anteriormente, no se usa actualmente

## 🎯 Configuración Actual Simplificada

### Tópico Principal en Uso:
```yaml
Tópico: microservicio-eventos
├── Particiones: 3
├── Replicación: 1
├── Consumer Group: microservicio-eventos-group
└── Microservicio: Spring Boot (puerto 8082)
```

## 🔧 Comandos Útiles

```bash
# Ver detalles de un tópico específico
docker exec kafka-transforma-ecuador kafka-topics --describe --topic microservicio-eventos --bootstrap-server localhost:9092

# Ver offsets de consumer groups
docker exec kafka-transforma-ecuador kafka-consumer-groups --bootstrap-server localhost:9092 --list

# Ver lag de un consumer group
docker exec kafka-transforma-ecuador kafka-consumer-groups --bootstrap-server localhost:9092 --describe --group microservicio-eventos-group

# Enviar mensaje de prueba
docker exec kafka-transforma-ecuador kafka-console-producer --topic test-topic --bootstrap-server localhost:9092
```

## 📊 Estado de Tópicos por Categoría

| Categoría | Tópicos | Estado | Uso Actual |
|-----------|---------|--------|------------|
| **Activos** | microservicio-eventos | ✅ | Principal |
| **Sistema** | __consumer_offsets | ✅ | Automático |
| **Testing** | test-topic, Events | ✅ | Disponible |
| **Errores** | errors.events | ✅ | Monitoring |
| **Legado** | salesforce.* (3 tópicos) | 🔶 | No usado |

## 🎪 Flujo de Datos Actual

```
Producer App → microservicio-eventos → Spring Boot Consumer
                        ↓ (error)
                 errors.events (DLQ)
```

## 💡 Recomendaciones

1. **Limpiar tópicos legado** si no se usan
2. **Monitorear** `__consumer_offsets` para detectar lag
3. **Usar** `test-topic` para pruebas antes de producción
4. **Revisar** `errors.events` para identificar problemas

## 📈 Métricas Relevantes
- **Total tópicos:** 8
- **Tópicos activos:** 1 principal + 3 auxiliares
- **Tópicos sistema:** 1 (__consumer_offsets)
- **Particiones totales:** ~67 (distribuidas)