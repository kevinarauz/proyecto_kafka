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

    private void sendToLegacySystem(SalesforceEvent event, String integrationType) {
        logger.info("Enviando evento a sistemas legados - Tipo: {}, Event: {}",
                    integrationType, event.getEventId());

        // Aquí iría la integración con los sistemas legados de Claro
        // Ejemplo: llamadas REST, colas JMS, bases de datos específicas, etc.

        // Por ahora, solo log de ejemplo
        logger.info("Integración con sistema legado simulada para evento: {}", event.getEventId());
    }
}