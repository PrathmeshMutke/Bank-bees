package com.banking.account.service;

import com.banking.account.entity.Account;
import com.banking.account.entity.AccountBalance;
import com.banking.account.entity.DebitCard;
import com.banking.account.repository.AccountBalanceRepository;
import com.banking.account.repository.AccountRepository;
import com.banking.account.repository.DebitCardRepository;
import com.banking.core.dto.ApiResponse;
import com.banking.core.exception.CustomExceptions.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Random;

@Service
public class AccountService {

    private static final Logger log = LoggerFactory.getLogger(AccountService.class);

    private final AccountRepository accountRepository;
    private final AccountBalanceRepository balanceRepository;
    private final DebitCardRepository debitCardRepository;

    public AccountService(AccountRepository accountRepository, AccountBalanceRepository balanceRepository, DebitCardRepository debitCardRepository) {
        this.accountRepository = accountRepository;
        this.balanceRepository = balanceRepository;
        this.debitCardRepository = debitCardRepository;
    }

    @Transactional
    public ApiResponse<Account> createAccount(CreateAccountRequest request) {
        log.info("[AccountService] Provisioning new {} account for customerId={}", request.getAccountType(), request.getCustomerId());
        String accountNo = "ACC" + (1000000000L + new Random().nextInt(900000000));
        
        Account account = Account.builder()
                .customerId(request.getCustomerId())
                .accountNumber(accountNo)
                .accountType(request.getAccountType())
                .status("ACTIVE")
                .currency("INR")
                .build();
        
        Account savedAccount = accountRepository.save(account);
        log.info("[AccountService] Account {} created with id={}", accountNo, savedAccount.getId());

        AccountBalance balance = AccountBalance.builder()
                .accountId(savedAccount.getId())
                .availableBalance(request.getInitialBalance())
                .ledgerBalance(request.getInitialBalance())
                .build();

        balanceRepository.save(balance);
        log.info("[AccountService] Initial balance of {} INR seeded for accountId={}", request.getInitialBalance(), savedAccount.getId());

        return ApiResponse.success(savedAccount, "Account provisioned successfully");
    }

    public ApiResponse<List<Account>> getAccountsByCustomerId(Long customerId) {
        log.info("[AccountService] Fetching all accounts for customerId={}", customerId);
        List<Account> accounts = accountRepository.findByCustomerId(customerId);
        log.info("[AccountService] Found {} account(s) for customerId={}", accounts.size(), customerId);
        return ApiResponse.success(accounts);
    }

    public ApiResponse<AccountBalance> getBalance(Long accountId) {
        log.info("[AccountService] Fetching balance for accountId={}", accountId);
        AccountBalance balance = balanceRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account balance not found"));
        log.info("[AccountService] Balance for accountId={} is available={}", accountId, balance.getAvailableBalance());
        return ApiResponse.success(balance);
    }

    @Transactional
    public ApiResponse<DebitCard> issueCard(Long accountId, boolean isVirtual) {
        log.info("[AccountService] Issuing {} debit card for accountId={}", isVirtual ? "virtual" : "physical", accountId);
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));

        Random rand = new Random();
        StringBuilder cardNo = new StringBuilder("4111");
        for (int i = 0; i < 12; i++) {
            cardNo.append(rand.nextInt(10));
        }

        DebitCard card = DebitCard.builder()
                .accountId(accountId)
                .cardNumber(cardNo.toString())
                .cardHolderName("Account Holder")
                .expiryDate(LocalDate.now().plusYears(5))
                .cvvHash("123")
                .pinHash("1234")
                .status("ACTIVE")
                .isVirtual(isVirtual ? 1 : 0)
                .dailyLimit(new BigDecimal("50000.00"))
                .dailySpent(BigDecimal.ZERO)
                .build();

        DebitCard savedCard = debitCardRepository.save(card);
        log.info("[AccountService] Card issued with id={} for accountId={}", savedCard.getId(), accountId);
        return ApiResponse.success(savedCard, "Debit card issued successfully");
    }

    @Transactional
    public ApiResponse<DebitCard> updateCardStatus(Long cardId, String status) {
        log.info("[AccountService] Updating card status: cardId={}, newStatus={}", cardId, status);
        DebitCard card = debitCardRepository.findById(cardId)
                .orElseThrow(() -> new ResourceNotFoundException("Debit card not found"));
        
        if (!List.of("ACTIVE", "BLOCKED", "FROZEN").contains(status)) {
            log.warn("[AccountService] Invalid card status provided: {}", status);
            throw new InvalidTransactionException("Invalid card status");
        }

        String previousStatus = card.getStatus();
        card.setStatus(status);
        DebitCard updatedCard = debitCardRepository.save(card);
        log.info("[AccountService] Card {} status changed from {} to {}", cardId, previousStatus, status);
        return ApiResponse.success(updatedCard, "Card status updated to " + status);
    }

    @Transactional
    public ApiResponse<DebitCard> updateCardLimit(Long cardId, BigDecimal limit) {
        log.info("[AccountService] Updating daily limit for cardId={} to {}", cardId, limit);
        DebitCard card = debitCardRepository.findById(cardId)
                .orElseThrow(() -> new ResourceNotFoundException("Debit card not found"));

        if (limit.compareTo(BigDecimal.ZERO) < 0) {
            log.warn("[AccountService] Negative daily limit rejected for cardId={}", cardId);
            throw new InvalidTransactionException("Daily limit must be positive");
        }

        BigDecimal oldLimit = card.getDailyLimit();
        card.setDailyLimit(limit);
        DebitCard updatedCard = debitCardRepository.save(card);
        log.info("[AccountService] Card {} daily limit updated from {} to {}", cardId, oldLimit, limit);
        return ApiResponse.success(updatedCard, "Daily limit updated successfully");
    }

    @Transactional
    public ApiResponse<String> changePin(Long cardId, String oldPin, String newPin) {
        log.info("[AccountService] PIN change request for cardId={}", cardId);
        DebitCard card = debitCardRepository.findById(cardId)
                .orElseThrow(() -> new ResourceNotFoundException("Debit card not found"));

        if (!card.getPinHash().equals(oldPin)) {
            log.warn("[AccountService] PIN change failed - incorrect old PIN for cardId={}", cardId);
            throw new UnauthorizedException("Incorrect old PIN");
        }

        card.setPinHash(newPin);
        debitCardRepository.save(card);
        log.info("[AccountService] PIN updated successfully for cardId={}", cardId);
        return ApiResponse.success("PIN updated successfully");
    }

    public ApiResponse<List<DebitCard>> getCardsByAccountId(Long accountId) {
        log.info("[AccountService] Fetching cards for accountId={}", accountId);
        return ApiResponse.success(debitCardRepository.findByAccountId(accountId));
    }

    public static class CreateAccountRequest {
        private Long customerId;
        private String accountType;
        private BigDecimal initialBalance;

        public Long getCustomerId() { return customerId; }
        public void setCustomerId(Long customerId) { this.customerId = customerId; }
        public String getAccountType() { return accountType; }
        public void setAccountType(String accountType) { this.accountType = accountType; }
        public BigDecimal getInitialBalance() { return initialBalance; }
        public void setInitialBalance(BigDecimal initialBalance) { this.initialBalance = initialBalance; }
    }
}
