package com.claro.ecuador.events.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Consumer específico para topic: Events
 * Maneja únicamente eventos generales del sistema
 *
 * @author Global HITSS
 */
@Component
public class GeneralEventsConsumer {

    private static final Logger logger = LoggerFactory.getLogger(GeneralEventsConsumer.class);

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Consumer exclusivo para topic Events
     * Propósito: Procesar eventos generales del sistema (JSON genérico)
     */
    @KafkaListener(topics = "Events", groupId = "events-processor-group")
    public void consumeGeneralEvents(@Payload String message,
                                   @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                                   @Header(KafkaHeaders.RECEIVED_PARTITION_ID) int partition,
                                   @Header(KafkaHeaders.OFFSET) long offset) {
        try {
            logger.info("📊 EVENTS PROCESSOR - Recibido evento general en topic: {}, partition: {}, offset: {}",
                       topic, partition, offset);
            logger.debug("Mensaje: {}", message);

            // Procesamiento específico para eventos generales
            processGeneralEvent(message);

            logger.info("✅ EVENTS PROCESSOR - Evento general procesado exitosamente - Offset: {}", offset);

        } catch (Exception e) {
            logger.error("❌ EVENTS PROCESSOR - Error procesando evento general en topic {}: {}",
                        topic, e.getMessage(), e);

            // Manejo de errores específico para eventos generales
            handleGeneralEventError(message, topic, e);
        }
    }

    /**
     * Lógica específica para eventos generales
     */
    private void processGeneralEvent(String message) throws Exception {
        try {
            logger.info("🔧 Procesando evento general del sistema");

            // Parsear como JSON genérico
            Map<String, Object> eventData = objectMapper.readValue(message, Map.class);

            // Extraer campos comunes de eventos generales
            String eventId = extractEventId(eventData);
            String eventType = extractEventType(eventData);
            String source = extractSource(eventData);

            logger.info("📋 Evento general - ID: {}, Tipo: {}, Fuente: {}", eventId, eventType, source);

            // Procesamiento específico según tipo de evento general
            switch (eventType.toUpperCase()) {
                case "SYSTEM_ALERT":
                    processSystemAlert(eventData);
                    break;
                case "USER_ACTION":
                    processUserAction(eventData);
                    break;
                case "SYSTEM_METRIC":
                    processSystemMetric(eventData);
                    break;
                case "NOTIFICATION":
                    processNotification(eventData);
                    break;
                default:
                    processUnknownEvent(eventData);
            }

            // Enviar a almacenamiento genérico
            storeGeneralEvent(eventData);

        } catch (Exception e) {
            logger.error("Error en procesamiento de evento general: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Procesar alertas del sistema
     */
    private void processSystemAlert(Map<String, Object> eventData) {
        logger.info("🚨 Procesando alerta del sistema");

        String severity = (String) eventData.getOrDefault("severity", "INFO");
        String message = (String) eventData.getOrDefault("message", "Sin mensaje");

        logger.info("Alerta - Severidad: {}, Mensaje: {}", severity, message);

        if ("CRITICAL".equals(severity) || "ERROR".equals(severity)) {
            triggerCriticalAlert(eventData);
        }
    }

    /**
     * Procesar acciones de usuario
     */
    private void processUserAction(Map<String, Object> eventData) {
        logger.info("👤 Procesando acción de usuario");

        String userId = (String) eventData.getOrDefault("userId", "unknown");
        String action = (String) eventData.getOrDefault("action", "unknown");

        logger.info("Acción - Usuario: {}, Acción: {}", userId, action);

        // Auditoría de acciones de usuario
        auditUserAction(eventData);
    }

    /**
     * Procesar métricas del sistema
     */
    private void processSystemMetric(Map<String, Object> eventData) {
        logger.info("📊 Procesando métrica del sistema");

        String metricName = (String) eventData.getOrDefault("metricName", "unknown");
        Object value = eventData.getOrDefault("value", 0);

        logger.info("Métrica - Nombre: {}, Valor: {}", metricName, value);

        // Almacenar métrica para análisis
        storeMetric(eventData);
    }

    /**
     * Procesar notificaciones
     */
    private void processNotification(Map<String, Object> eventData) {
        logger.info("📢 Procesando notificación");

        String recipient = (String) eventData.getOrDefault("recipient", "unknown");
        String channel = (String) eventData.getOrDefault("channel", "email");

        logger.info("Notificación - Destinatario: {}, Canal: {}", recipient, channel);

        // Enviar notificación
        sendNotification(eventData);
    }

    /**
     * Procesar eventos desconocidos
     */
    private void processUnknownEvent(Map<String, Object> eventData) {
        logger.warn("❓ Tipo de evento general no reconocido, aplicando procesamiento genérico");

        // Procesamiento genérico para eventos no clasificados
        applyGenericProcessing(eventData);
    }

    // Métodos auxiliares para extracción de campos
    private String extractEventId(Map<String, Object> eventData) {
        Object id = eventData.get("eventId");
        if (id == null) id = eventData.get("id");
        if (id == null) id = eventData.get("Id");
        return id != null ? id.toString() : "unknown-" + System.currentTimeMillis();
    }

    private String extractEventType(Map<String, Object> eventData) {
        Object type = eventData.get("eventType");
        if (type == null) type = eventData.get("type");
        if (type == null) type = eventData.get("Type");
        return type != null ? type.toString() : "UNKNOWN_EVENT";
    }

    private String extractSource(Map<String, Object> eventData) {
        Object source = eventData.get("source");
        if (source == null) source = eventData.get("origin");
        return source != null ? source.toString() : "UNKNOWN_SOURCE";
    }

    // Métodos de procesamiento específico
    private void triggerCriticalAlert(Map<String, Object> eventData) {
        logger.error("🚨 ALERTA CRÍTICA ACTIVADA: {}", eventData);
        // Integración con sistema de alertas
    }

    private void auditUserAction(Map<String, Object> eventData) {
        logger.info("📝 Registrando auditoría de acción de usuario");
        // Guardar en auditoría
    }

    private void storeMetric(Map<String, Object> eventData) {
        logger.info("💾 Almacenando métrica del sistema");
        // Enviar a sistema de métricas
    }

    private void sendNotification(Map<String, Object> eventData) {
        logger.info("📧 Enviando notificación");
        // Integración con sistema de notificaciones
    }

    private void applyGenericProcessing(Map<String, Object> eventData) {
        logger.info("🔄 Aplicando procesamiento genérico");
        // Transformaciones genéricas
    }

    private void storeGeneralEvent(Map<String, Object> eventData) {
        logger.info("💾 Almacenando evento general");
        // Guardar en Elasticsearch con índice específico para eventos generales
    }

    /**
     * Manejo de errores específico para eventos generales
     */
    private void handleGeneralEventError(String message, String topic, Exception error) {
        logger.error("🚨 Error específico en EVENTS PROCESSOR: {}", error.getMessage());

        // Lógica de error específica para eventos generales:
        // 1. Clasificar tipo de error
        // 2. Aplicar reintentos específicos
        // 3. Enviar a DLQ si es necesario
        // 4. Generar métricas de error para eventos generales
    }
}