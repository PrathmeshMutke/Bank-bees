package com.banking.transaction.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "TRANSACTION_HISTORY")
public class TransactionHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "transaction_id", nullable = false)
    private Long transactionId;

    @Column(name = "account_id", nullable = false)
    private Long accountId;

    @Column(name = "balance_after_txn", nullable = false)
    private BigDecimal balanceAfterTxn;

    @Column(name = "entry_type", nullable = false)
    private String entryType; // DEBIT, CREDIT

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public TransactionHistory() {}

    public TransactionHistory(Long id, Long transactionId, Long accountId, BigDecimal balanceAfterTxn, String entryType, LocalDateTime createdAt) {
        this.id = id;
        this.transactionId = transactionId;
        this.accountId = accountId;
        this.balanceAfterTxn = balanceAfterTxn;
        this.entryType = entryType;
        this.createdAt = createdAt;
    }

    public static TransactionHistoryBuilder builder() {
        return new TransactionHistoryBuilder();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getTransactionId() { return transactionId; }
    public void setTransactionId(Long transactionId) { this.transactionId = transactionId; }
    public Long getAccountId() { return accountId; }
    public void setAccountId(Long accountId) { this.accountId = accountId; }
    public BigDecimal getBalanceAfterTxn() { return balanceAfterTxn; }
    public void setBalanceAfterTxn(BigDecimal balanceAfterTxn) { this.balanceAfterTxn = balanceAfterTxn; }
    public String getEntryType() { return entryType; }
    public void setEntryType(String entryType) { this.entryType = entryType; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public static class TransactionHistoryBuilder {
        private Long id;
        private Long transactionId;
        private Long accountId;
        private BigDecimal balanceAfterTxn;
        private String entryType;
        private LocalDateTime createdAt;

        public TransactionHistoryBuilder id(Long id) { this.id = id; return this; }
        public TransactionHistoryBuilder transactionId(Long transactionId) { this.transactionId = transactionId; return this; }
        public TransactionHistoryBuilder accountId(Long accountId) { this.accountId = accountId; return this; }
        public TransactionHistoryBuilder balanceAfterTxn(BigDecimal balanceAfterTxn) { this.balanceAfterTxn = balanceAfterTxn; return this; }
        public TransactionHistoryBuilder entryType(String entryType) { this.entryType = entryType; return this; }
        public TransactionHistoryBuilder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }

        public TransactionHistory build() {
            return new TransactionHistory(id, transactionId, accountId, balanceAfterTxn, entryType, createdAt);
        }
    }
}
