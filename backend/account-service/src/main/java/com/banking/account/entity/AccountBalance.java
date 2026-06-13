package com.banking.account.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "ACCOUNT_BALANCES")
public class AccountBalance {
    @Id
    @Column(name = "account_id")
    private Long accountId;

    @Column(name = "ledger_balance", nullable = false)
    private BigDecimal ledgerBalance;

    @Column(name = "available_balance", nullable = false)
    private BigDecimal availableBalance;

    @Version
    @Column(nullable = false)
    private Long version;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        updatedAt = LocalDateTime.now();
        if (ledgerBalance == null) ledgerBalance = BigDecimal.ZERO;
        if (availableBalance == null) availableBalance = BigDecimal.ZERO;
        if (version == null) version = 0L;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public AccountBalance() {}

    public AccountBalance(Long accountId, BigDecimal ledgerBalance, BigDecimal availableBalance, Long version, LocalDateTime updatedAt) {
        this.accountId = accountId;
        this.ledgerBalance = ledgerBalance;
        this.availableBalance = availableBalance;
        this.version = version;
        this.updatedAt = updatedAt;
    }

    public static AccountBalanceBuilder builder() {
        return new AccountBalanceBuilder();
    }

    public Long getAccountId() { return accountId; }
    public void setAccountId(Long accountId) { this.accountId = accountId; }
    public BigDecimal getLedgerBalance() { return ledgerBalance; }
    public void setLedgerBalance(BigDecimal ledgerBalance) { this.ledgerBalance = ledgerBalance; }
    public BigDecimal getAvailableBalance() { return availableBalance; }
    public void setAvailableBalance(BigDecimal availableBalance) { this.availableBalance = availableBalance; }
    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public static class AccountBalanceBuilder {
        private Long accountId;
        private BigDecimal ledgerBalance;
        private BigDecimal availableBalance;
        private Long version;
        private LocalDateTime updatedAt;

        public AccountBalanceBuilder accountId(Long accountId) { this.accountId = accountId; return this; }
        public AccountBalanceBuilder ledgerBalance(BigDecimal ledgerBalance) { this.ledgerBalance = ledgerBalance; return this; }
        public AccountBalanceBuilder availableBalance(BigDecimal availableBalance) { this.availableBalance = availableBalance; return this; }
        public AccountBalanceBuilder version(Long version) { this.version = version; return this; }
        public AccountBalanceBuilder updatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; return this; }

        public AccountBalance build() {
            return new AccountBalance(accountId, ledgerBalance, availableBalance, version, updatedAt);
        }
    }
}
