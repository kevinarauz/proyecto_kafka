package com.claro.ecuador.eventos.consumer;

import com.claro.ecuador.eventos.model.SalesforceEvent;
import com.claro.ecuador.eventos.service.EventProcessingService;
import com.claro.ecuador.eventos.service.ElasticsearchLogService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

/**
 * Consumidor de eventos de Salesforce desde Kafka
 *
 * @author Global HITSS
 */
@Component
public class SalesforceEventConsumer {

    private static final Logger logger = LoggerFactory.getLogger(SalesforceEventConsumer.class);

    @Autowired
    private EventProcessingService eventProcessingService;

    @Autowired
    private ElasticsearchLogService elasticsearchLogService;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Consumidor único del topic del microservicio
     */
    @KafkaListener(topics = "back-logstash-connect", groupId = "back-logstash-connect-group")
    public void consumeMicroserviceEvents(@Payload String message,
                                          @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                                          @Header(KafkaHeaders.RECEIVED_PARTITION_ID) int partition,
                                          @Header(KafkaHeaders.OFFSET) long offset) {
        try {
            logger.info("🚀 Recibido evento en topic: {}, partition: {}, offset: {}", topic, partition, offset);
            logger.debug("Mensaje: {}", message);

            SalesforceEvent event = objectMapper.readValue(message, SalesforceEvent.class);
            eventProcessingService.processEvent(event);

            // Log exitoso en Elasticsearch
            elasticsearchLogService.logEventSuccess(
                event.getEventId(),
                event.getEventType(),
                topic,
                partition,
                offset,
                message
            );

            logger.info("✅ Evento procesado exitosamente: {}", event.getEventId());

        } catch (Exception e) {
            logger.error("❌ Error procesando evento en topic {}: {}", topic, e.getMessage(), e);

            // Log error en Elasticsearch
            elasticsearchLogService.logEventError(
                "unknown",
                "ERROR_EVENT",
                topic,
                partition,
                offset,
                message,
                e
            );

            eventProcessingService.handleEventError(message, topic, e);
        }
    }

    /**
     * Consumidor para eventos generales del sistema
     */
    @KafkaListener(topics = "Events", groupId = "back-logstash-connect-group")
    public void consumeGeneralEvents(@Payload String message,
                                   @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                                   @Header(KafkaHeaders.RECEIVED_PARTITION_ID) int partition,
                                   @Header(KafkaHeaders.OFFSET) long offset) {
        try {
            logger.info("📊 Procesando evento general en topic: {}, partition: {}, offset: {}", topic, partition, offset);
            logger.debug("Mensaje: {}", message);

            // Procesar evento general (puede ser JSON genérico)
            eventProcessingService.processGeneralEvent(message);

            // Log exitoso en Elasticsearch
            elasticsearchLogService.logEventSuccess(
                "general-" + offset,
                "GENERAL_EVENT",
                topic,
                partition,
                offset,
                message
            );

            logger.info("✅ Evento general procesado exitosamente");

        } catch (Exception e) {
            logger.error("❌ Error procesando evento general en topic {}: {}", topic, e.getMessage(), e);

            // Log error en Elasticsearch
            elasticsearchLogService.logEventError(
                "general-error-" + offset,
                "GENERAL_EVENT_ERROR",
                topic,
                partition,
                offset,
                message,
                e
            );

            // Enviar a errors.events si falla
            sendToErrorTopic(message, topic, e);
        }
    }

    /**
     * Consumidor para eventos de errores (Dead Letter Queue)
     */
    @KafkaListener(topics = "errors.events", groupId = "back-logstash-connect-group")
    public void consumeErrorEvents(@Payload String message,
                                 @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                                 @Header(KafkaHeaders.RECEIVED_PARTITION_ID) int partition,
                                 @Header(KafkaHeaders.OFFSET) long offset) {
        try {
            logger.warn("🚨 Procesando evento de error en topic: {}, partition: {}, offset: {}", topic, partition, offset);
            logger.debug("Mensaje de error: {}", message);

            // Procesar evento de error para alertas y monitoreo
            eventProcessingService.processErrorEvent(message);

            // Log en Elasticsearch para análisis de errores
            elasticsearchLogService.logEvent(
                "error-" + offset,
                "ERROR_PROCESSING",
                "Procesando evento de error desde DLQ: " + message,
                "WARN",
                "ERROR_HANDLING"
            );

            logger.info("⚠️ Evento de error procesado para análisis");

        } catch (Exception e) {
            logger.error("💥 Error crítico procesando evento de error: {}", e.getMessage(), e);

            // Log error crítico
            elasticsearchLogService.logEvent(
                "critical-error-" + offset,
                "CRITICAL_ERROR",
                "Error crítico procesando DLQ: " + e.getMessage(),
                "ERROR",
                "CRITICAL_FAILURE"
            );
        }
    }

    /**
     * Método auxiliar para enviar eventos fallidos al tópico de errores
     */
    private void sendToErrorTopic(String originalMessage, String sourceTopic, Exception error) {
        try {
            // Aquí podrías implementar un producer para enviar al topic errors.events
            logger.info("📤 Enviando evento fallido de {} a errors.events", sourceTopic);

            // Log del envío a errors.events
            elasticsearchLogService.logEvent(
                "error-redirect",
                "ERROR_REDIRECT",
                "Redirigiendo evento fallido de " + sourceTopic + " a errors.events",
                "INFO",
                "ERROR_REDIRECT"
            );

        } catch (Exception e) {
            logger.error("💥 Error enviando a errors.events: {}", e.getMessage());
        }
    }
}