package com.claro.ecuador.eventos.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.LocalDateTime;

/**
 * Modelo para almacenar logs de eventos en Elasticsearch
 *
 * @author Global HITSS
 * @project Transforma Ecuador
 * @client Claro Ecuador
 */
@Document(indexName = "transforma-eventos-logs")
public class EventLog {

    @Id
    private String id;

    @Field(type = FieldType.Keyword)
    private String eventId;

    @Field(type = FieldType.Keyword)
    private String eventType;

    @Field(type = FieldType.Text)
    private String message;

    @Field(type = FieldType.Keyword)
    private String level; // INFO, WARN, ERROR

    @Field(type = FieldType.Keyword)
    private String kafkaTopic;

    @Field(type = FieldType.Integer)
    private Integer kafkaPartition;

    @Field(type = FieldType.Long)
    private Long kafkaOffset;

    @Field(type = FieldType.Keyword)
    private String consumerGroup;

    @Field(type = FieldType.Text)
    private String originalMessage;

    @Field(type = FieldType.Text)
    private String errorMessage;

    @Field(type = FieldType.Keyword)
    private String processingStatus; // SUCCESS, ERROR, RETRY

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Field(type = FieldType.Date, format = {}, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;

    @Field(type = FieldType.Keyword)
    private String microserviceVersion;

    @Field(type = FieldType.Keyword)
    private String environment;

    // Constructors
    public EventLog() {
        this.timestamp = LocalDateTime.now();
        this.microserviceVersion = "1.0.0";
        this.environment = "development";
    }

    public EventLog(String eventId, String eventType, String message, String level) {
        this();
        this.eventId = eventId;
        this.eventType = eventType;
        this.message = message;
        this.level = level;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getKafkaTopic() {
        return kafkaTopic;
    }

    public void setKafkaTopic(String kafkaTopic) {
        this.kafkaTopic = kafkaTopic;
    }

    public Integer getKafkaPartition() {
        return kafkaPartition;
    }

    public void setKafkaPartition(Integer kafkaPartition) {
        this.kafkaPartition = kafkaPartition;
    }

    public Long getKafkaOffset() {
        return kafkaOffset;
    }

    public void setKafkaOffset(Long kafkaOffset) {
        this.kafkaOffset = kafkaOffset;
    }

    public String getConsumerGroup() {
        return consumerGroup;
    }

    public void setConsumerGroup(String consumerGroup) {
        this.consumerGroup = consumerGroup;
    }

    public String getOriginalMessage() {
        return originalMessage;
    }

    public void setOriginalMessage(String originalMessage) {
        this.originalMessage = originalMessage;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getProcessingStatus() {
        return processingStatus;
    }

    public void setProcessingStatus(String processingStatus) {
        this.processingStatus = processingStatus;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getMicroserviceVersion() {
        return microserviceVersion;
    }

    public void setMicroserviceVersion(String microserviceVersion) {
        this.microserviceVersion = microserviceVersion;
    }

    public String getEnvironment() {
        return environment;
    }

    public void setEnvironment(String environment) {
        this.environment = environment;
    }

    @Override
    public String toString() {
        return "EventLog{" +
                "id='" + id + '\'' +
                ", eventId='" + eventId + '\'' +
                ", eventType='" + eventType + '\'' +
                ", level='" + level + '\'' +
                ", timestamp=" + timestamp +
                ", processingStatus='" + processingStatus + '\'' +
                '}';
    }
}