package com.banking.transaction.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "TRANSACTIONS")
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "transaction_reference", unique = true, nullable = false)
    private String transactionReference;

    @Column(name = "source_account_id")
    private Long sourceAccountId;

    @Column(name = "destination_account_id")
    private Long destinationAccountId;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(name = "transaction_type", nullable = false)
    private String transactionType; // DEPOSIT, WITHDRAWAL, TRANSFER

    @Column(nullable = false)
    private String channel; // NEFT, RTGS, IMPS, UPI, ATM, WEB

    @Column(nullable = false)
    private String status; // PENDING, PROCESSING, SUCCESS, FAILED, REVERSED

    private String description;

    @Column(name = "transaction_date", nullable = false)
    private LocalDateTime transactionDate;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        transactionDate = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) status = "PENDING";
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Transaction() {}

    public Transaction(Long id, String transactionReference, Long sourceAccountId, Long destinationAccountId, BigDecimal amount, String transactionType, String channel, String status, String description, LocalDateTime transactionDate, LocalDateTime updatedAt) {
        this.id = id;
        this.transactionReference = transactionReference;
        this.sourceAccountId = sourceAccountId;
        this.destinationAccountId = destinationAccountId;
        this.amount = amount;
        this.transactionType = transactionType;
        this.channel = channel;
        this.status = status;
        this.description = description;
        this.transactionDate = transactionDate;
        this.updatedAt = updatedAt;
    }

    public static TransactionBuilder builder() {
        return new TransactionBuilder();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTransactionReference() { return transactionReference; }
    public void setTransactionReference(String transactionReference) { this.transactionReference = transactionReference; }
    public Long getSourceAccountId() { return sourceAccountId; }
    public void setSourceAccountId(Long sourceAccountId) { this.sourceAccountId = sourceAccountId; }
    public Long getDestinationAccountId() { return destinationAccountId; }
    public void setDestinationAccountId(Long destinationAccountId) { this.destinationAccountId = destinationAccountId; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public String getTransactionType() { return transactionType; }
    public void setTransactionType(String transactionType) { this.transactionType = transactionType; }
    public String getChannel() { return channel; }
    public void setChannel(String channel) { this.channel = channel; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public LocalDateTime getTransactionDate() { return transactionDate; }
    public void setTransactionDate(LocalDateTime transactionDate) { this.transactionDate = transactionDate; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public static class TransactionBuilder {
        private Long id;
        private String transactionReference;
        private Long sourceAccountId;
        private Long destinationAccountId;
        private BigDecimal amount;
        private String transactionType;
        private String channel;
        private String status;
        private String description;
        private LocalDateTime transactionDate;
        private LocalDateTime updatedAt;

        public TransactionBuilder id(Long id) { this.id = id; return this; }
        public TransactionBuilder transactionReference(String transactionReference) { this.transactionReference = transactionReference; return this; }
        public TransactionBuilder sourceAccountId(Long sourceAccountId) { this.sourceAccountId = sourceAccountId; return this; }
        public TransactionBuilder destinationAccountId(Long destinationAccountId) { this.destinationAccountId = destinationAccountId; return this; }
        public TransactionBuilder amount(BigDecimal amount) { this.amount = amount; return this; }
        public TransactionBuilder transactionType(String transactionType) { this.transactionType = transactionType; return this; }
        public TransactionBuilder channel(String channel) { this.channel = channel; return this; }
        public TransactionBuilder status(String status) { this.status = status; return this; }
        public TransactionBuilder description(String description) { this.description = description; return this; }
        public TransactionBuilder transactionDate(LocalDateTime transactionDate) { this.transactionDate = transactionDate; return this; }
        public TransactionBuilder updatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; return this; }

        public Transaction build() {
            return new Transaction(id, transactionReference, sourceAccountId, destinationAccountId, amount, transactionType, channel, status, description, transactionDate, updatedAt);
        }
    }
}
