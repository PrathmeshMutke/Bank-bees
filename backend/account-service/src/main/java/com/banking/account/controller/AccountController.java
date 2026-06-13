package com.banking.account.controller;

import com.banking.account.entity.Account;
import com.banking.account.entity.AccountBalance;
import com.banking.account.entity.DebitCard;
import com.banking.account.service.AccountService;
import com.banking.account.service.AccountService.CreateAccountRequest;
import com.banking.core.dto.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/accounts")
public class AccountController {

    private static final Logger log = LoggerFactory.getLogger(AccountController.class);

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Account>> createAccount(@RequestBody CreateAccountRequest request) {
        log.info("POST /api/v1/accounts - Creating account for customerId={}, type={}", request.getCustomerId(), request.getAccountType());
        ResponseEntity<ApiResponse<Account>> response = ResponseEntity.ok(accountService.createAccount(request));
        log.info("Account created successfully for customerId={}", request.getCustomerId());
        return response;
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<ApiResponse<List<Account>>> getAccountsByCustomerId(@PathVariable Long customerId) {
        log.info("GET /api/v1/accounts/customer/{} - Fetching accounts", customerId);
        return ResponseEntity.ok(accountService.getAccountsByCustomerId(customerId));
    }

    @GetMapping("/{accountId}/balance")
    public ResponseEntity<ApiResponse<AccountBalance>> getBalance(@PathVariable Long accountId) {
        log.info("GET /api/v1/accounts/{}/balance - Fetching balance", accountId);
        return ResponseEntity.ok(accountService.getBalance(accountId));
    }

    @PostMapping("/{accountId}/cards")
    public ResponseEntity<ApiResponse<DebitCard>> issueCard(
            @PathVariable Long accountId,
            @RequestParam boolean isVirtual) {
        log.info("POST /api/v1/accounts/{}/cards - Issuing {} card", accountId, isVirtual ? "virtual" : "physical");
        ResponseEntity<ApiResponse<DebitCard>> response = ResponseEntity.ok(accountService.issueCard(accountId, isVirtual));
        log.info("Card issued successfully for accountId={}", accountId);
        return response;
    }

    @GetMapping("/{accountId}/cards")
    public ResponseEntity<ApiResponse<List<DebitCard>>> getCards(@PathVariable Long accountId) {
        log.info("GET /api/v1/accounts/{}/cards - Fetching cards", accountId);
        return ResponseEntity.ok(accountService.getCardsByAccountId(accountId));
    }

    @PutMapping("/cards/{cardId}/status")
    public ResponseEntity<ApiResponse<DebitCard>> updateCardStatus(
            @PathVariable Long cardId,
            @RequestParam String status) {
        log.info("PUT /api/v1/accounts/cards/{}/status - Updating card status to {}", cardId, status);
        ResponseEntity<ApiResponse<DebitCard>> response = ResponseEntity.ok(accountService.updateCardStatus(cardId, status));
        log.info("Card {} status updated to {}", cardId, status);
        return response;
    }

    @PutMapping("/cards/{cardId}/limit")
    public ResponseEntity<ApiResponse<DebitCard>> updateCardLimit(
            @PathVariable Long cardId,
            @RequestParam BigDecimal limit) {
        log.info("PUT /api/v1/accounts/cards/{}/limit - Updating daily limit to {}", cardId, limit);
        return ResponseEntity.ok(accountService.updateCardLimit(cardId, limit));
    }

    @PutMapping("/cards/{cardId}/pin")
    public ResponseEntity<ApiResponse<String>> changePin(
            @PathVariable Long cardId,
            @RequestParam String oldPin,
            @RequestParam String newPin) {
        log.info("PUT /api/v1/accounts/cards/{}/pin - PIN change requested", cardId);
        ResponseEntity<ApiResponse<String>> response = ResponseEntity.ok(accountService.changePin(cardId, oldPin, newPin));
        log.info("PIN changed successfully for cardId={}", cardId);
        return response;
    }
}
