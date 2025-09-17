package com.claro.ecuador.eventos.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * Modelo para eventos de Salesforce
 *
 * @author Global HITSS
 */
@Entity
@Table(name = "salesforce_events")
public class SalesforceEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "event_id")
    @JsonProperty("eventId")
    private String eventId;

    @NotNull
    @Column(name = "event_type")
    @JsonProperty("eventType")
    private String eventType;

    @Column(name = "customer_id")
    @JsonProperty("customerId")
    private String customerId;

    @Column(name = "account_id")
    @JsonProperty("accountId")
    private String accountId;

    @Column(name = "order_id")
    @JsonProperty("orderId")
    private String orderId;

    @Column(name = "contract_id")
    @JsonProperty("contractId")
    private String contractId;

    @Column(name = "event_data", columnDefinition = "TEXT")
    @JsonProperty("eventData")
    private String eventData;

    @Column(name = "source_system")
    @JsonProperty("sourceSystem")
    private String sourceSystem = "SALESFORCE";

    @Column(name = "timestamp")
    @JsonProperty("timestamp")
    private LocalDateTime timestamp;

    @Column(name = "processed")
    private Boolean processed = false;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (timestamp == null) {
            timestamp = LocalDateTime.now();
        }
    }

    // Constructores
    public SalesforceEvent() {}

    public SalesforceEvent(String eventId, String eventType, String customerId) {
        this.eventId = eventId;
        this.eventType = eventType;
        this.customerId = customerId;
        this.timestamp = LocalDateTime.now();
    }

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }

    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }

    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }

    public String getAccountId() { return accountId; }
    public void setAccountId(String accountId) { this.accountId = accountId; }

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }

    public String getContractId() { return contractId; }
    public void setContractId(String contractId) { this.contractId = contractId; }

    public String getEventData() { return eventData; }
    public void setEventData(String eventData) { this.eventData = eventData; }

    public String getSourceSystem() { return sourceSystem; }
    public void setSourceSystem(String sourceSystem) { this.sourceSystem = sourceSystem; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public Boolean getProcessed() { return processed; }
    public void setProcessed(Boolean processed) { this.processed = processed; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return "SalesforceEvent{" +
                "id=" + id +
                ", eventId='" + eventId + '\'' +
                ", eventType='" + eventType + '\'' +
                ", customerId='" + customerId + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}