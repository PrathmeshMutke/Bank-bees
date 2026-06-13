package com.banking.transaction.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "OUTBOX")
public class OutboxMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "aggregate_type", nullable = false)
    private String aggregateType;

    @Column(name = "aggregate_id", nullable = false)
    private String aggregateId;

    @Column(name = "event_type", nullable = false)
    private String eventType;

    @Lob
    @Column(nullable = false)
    private String payload;

    @Column(nullable = false)
    private String status; // PENDING, PROCESSED, FAILED

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (status == null) status = "PENDING";
    }

    public OutboxMessage() {}

    public OutboxMessage(Long id, String aggregateType, String aggregateId, String eventType, String payload, String status, LocalDateTime createdAt) {
        this.id = id;
        this.aggregateType = aggregateType;
        this.aggregateId = aggregateId;
        this.eventType = eventType;
        this.payload = payload;
        this.status = status;
        this.createdAt = createdAt;
    }

    public static OutboxMessageBuilder builder() {
        return new OutboxMessageBuilder();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getAggregateType() { return aggregateType; }
    public void setAggregateType(String aggregateType) { this.aggregateType = aggregateType; }
    public String getAggregateId() { return aggregateId; }
    public void setAggregateId(String aggregateId) { this.aggregateId = aggregateId; }
    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }
    public String getPayload() { return payload; }
    public void setPayload(String payload) { this.payload = payload; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public static class OutboxMessageBuilder {
        private Long id;
        private String aggregateType;
        private String aggregateId;
        private String eventType;
        private String payload;
        private String status;
        private LocalDateTime createdAt;

        public OutboxMessageBuilder id(Long id) { this.id = id; return this; }
        public OutboxMessageBuilder aggregateType(String aggregateType) { this.aggregateType = aggregateType; return this; }
        public OutboxMessageBuilder aggregateId(String aggregateId) { this.aggregateId = aggregateId; return this; }
        public OutboxMessageBuilder eventType(String eventType) { this.eventType = eventType; return this; }
        public OutboxMessageBuilder payload(String payload) { this.payload = payload; return this; }
        public OutboxMessageBuilder status(String status) { this.status = status; return this; }
        public OutboxMessageBuilder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }

        public OutboxMessage build() {
            return new OutboxMessage(id, aggregateType, aggregateId, eventType, payload, status, createdAt);
        }
    }
}
