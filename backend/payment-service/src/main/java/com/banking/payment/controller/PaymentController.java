package com.banking.payment.controller;

import com.banking.core.dto.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/payments")
public class PaymentController {

    private static final Logger log = LoggerFactory.getLogger(PaymentController.class);

    @PostMapping("/charge")
    public ResponseEntity<ApiResponse<PaymentTxnResponse>> chargeCard(@RequestBody PaymentChargeRequest request) {
        log.info("POST /api/v1/payments/charge - accountId={}, amount={}, provider={}",
                request.getAccountId(), request.getAmount(), request.getProvider());
        // Mock processing charge
        String externalId = "ch_" + UUID.randomUUID().toString().substring(0, 16);
        PaymentTxnResponse response = new PaymentTxnResponse();
        response.setTransactionId(externalId);
        response.setAmount(request.getAmount());
        response.setProvider(request.getProvider());
        response.setStatus("SUCCESS");

        log.info("Payment charged successfully: txnId={}, amount={}, provider={}",
                externalId, request.getAmount(), request.getProvider());
        return ResponseEntity.ok(ApiResponse.success(response, "Payment charged successfully via " + request.getProvider()));
    }

    @PostMapping("/refund/{txnId}")
    public ResponseEntity<ApiResponse<String>> refundCard(
            @PathVariable String txnId,
            @RequestParam BigDecimal amount) {
        log.info("POST /api/v1/payments/refund/{} - amount={}", txnId, amount);
        log.info("Refund processed for txnId={}, amount={}", txnId, amount);
        return ResponseEntity.ok(ApiResponse.success("Refund of " + amount + " processed successfully for external reference ID: " + txnId));
    }

    public static class PaymentChargeRequest {
        private String accountId;
        private BigDecimal amount;
        private String provider; // STRIPE, RAZORPAY
        private String token; // Stripe token representation

        public String getAccountId() { return accountId; }
        public void setAccountId(String accountId) { this.accountId = accountId; }
        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }
        public String getProvider() { return provider; }
        public void setProvider(String provider) { this.provider = provider; }
        public String getToken() { return token; }
        public void setToken(String token) { this.token = token; }
    }

    public static class PaymentTxnResponse {
        private String transactionId;
        private BigDecimal amount;
        private String provider;
        private String status;

        public String getTransactionId() { return transactionId; }
        public void setTransactionId(String transactionId) { this.transactionId = transactionId; }
        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }
        public String getProvider() { return provider; }
        public void setProvider(String provider) { this.provider = provider; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }
}
