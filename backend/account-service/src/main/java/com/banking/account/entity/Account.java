package com.banking.account.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "ACCOUNTS")
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    @Column(name = "account_number", unique = true, nullable = false)
    private String accountNumber;

    @Column(name = "account_type", nullable = false)
    private String accountType; // SAVINGS, CURRENT, SALARY, JOINT, FD, RD

    @Column(nullable = false)
    private String status; // ACTIVE, FROZEN, CLOSED

    @Column(nullable = false)
    private String currency; // INR, USD, etc.

    @Column(name = "open_date", nullable = false)
    private LocalDate openDate;

    @Column(name = "close_date")
    private LocalDate closeDate;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (openDate == null) openDate = LocalDate.now();
        if (status == null) status = "ACTIVE";
        if (currency == null) currency = "INR";
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Account() {}

    public Account(Long id, Long customerId, String accountNumber, String accountType, String status, String currency, LocalDate openDate, LocalDate closeDate, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.customerId = customerId;
        this.accountNumber = accountNumber;
        this.accountType = accountType;
        this.status = status;
        this.currency = currency;
        this.openDate = openDate;
        this.closeDate = closeDate;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static AccountBuilder builder() {
        return new AccountBuilder();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }
    public String getAccountNumber() { return accountNumber; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }
    public String getAccountType() { return accountType; }
    public void setAccountType(String accountType) { this.accountType = accountType; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public LocalDate getOpenDate() { return openDate; }
    public void setOpenDate(LocalDate openDate) { this.openDate = openDate; }
    public LocalDate getCloseDate() { return closeDate; }
    public void setCloseDate(LocalDate closeDate) { this.closeDate = closeDate; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public static class AccountBuilder {
        private Long id;
        private Long customerId;
        private String accountNumber;
        private String accountType;
        private String status;
        private String currency;
        private LocalDate openDate;
        private LocalDate closeDate;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public AccountBuilder id(Long id) { this.id = id; return this; }
        public AccountBuilder customerId(Long customerId) { this.customerId = customerId; return this; }
        public AccountBuilder accountNumber(String accountNumber) { this.accountNumber = accountNumber; return this; }
        public AccountBuilder accountType(String accountType) { this.accountType = accountType; return this; }
        public AccountBuilder status(String status) { this.status = status; return this; }
        public AccountBuilder currency(String currency) { this.currency = currency; return this; }
        public AccountBuilder openDate(LocalDate openDate) { this.openDate = openDate; return this; }
        public AccountBuilder closeDate(LocalDate closeDate) { this.closeDate = closeDate; return this; }
        public AccountBuilder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public AccountBuilder updatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; return this; }

        public Account build() {
            return new Account(id, customerId, accountNumber, accountType, status, currency, openDate, closeDate, createdAt, updatedAt);
        }
    }
}
