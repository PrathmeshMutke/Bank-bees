package com.banking.account.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "DEBIT_CARDS")
public class DebitCard {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "account_id", nullable = false)
    private Long accountId;

    @Column(name = "card_number", unique = true, nullable = false)
    private String cardNumber;

    @Column(name = "card_holder_name", nullable = false)
    private String cardHolderName;

    @Column(name = "expiry_date", nullable = false)
    private LocalDate expiryDate;

    @Column(name = "cvv_hash", nullable = false)
    private String cvvHash;

    @Column(name = "pin_hash", nullable = false)
    private String pinHash;

    @Column(nullable = false)
    private String status; // ACTIVE, BLOCKED, FROZEN

    @Column(name = "daily_limit", nullable = false)
    private BigDecimal dailyLimit;

    @Column(name = "daily_spent", nullable = false)
    private BigDecimal dailySpent;

    @Column(name = "is_virtual", nullable = false)
    private Integer isVirtual; // 0 or 1

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (status == null) status = "ACTIVE";
        if (dailyLimit == null) dailyLimit = new BigDecimal("50000.00");
        if (dailySpent == null) dailySpent = BigDecimal.ZERO;
        if (isVirtual == null) isVirtual = 0;
    }

    public DebitCard() {}

    public DebitCard(Long id, Long accountId, String cardNumber, String cardHolderName, LocalDate expiryDate, String cvvHash, String pinHash, String status, BigDecimal dailyLimit, BigDecimal dailySpent, Integer isVirtual, LocalDateTime createdAt) {
        this.id = id;
        this.accountId = accountId;
        this.cardNumber = cardNumber;
        this.cardHolderName = cardHolderName;
        this.expiryDate = expiryDate;
        this.cvvHash = cvvHash;
        this.pinHash = pinHash;
        this.status = status;
        this.dailyLimit = dailyLimit;
        this.dailySpent = dailySpent;
        this.isVirtual = isVirtual;
        this.createdAt = createdAt;
    }

    public static DebitCardBuilder builder() {
        return new DebitCardBuilder();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getAccountId() { return accountId; }
    public void setAccountId(Long accountId) { this.accountId = accountId; }
    public String getCardNumber() { return cardNumber; }
    public void setCardNumber(String cardNumber) { this.cardNumber = cardNumber; }
    public String getCardHolderName() { return cardHolderName; }
    public void setCardHolderName(String cardHolderName) { this.cardHolderName = cardHolderName; }
    public LocalDate getExpiryDate() { return expiryDate; }
    public void setExpiryDate(LocalDate expiryDate) { this.expiryDate = expiryDate; }
    public String getCvvHash() { return cvvHash; }
    public void setCvvHash(String cvvHash) { this.cvvHash = cvvHash; }
    public String getPinHash() { return pinHash; }
    public void setPinHash(String pinHash) { this.pinHash = pinHash; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public BigDecimal getDailyLimit() { return dailyLimit; }
    public void setDailyLimit(BigDecimal dailyLimit) { this.dailyLimit = dailyLimit; }
    public BigDecimal getDailySpent() { return dailySpent; }
    public void setDailySpent(BigDecimal dailySpent) { this.dailySpent = dailySpent; }
    public Integer getIsVirtual() { return isVirtual; }
    public void setIsVirtual(Integer isVirtual) { this.isVirtual = isVirtual; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public static class DebitCardBuilder {
        private Long id;
        private Long accountId;
        private String cardNumber;
        private String cardHolderName;
        private LocalDate expiryDate;
        private String cvvHash;
        private String pinHash;
        private String status;
        private BigDecimal dailyLimit;
        private BigDecimal dailySpent;
        private Integer isVirtual;
        private LocalDateTime createdAt;

        public DebitCardBuilder id(Long id) { this.id = id; return this; }
        public DebitCardBuilder accountId(Long accountId) { this.accountId = accountId; return this; }
        public DebitCardBuilder cardNumber(String cardNumber) { this.cardNumber = cardNumber; return this; }
        public DebitCardBuilder cardHolderName(String cardHolderName) { this.cardHolderName = cardHolderName; return this; }
        public DebitCardBuilder expiryDate(LocalDate expiryDate) { this.expiryDate = expiryDate; return this; }
        public DebitCardBuilder cvvHash(String cvvHash) { this.cvvHash = cvvHash; return this; }
        public DebitCardBuilder pinHash(String pinHash) { this.pinHash = pinHash; return this; }
        public DebitCardBuilder status(String status) { this.status = status; return this; }
        public DebitCardBuilder dailyLimit(BigDecimal dailyLimit) { this.dailyLimit = dailyLimit; return this; }
        public DebitCardBuilder dailySpent(BigDecimal dailySpent) { this.dailySpent = dailySpent; return this; }
        public DebitCardBuilder isVirtual(Integer isVirtual) { this.isVirtual = isVirtual; return this; }
        public DebitCardBuilder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }

        public DebitCard build() {
            return new DebitCard(id, accountId, cardNumber, cardHolderName, expiryDate, cvvHash, pinHash, status, dailyLimit, dailySpent, isVirtual, createdAt);
        }
    }
}
