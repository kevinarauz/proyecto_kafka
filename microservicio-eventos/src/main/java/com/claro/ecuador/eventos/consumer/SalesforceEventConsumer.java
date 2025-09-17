package com.claro.ecuador.eventos.consumer;

import com.claro.ecuador.eventos.model.SalesforceEvent;
import com.claro.ecuador.eventos.service.EventProcessingService;
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
    private ObjectMapper objectMapper;

    /**
     * Consumidor único del topic del microservicio
     */
    @KafkaListener(topics = "microservicio-eventos", groupId = "microservicio-eventos-group")
    public void consumeMicroserviceEvents(@Payload String message,
                                          @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                                          @Header(KafkaHeaders.RECEIVED_PARTITION_ID) int partition,
                                          @Header(KafkaHeaders.OFFSET) long offset) {
        try {
            logger.info("🚀 Recibido evento en topic: {}, partition: {}, offset: {}", topic, partition, offset);
            logger.debug("Mensaje: {}", message);

            SalesforceEvent event = objectMapper.readValue(message, SalesforceEvent.class);
            eventProcessingService.processEvent(event);

            logger.info("✅ Evento procesado exitosamente: {}", event.getEventId());

        } catch (Exception e) {
            logger.error("❌ Error procesando evento en topic {}: {}", topic, e.getMessage(), e);
            eventProcessingService.handleEventError(message, topic, e);
        }
    }
}