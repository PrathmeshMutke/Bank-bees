package com.banking.transaction.controller;

import com.banking.transaction.entity.Transaction;
import com.banking.transaction.service.TransactionService;
import com.banking.transaction.service.TransactionService.DepositRequest;
import com.banking.transaction.service.TransactionService.TransferRequest;
import com.banking.core.dto.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/transactions")
public class TransactionController {

    private static final Logger log = LoggerFactory.getLogger(TransactionController.class);

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping("/transfer")
    public ResponseEntity<ApiResponse<String>> transfer(@RequestBody TransferRequest request) {
        log.info("POST /api/v1/transactions/transfer - src={}, dest={}, amount={}, channel={}",
                request.getSourceAccountId(), request.getDestinationAccountId(),
                request.getAmount(), request.getChannel());
        ResponseEntity<ApiResponse<String>> response = ResponseEntity.ok(transactionService.transferFunds(request));
        log.info("Transfer request processed: src={}, dest={}, amount={}",
                request.getSourceAccountId(), request.getDestinationAccountId(), request.getAmount());
        return response;
    }

    @PostMapping("/deposit")
    public ResponseEntity<ApiResponse<Transaction>> deposit(@RequestBody DepositRequest request) {
        log.info("POST /api/v1/transactions/deposit - accountId={}, amount={}",
                request.getAccountId(), request.getAmount());
        ResponseEntity<ApiResponse<Transaction>> response = ResponseEntity.ok(transactionService.deposit(request));
        log.info("Deposit request processed for accountId={}, amount={}", request.getAccountId(), request.getAmount());
        return response;
    }

    @GetMapping("/account/{accountId}")
    public ResponseEntity<ApiResponse<List<Transaction>>> getHistory(@PathVariable Long accountId) {
        log.info("GET /api/v1/transactions/account/{} - Fetching transaction history", accountId);
        return ResponseEntity.ok(transactionService.getTransactionHistory(accountId));
    }
}
