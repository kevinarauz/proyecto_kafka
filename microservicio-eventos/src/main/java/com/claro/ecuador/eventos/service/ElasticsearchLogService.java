package com.claro.ecuador.eventos.service;

import com.claro.ecuador.eventos.model.EventLog;
import com.claro.ecuador.eventos.repository.EventLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Servicio para gestionar logs en Elasticsearch
 *
 * @author Global HITSS
 * @project Transforma Ecuador
 * @client Claro Ecuador
 */
@Service
public class ElasticsearchLogService {

    private static final Logger logger = LoggerFactory.getLogger(ElasticsearchLogService.class);

    @Autowired
    private EventLogRepository eventLogRepository;

    /**
     * Guardar log de evento procesado exitosamente
     */
    public void logEventSuccess(String eventId, String eventType, String kafkaTopic,
                               int partition, long offset, String originalMessage) {
        try {
            EventLog eventLog = new EventLog(eventId, eventType,
                "Evento procesado exitosamente", "INFO");

            eventLog.setKafkaTopic(kafkaTopic);
            eventLog.setKafkaPartition(partition);
            eventLog.setKafkaOffset(offset);
            eventLog.setConsumerGroup("back-logstash-connect-group");
            eventLog.setOriginalMessage(originalMessage);
            eventLog.setProcessingStatus("SUCCESS");

            eventLogRepository.save(eventLog);

        } catch (Exception e) {
            logger.error("Error guardando log de éxito en Elasticsearch: {}", e.getMessage());
        }
    }

    /**
     * Guardar log de evento con error
     */
    public void logEventError(String eventId, String eventType, String kafkaTopic,
                             int partition, long offset, String originalMessage, Exception error) {
        try {
            EventLog eventLog = new EventLog(eventId, eventType,
                "Error procesando evento: " + error.getMessage(), "ERROR");

            eventLog.setKafkaTopic(kafkaTopic);
            eventLog.setKafkaPartition(partition);
            eventLog.setKafkaOffset(offset);
            eventLog.setConsumerGroup("back-logstash-connect-group");
            eventLog.setOriginalMessage(originalMessage);
            eventLog.setErrorMessage(error.getMessage());
            eventLog.setProcessingStatus("ERROR");

            eventLogRepository.save(eventLog);

        } catch (Exception e) {
            logger.error("Error guardando log de error en Elasticsearch: {}", e.getMessage());
        }
    }

    /**
     * Guardar log genérico
     */
    public void logEvent(String eventId, String eventType, String message, String level,
                        String processingStatus) {
        try {
            EventLog eventLog = new EventLog(eventId, eventType, message, level);
            eventLog.setProcessingStatus(processingStatus);

            eventLogRepository.save(eventLog);

        } catch (Exception e) {
            logger.error("Error guardando log genérico en Elasticsearch: {}", e.getMessage());
        }
    }

    /**
     * Obtener logs de errores recientes
     */
    public List<EventLog> getRecentErrors(int hoursBack) {
        LocalDateTime timeLimit = LocalDateTime.now().minusHours(hoursBack);
        return eventLogRepository.findByLevelAndTimestampAfter("ERROR", timeLimit);
    }

    /**
     * Obtener estadísticas de procesamiento
     */
    public void logProcessingStats() {
        try {
            long successCount = eventLogRepository.countByProcessingStatus("SUCCESS");
            long errorCount = eventLogRepository.countByProcessingStatus("ERROR");
            long retryCount = eventLogRepository.countByProcessingStatus("RETRY");

            logger.info("📊 Estadísticas de procesamiento - Éxitos: {}, Errores: {}, Reintentos: {}",
                       successCount, errorCount, retryCount);

        } catch (Exception e) {
            logger.error("Error obteniendo estadísticas de Elasticsearch: {}", e.getMessage());
        }
    }

    /**
     * Verificar conectividad con Elasticsearch
     */
    public boolean isElasticsearchAvailable() {
        try {
            eventLogRepository.count();
            return true;
        } catch (Exception e) {
            logger.warn("Elasticsearch no disponible: {}", e.getMessage());
            return false;
        }
    }
}