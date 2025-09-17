package com.claro.ecuador.eventos.service;

import com.claro.ecuador.eventos.model.SalesforceEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Servicio para procesamiento de eventos de Salesforce
 *
 * @author Global HITSS
 */
@Service
@Transactional
public class EventProcessingService {

    private static final Logger logger = LoggerFactory.getLogger(EventProcessingService.class);

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Procesar evento general
     */
    public void processEvent(SalesforceEvent event) {
        try {
            logger.info("Procesando evento general: {}", event.getEventId());

            // Validar datos del evento
            validateEvent(event);

            // Guardar en base de datos
            saveEvent(event);

            // Procesar según tipo de evento
            switch (event.getEventType().toUpperCase()) {
                case "CUSTOMER_CREATED":
                case "CUSTOMER_UPDATED":
                    processCustomerLogic(event);
                    break;
                case "ORDER_CREATED":
                case "ORDER_UPDATED":
                    processOrderLogic(event);
                    break;
                case "CONTRACT_CREATED":
                case "CONTRACT_SIGNED":
                    processContractLogic(event);
                    break;
                default:
                    logger.warn("Tipo de evento no reconocido: {}", event.getEventType());
            }

            // Marcar como procesado
            event.setProcessed(true);
            entityManager.merge(event);

            logger.info("Evento procesado exitosamente: {}", event.getEventId());

        } catch (Exception e) {
            logger.error("Error procesando evento {}: {}", event.getEventId(), e.getMessage());
            throw new RuntimeException("Error procesando evento", e);
        }
    }

    /**
     * Procesar evento específico de cliente
     */
    public void processCustomerEvent(SalesforceEvent event) {
        logger.info("Procesando evento específico de cliente: {}", event.getCustomerId());

        saveEvent(event);
        processCustomerLogic(event);

        // Integración con sistemas legados de Claro
        sendToLegacySystem(event, "CUSTOMER_INTEGRATION");
    }

    /**
     * Procesar evento específico de orden
     */
    public void processOrderEvent(SalesforceEvent event) {
        logger.info("Procesando evento específico de orden: {}", event.getOrderId());

        saveEvent(event);
        processOrderLogic(event);

        // Integración con sistemas legados de Claro
        sendToLegacySystem(event, "ORDER_INTEGRATION");
    }

    /**
     * Manejar errores de eventos
     */
    public void handleEventError(String originalMessage, String topic, Exception error) {
        try {
            Map<String, Object> errorEvent = new HashMap<>();
            errorEvent.put("timestamp", LocalDateTime.now().toString());
            errorEvent.put("originalMessage", originalMessage);
            errorEvent.put("topic", topic);
            errorEvent.put("error", error.getMessage());
            errorEvent.put("errorType", error.getClass().getSimpleName());

            String errorJson = objectMapper.writeValueAsString(errorEvent);

            // Enviar a topic de errores
            kafkaTemplate.send("errors.events", errorJson);

            logger.info("Error enviado a topic errors.events");

        } catch (Exception e) {
            logger.error("Error enviando evento de error: {}", e.getMessage());
        }
    }

    private void validateEvent(SalesforceEvent event) {
        if (event.getEventId() == null || event.getEventId().trim().isEmpty()) {
            throw new IllegalArgumentException("Event ID es requerido");
        }
        if (event.getEventType() == null || event.getEventType().trim().isEmpty()) {
            throw new IllegalArgumentException("Event Type es requerido");
        }
    }

    private void saveEvent(SalesforceEvent event) {
        try {
            entityManager.persist(event);
            logger.debug("Evento guardado en base de datos: {}", event.getEventId());
        } catch (Exception e) {
            logger.error("Error guardando evento en BD: {}", e.getMessage());
            throw e;
        }
    }

    private void processCustomerLogic(SalesforceEvent event) {
        logger.info("Ejecutando lógica específica de cliente para: {}", event.getCustomerId());
        // Aquí iría la lógica específica para eventos de cliente
        // Ejemplo: validaciones, transformaciones, etc.
    }

    private void processOrderLogic(SalesforceEvent event) {
        logger.info("Ejecutando lógica específica de orden para: {}", event.getOrderId());
        // Aquí iría la lógica específica para eventos de orden
        // Ejemplo: validaciones, cálculos, etc.
    }

    private void processContractLogic(SalesforceEvent event) {
        logger.info("Ejecutando lógica específica de contrato para: {}", event.getContractId());
        // Aquí iría la lógica específica para eventos de contrato
        // Ejemplo: validaciones legales, activaciones, etc.
    }

    /**
     * Procesar evento general (JSON genérico)
     */
    public void processGeneralEvent(String message) {
        try {
            logger.info("Procesando evento general desde topic Events");

            // Intentar parsear como JSON genérico
            Map<String, Object> eventData = objectMapper.readValue(message, Map.class);

            // Extraer campos comunes
            String eventId = extractEventId(eventData);
            String eventType = extractEventType(eventData);

            logger.info("Evento general - ID: {}, Tipo: {}", eventId, eventType);

            // Procesar según el tipo de evento
            processGenericEventLogic(eventData);

            logger.info("Evento general procesado exitosamente");

        } catch (Exception e) {
            logger.error("Error procesando evento general: {}", e.getMessage());
            throw new RuntimeException("Error procesando evento general", e);
        }
    }

    /**
     * Procesar evento de error desde DLQ (Dead Letter Queue)
     */
    public void processErrorEvent(String errorMessage) {
        try {
            logger.warn("Procesando evento de error desde DLQ");

            // Parsear mensaje de error
            Map<String, Object> errorData = objectMapper.readValue(errorMessage, Map.class);

            // Extraer información del error
            String originalTopic = (String) errorData.get("topic");
            String error = (String) errorData.get("error");
            String timestamp = (String) errorData.get("timestamp");

            logger.warn("Error procesado - Topic original: {}, Error: {}, Timestamp: {}",
                       originalTopic, error, timestamp);

            // Procesar para análisis y alertas
            processErrorAnalysis(errorData);

            // Determinar si se debe reintentar
            if (shouldRetryEvent(errorData)) {
                scheduleRetry(errorData);
            }

            logger.info("Evento de error procesado para análisis");

        } catch (Exception e) {
            logger.error("Error crítico procesando evento de error: {}", e.getMessage());
        }
    }

    private String extractEventId(Map<String, Object> eventData) {
        // Intentar diferentes nombres de campos para el ID
        Object id = eventData.get("eventId");
        if (id == null) id = eventData.get("id");
        if (id == null) id = eventData.get("Id");
        return id != null ? id.toString() : "unknown-" + System.currentTimeMillis();
    }

    private String extractEventType(Map<String, Object> eventData) {
        // Intentar diferentes nombres de campos para el tipo
        Object type = eventData.get("eventType");
        if (type == null) type = eventData.get("type");
        if (type == null) type = eventData.get("Type");
        return type != null ? type.toString() : "UNKNOWN_EVENT";
    }

    private void processGenericEventLogic(Map<String, Object> eventData) {
        // Lógica genérica para eventos del topic Events
        logger.info("Ejecutando lógica genérica para evento: {}", eventData.get("eventId"));

        // Validaciones genéricas
        if (eventData.containsKey("customerId")) {
            logger.info("Evento relacionado con cliente: {}", eventData.get("customerId"));
        }

        if (eventData.containsKey("orderId")) {
            logger.info("Evento relacionado con orden: {}", eventData.get("orderId"));
        }

        // Aplicar transformaciones si es necesario
        applyGenericTransformations(eventData);
    }

    private void processErrorAnalysis(Map<String, Object> errorData) {
        // Análisis de errores para alertas y métricas
        String errorType = (String) errorData.get("errorType");
        String originalTopic = (String) errorData.get("topic");

        logger.info("Analizando error - Tipo: {}, Topic: {}", errorType, originalTopic);

        // Incrementar métricas de error
        updateErrorMetrics(errorType, originalTopic);

        // Generar alerta si es crítico
        if (isCriticalError(errorType)) {
            generateCriticalAlert(errorData);
        }
    }

    private boolean shouldRetryEvent(Map<String, Object> errorData) {
        // Determinar si el evento debe ser reintentado
        String errorType = (String) errorData.get("errorType");

        // No reintentar errores de parsing JSON o validación
        return !("JsonProcessingException".equals(errorType) ||
                 "IllegalArgumentException".equals(errorType));
    }

    private void scheduleRetry(Map<String, Object> errorData) {
        logger.info("Programando reintento para evento: {}", errorData.get("originalMessage"));
        // Aquí se podría implementar lógica de reintento con backoff exponencial
    }

    private void applyGenericTransformations(Map<String, Object> eventData) {
        // Transformaciones genéricas como normalización de fechas, campos, etc.
        if (eventData.containsKey("timestamp")) {
            // Normalizar formato de timestamp si es necesario
        }
    }

    private void updateErrorMetrics(String errorType, String originalTopic) {
        // Actualizar métricas de errores para monitoreo
        logger.debug("Actualizando métricas - Error: {}, Topic: {}", errorType, originalTopic);
    }

    private boolean isCriticalError(String errorType) {
        // Determinar si es un error crítico que requiere alerta inmediata
        return "NullPointerException".equals(errorType) ||
               "OutOfMemoryError".equals(errorType) ||
               "DatabaseConnectionException".equals(errorType);
    }

    private void generateCriticalAlert(Map<String, Object> errorData) {
        logger.error("🚨 ALERTA CRÍTICA: Error crítico detectado: {}", errorData);
        // Aquí se podría integrar con sistemas de alertas (email, Slack, PagerDuty, etc.)
    }

    private void sendToLegacySystem(SalesforceEvent event, String integrationType) {
        logger.info("Enviando evento a sistemas legados - Tipo: {}, Event: {}",
                    integrationType, event.getEventId());

        // Aquí iría la integración con los sistemas legados de Claro
        // Ejemplo: llamadas REST, colas JMS, bases de datos específicas, etc.

        // Por ahora, solo log de ejemplo
        logger.info("Integración con sistema legado simulada para evento: {}", event.getEventId());
    }
}