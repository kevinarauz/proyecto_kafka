package com.claro.ecuador.eventos.repository;

import com.claro.ecuador.eventos.model.EventLog;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository para gestionar logs de eventos en Elasticsearch
 *
 * @author Global HITSS
 * @project Transforma Ecuador
 * @client Claro Ecuador
 */
@Repository
public interface EventLogRepository extends ElasticsearchRepository<EventLog, String> {

    /**
     * Buscar logs por ID de evento
     */
    List<EventLog> findByEventId(String eventId);

    /**
     * Buscar logs por tipo de evento
     */
    List<EventLog> findByEventType(String eventType);

    /**
     * Buscar logs por nivel (INFO, WARN, ERROR)
     */
    List<EventLog> findByLevel(String level);

    /**
     * Buscar logs por estado de procesamiento
     */
    List<EventLog> findByProcessingStatus(String processingStatus);

    /**
     * Buscar logs por tópico de Kafka
     */
    List<EventLog> findByKafkaTopic(String kafkaTopic);

    /**
     * Buscar logs en un rango de fechas
     */
    List<EventLog> findByTimestampBetween(LocalDateTime start, LocalDateTime end);

    /**
     * Buscar logs de errores en las últimas horas
     */
    List<EventLog> findByLevelAndTimestampAfter(String level, LocalDateTime timestamp);

    /**
     * Contar logs por estado de procesamiento
     */
    long countByProcessingStatus(String processingStatus);
}