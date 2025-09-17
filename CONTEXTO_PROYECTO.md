# CONTEXTO DEL PROYECTO - TRANSFORMA ECUADOR

**Cliente:** CLARO ECUADOR
**Proyecto:** TRANSFORMA ECUADOR - Interfaz de Eventos
**Proveedor:** Global HITSS
**Analista:** Claude Code - Sonnet 4
**Fecha:** 2025-09-17

## Propósito del Proyecto

Este proyecto implementa una **interfaz de generación de eventos** para modernizar la integración entre Salesforce y los sistemas legados de Claro Ecuador, utilizando Apache Kafka como broker de mensajería.

## Arquitectura del Sistema

### Flujo de Datos
```
Salesforce → Kafka REST Proxy → Kafka Cluster → [Sink Connector / Logstash] → [DB / Elasticsearch]
```

### Componentes Identificados

#### 1. Infraestructura de Red
- **F5 Load Balancer:** 10.38.2.88:9092
  - Función: Balancear conexiones hacia el cluster Kafka
  - Estado: Pendiente validación

#### 2. Kafka REST Proxy
- **Función:** Recibir POST de Salesforce y publicar en topics
- **Estado:** Pendiente validación

#### 3. Kafka Broker/Cluster
- **Función:** Almacenar eventos
- **Estado:** Pendiente validación

#### 4. Conectores

##### Sink Connector (Base de Datos)
- **Topic origen:** `errors.events`
- **Destino:** Base de datos `dbcri21.conecel.com`
- **Tabla:** `error_events`
- **Estado:** NO existe (requiere implementación)

##### Logstash Connector (Elasticsearch)
- **Topic origen:** `Events`
- **Destino:** Elasticsearch cluster
  - IPs: 10.57.106.55 / 10.57.106.59
  - Puerto: 9243
- **Estado:** Pendiente validación

## Entornos

### Desarrollo/Testing (Dummy)
- **URL:** https://apis.dev.claro.com.ec
- **Requisito:** Acceso a plataforma APIGEE para configuración

### Producción
- **Estado:** Pendiente definición de URLs y accesos

## Participantes del Proyecto

### Claro Ecuador
- **TIC William Alejandro:** walejanv@claro.com.ec
- **TIC Karla Avendaño:** kavendano@claro.com.ec
- **TIC Glenda Choez:** gchoezh@claro.com.ec
- **TIC Orlando Macias:** omacias@claro.com.ec

### Global HITSS
- **Kevin Eduardo Arauz González:** arauzk@hitss.com
- **Jose Aldair Baidal Burgos:** baidalj@hitss.com
- **Carlos Steven Rodriguez Panchana:** rodriguezcs@hitss.com
- **Liliana Sofia Aviles Chacon:** avilesls@hitss.com

### ERP SOL
- **Enrique Alejandro Aguilar Mendoza:** eaguilar@erpsol.com.mx
- **Jerson Rodriguez:** jrodriguez@erpsol.com.mx

## Estado Actual

### Completado ✅
1. Setup básico de Kafka con Docker Compose
2. Configuración de desarrollo local
3. Documentación inicial

### Pendiente ❌
1. **Validación de infraestructura existente** en producción
2. **Implementación del Sink Connector** para errores
3. **Configuración de Logstash Connector**
4. **Acceso a plataforma APIGEE** para entorno de desarrollo
5. **Mapeo completo de eventos** Salesforce → Legado

## Próximos Pasos

1. **Coordinación con TIC William Alejandro** para validar componentes existentes
2. **Solicitud de accesos** a plataforma APIGEE
3. **Implementación de conectores** faltantes
4. **Testing end-to-end** del flujo de eventos
5. **Documentación técnica** detallada para producción

## Consideraciones Técnicas

- El proyecto requiere integración con **infraestructura corporativa existente**
- **No es un setup genérico de Kafka**, sino una solución empresarial específica
- Necesita **coordinación estrecha con equipos de TIC** de Claro
- **Cumplimiento de estándares** de seguridad y gobierno de datos de Claro Ecuador