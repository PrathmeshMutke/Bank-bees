package com.banking.core.event;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class BankingEvents {

    public static class BaseEvent implements Serializable {
        private String eventId;
        private LocalDateTime timestamp = LocalDateTime.now();

        public BaseEvent() {}

        public BaseEvent(String eventId, LocalDateTime timestamp) {
            this.eventId = eventId;
            this.timestamp = timestamp;
        }

        public String getEventId() {
            return eventId;
        }

        public void setEventId(String eventId) {
            this.eventId = eventId;
        }

        public LocalDateTime getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(LocalDateTime timestamp) {
            this.timestamp = timestamp;
        }
    }

    public static class CustomerCreatedEvent implements Serializable {
        private Long customerId;
        private Long userId;
        private String email;
        private String firstName;
        private String lastName;
        private String panNumber;

        public CustomerCreatedEvent() {}

        public CustomerCreatedEvent(Long customerId, Long userId, String email, String firstName, String lastName, String panNumber) {
            this.customerId = customerId;
            this.userId = userId;
            this.email = email;
            this.firstName = firstName;
            this.lastName = lastName;
            this.panNumber = panNumber;
        }

        public Long getCustomerId() { return customerId; }
        public void setCustomerId(Long customerId) { this.customerId = customerId; }
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getFirstName() { return firstName; }
        public void setFirstName(String firstName) { this.firstName = firstName; }
        public String getLastName() { return lastName; }
        public void setLastName(String lastName) { this.lastName = lastName; }
        public String getPanNumber() { return panNumber; }
        public void setPanNumber(String panNumber) { this.panNumber = panNumber; }
    }

    public static class TransactionInitiatedEvent implements Serializable {
        private String transactionReference;
        private Long sourceAccountId;
        private Long destinationAccountId;
        private BigDecimal amount;
        private String transactionType;
        private String channel;

        public TransactionInitiatedEvent() {}

        public TransactionInitiatedEvent(String transactionReference, Long sourceAccountId, Long destinationAccountId, BigDecimal amount, String transactionType, String channel) {
            this.transactionReference = transactionReference;
            this.sourceAccountId = sourceAccountId;
            this.destinationAccountId = destinationAccountId;
            this.amount = amount;
            this.transactionType = transactionType;
            this.channel = channel;
        }

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
    }

    public static class TransactionSuccessEvent implements Serializable {
        private Long transactionId;
        private String transactionReference;
        private Long sourceAccountId;
        private Long destinationAccountId;
        private BigDecimal amount;
        private String transactionType;
        private String channel;
        private BigDecimal balanceAfterTxn;

        public TransactionSuccessEvent() {}

        public TransactionSuccessEvent(Long transactionId, String transactionReference, Long sourceAccountId, Long destinationAccountId, BigDecimal amount, String transactionType, String channel, BigDecimal balanceAfterTxn) {
            this.transactionId = transactionId;
            this.transactionReference = transactionReference;
            this.sourceAccountId = sourceAccountId;
            this.destinationAccountId = destinationAccountId;
            this.amount = amount;
            this.transactionType = transactionType;
            this.channel = channel;
            this.balanceAfterTxn = balanceAfterTxn;
        }

        public Long getTransactionId() { return transactionId; }
        public void setTransactionId(Long transactionId) { this.transactionId = transactionId; }
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
        public BigDecimal getBalanceAfterTxn() { return balanceAfterTxn; }
        public void setBalanceAfterTxn(BigDecimal balanceAfterTxn) { this.balanceAfterTxn = balanceAfterTxn; }
    }

    public static class TransactionFailedEvent implements Serializable {
        private String transactionReference;
        private Long sourceAccountId;
        private Long destinationAccountId;
        private BigDecimal amount;
        private String failureReason;

        public TransactionFailedEvent() {}

        public TransactionFailedEvent(String transactionReference, Long sourceAccountId, Long destinationAccountId, BigDecimal amount, String failureReason) {
            this.transactionReference = transactionReference;
            this.sourceAccountId = sourceAccountId;
            this.destinationAccountId = destinationAccountId;
            this.amount = amount;
            this.failureReason = failureReason;
        }

        public String getTransactionReference() { return transactionReference; }
        public void setTransactionReference(String transactionReference) { this.transactionReference = transactionReference; }
        public Long getSourceAccountId() { return sourceAccountId; }
        public void setSourceAccountId(Long sourceAccountId) { this.sourceAccountId = sourceAccountId; }
        public Long getDestinationAccountId() { return destinationAccountId; }
        public void setDestinationAccountId(Long destinationAccountId) { this.destinationAccountId = destinationAccountId; }
        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }
        public String getFailureReason() { return failureReason; }
        public void setFailureReason(String failureReason) { this.failureReason = failureReason; }
    }

    public static class PaymentProcessedEvent implements Serializable {
        private Long transactionId;
        private BigDecimal amount;
        private String provider;
        private String externalPaymentId;
        private String status;

        public PaymentProcessedEvent() {}

        public PaymentProcessedEvent(Long transactionId, BigDecimal amount, String provider, String externalPaymentId, String status) {
            this.transactionId = transactionId;
            this.amount = amount;
            this.provider = provider;
            this.externalPaymentId = externalPaymentId;
            this.status = status;
        }

        public Long getTransactionId() { return transactionId; }
        public void setTransactionId(Long transactionId) { this.transactionId = transactionId; }
        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }
        public String getProvider() { return provider; }
        public void setProvider(String provider) { this.provider = provider; }
        public String getExternalPaymentId() { return externalPaymentId; }
        public void setExternalPaymentId(String externalPaymentId) { this.externalPaymentId = externalPaymentId; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }

    public static class FraudDetectedEvent implements Serializable {
        private String transactionReference;
        private BigDecimal amount;
        private BigDecimal riskScore;
        private String rulesTriggered;

        public FraudDetectedEvent() {}

        public FraudDetectedEvent(String transactionReference, BigDecimal amount, BigDecimal riskScore, String rulesTriggered) {
            this.transactionReference = transactionReference;
            this.amount = amount;
            this.riskScore = riskScore;
            this.rulesTriggered = rulesTriggered;
        }

        public String getTransactionReference() { return transactionReference; }
        public void setTransactionReference(String transactionReference) { this.transactionReference = transactionReference; }
        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }
        public BigDecimal getRiskScore() { return riskScore; }
        public void setRiskScore(BigDecimal riskScore) { this.riskScore = riskScore; }
        public String getRulesTriggered() { return rulesTriggered; }
        public void setRulesTriggered(String rulesTriggered) { this.rulesTriggered = rulesTriggered; }
    }
}
