package com.banking.transaction.service;

import com.banking.transaction.entity.Transaction;
import com.banking.transaction.entity.TransactionHistory;
import com.banking.transaction.entity.OutboxMessage;
import com.banking.transaction.repository.TransactionHistoryRepository;
import com.banking.transaction.repository.TransactionRepository;
import com.banking.transaction.repository.OutboxRepository;
import com.banking.core.dto.ApiResponse;
import com.banking.core.exception.CustomExceptions.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import java.sql.CallableStatement;
import java.sql.Types;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Service
public class TransactionService {

    private static final Logger log = LoggerFactory.getLogger(TransactionService.class);

    private final TransactionRepository transactionRepository;
    private final TransactionHistoryRepository historyRepository;
    private final OutboxRepository outboxRepository;
    private final JdbcTemplate jdbcTemplate;

    public TransactionService(TransactionRepository transactionRepository, TransactionHistoryRepository historyRepository, OutboxRepository outboxRepository, JdbcTemplate jdbcTemplate) {
        this.transactionRepository = transactionRepository;
        this.historyRepository = historyRepository;
        this.outboxRepository = outboxRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional
    public ApiResponse<String> transferFunds(TransferRequest request) {
        log.info("[TransactionService] Initiating fund transfer: src={}, dest={}, amount={}, channel={}, type={}",
                request.getSourceAccountId(), request.getDestinationAccountId(),
                request.getAmount(), request.getChannel(), request.getTransactionType());
        try {
            String[] txnRefHolder = new String[1];
            jdbcTemplate.execute((java.sql.Connection con) -> {
                CallableStatement cs = con.prepareCall(
                    "{call PKG_CORE_BANKING.PRC_TRANSFER_FUNDS(?,?,?,?,?,?,?)}");
                cs.setLong(1, request.getSourceAccountId());
                cs.setLong(2, request.getDestinationAccountId());
                cs.setBigDecimal(3, request.getAmount());
                cs.setString(4, request.getTransactionType());
                cs.setString(5, request.getChannel());
                cs.setString(6, request.getDescription());
                cs.registerOutParameter(7, Types.VARCHAR);
                cs.execute();
                txnRefHolder[0] = cs.getString(7);
                return cs;
            });
            log.info("[TransactionService] Fund transfer completed successfully. txnRef={}, src={}, dest={}, amount={}",
                    txnRefHolder[0], request.getSourceAccountId(), request.getDestinationAccountId(), request.getAmount());
            return ApiResponse.success(txnRefHolder[0], "Fund transfer processed successfully");
        } catch (Exception e) {
            log.error("[TransactionService] Fund transfer FAILED: src={}, dest={}, amount={}, error={}",
                    request.getSourceAccountId(), request.getDestinationAccountId(), request.getAmount(), e.getMessage(), e);
            throw new InvalidTransactionException("Transfer failed: " + e.getMessage());
        }
    }

    @Transactional
    public ApiResponse<Transaction> deposit(DepositRequest request) {
        log.info("[TransactionService] Processing deposit: accountId={}, amount={}, desc={}",
                request.getAccountId(), request.getAmount(), request.getDescription());
        try {
            String txnRef = "TXN" + (100000000000L + new java.util.Random().nextLong(900000000000L));
            
            jdbcTemplate.update("UPDATE ACCOUNT_BALANCES SET ledger_balance = ledger_balance + ?, available_balance = available_balance + ?, version = version + 1 WHERE account_id = ?",
                    request.getAmount(), request.getAmount(), request.getAccountId());
            log.info("[TransactionService] Balance updated for accountId={}, deposited={}", request.getAccountId(), request.getAmount());
            
            Transaction txn = Transaction.builder()
                    .transactionReference(txnRef)
                    .destinationAccountId(request.getAccountId())
                    .amount(request.getAmount())
                    .transactionType("DEPOSIT")
                    .channel("WEB")
                    .status("SUCCESS")
                    .description(request.getDescription())
                    .build();
            
            Transaction savedTxn = transactionRepository.save(txn);
            log.info("[TransactionService] Deposit transaction persisted: id={}, ref={}", savedTxn.getId(), txnRef);
            
            BigDecimal balanceAfter = jdbcTemplate.queryForObject(
                    "SELECT available_balance FROM ACCOUNT_BALANCES WHERE account_id = ?",
                    BigDecimal.class, request.getAccountId());

            historyRepository.save(TransactionHistory.builder()
                    .transactionId(savedTxn.getId())
                    .accountId(request.getAccountId())
                    .balanceAfterTxn(balanceAfter)
                    .entryType("CREDIT")
                    .build());
            log.info("[TransactionService] Transaction history entry created. balanceAfter={} for accountId={}", balanceAfter, request.getAccountId());

            outboxRepository.save(OutboxMessage.builder()
                    .aggregateType("TRANSACTION")
                    .aggregateId(savedTxn.getId().toString())
                    .eventType("TransactionSuccessEvent")
                    .payload("{\"transactionId\":" + savedTxn.getId() + ",\"ref\":\"" + txnRef + "\",\"amount\":" + request.getAmount() + ",\"dest\":" + request.getAccountId() + "}")
                    .status("PENDING")
                    .build());
            log.info("[TransactionService] Outbox event published for txnId={}", savedTxn.getId());

            return ApiResponse.success(savedTxn, "Deposit successful");
        } catch (Exception e) {
            log.error("[TransactionService] Deposit FAILED for accountId={}, amount={}, error={}",
                    request.getAccountId(), request.getAmount(), e.getMessage(), e);
            throw new InvalidTransactionException("Deposit failed: " + e.getMessage());
        }
    }

    public ApiResponse<List<Transaction>> getTransactionHistory(Long accountId) {
        log.info("[TransactionService] Fetching transaction history for accountId={}", accountId);
        List<Transaction> txns = transactionRepository.findBySourceAccountIdOrDestinationAccountIdOrderByTransactionDateDesc(accountId, accountId);
        log.info("[TransactionService] Found {} transaction(s) for accountId={}", txns.size(), accountId);
        return ApiResponse.success(txns);
    }

    public static class TransferRequest {
        private Long sourceAccountId;
        private Long destinationAccountId;
        private BigDecimal amount;
        private String transactionType;
        private String channel;
        private String description;

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
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }

    public static class DepositRequest {
        private Long accountId;
        private BigDecimal amount;
        private String description;

        public Long getAccountId() { return accountId; }
        public void setAccountId(Long accountId) { this.accountId = accountId; }
        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }
}
