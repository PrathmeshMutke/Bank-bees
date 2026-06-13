package com.banking.customer.controller;

import com.banking.customer.entity.Customer;
import com.banking.customer.entity.KycDocument;
import com.banking.customer.service.CustomerService;
import com.banking.customer.service.CustomerService.KycUploadRequest;
import com.banking.customer.service.CustomerService.OnboardRequest;
import com.banking.core.dto.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/customers")
public class CustomerController {

    private static final Logger log = LoggerFactory.getLogger(CustomerController.class);

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @PostMapping("/onboard")
    public ResponseEntity<ApiResponse<Customer>> onboard(@RequestBody OnboardRequest request) {
        log.info("POST /api/v1/customers/onboard - userId={}, name={} {}",
                request.getUserId(), request.getFirstName(), request.getLastName());
        ResponseEntity<ApiResponse<Customer>> response = ResponseEntity.ok(customerService.onboard(request));
        log.info("Customer onboarded successfully for userId={}", request.getUserId());
        return response;
    }

    @PostMapping("/{customerId}/kyc")
    public ResponseEntity<ApiResponse<KycDocument>> uploadKyc(
            @PathVariable Long customerId,
            @RequestBody KycUploadRequest request) {
        log.info("POST /api/v1/customers/{}/kyc - documentType={}", customerId, request.getDocumentType());
        return ResponseEntity.ok(customerService.uploadDocument(customerId, request));
    }

    @PutMapping("/{customerId}/kyc/{documentId}/verify")
    public ResponseEntity<ApiResponse<String>> verifyKyc(
            @PathVariable Long customerId,
            @PathVariable Long documentId,
            @RequestParam String status) {
        log.info("PUT /api/v1/customers/{}/kyc/{}/verify - status={}", customerId, documentId, status);
        return ResponseEntity.ok(customerService.verifyKyc(customerId, documentId, status));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<Customer>> getCustomerByUserId(@PathVariable Long userId) {
        log.info("GET /api/v1/customers/user/{} - Fetching customer profile", userId);
        return ResponseEntity.ok(customerService.getCustomerProfile(userId));
    }

    @GetMapping("/{customerId}/documents")
    public ResponseEntity<ApiResponse<List<KycDocument>>> getDocuments(@PathVariable Long customerId) {
        log.info("GET /api/v1/customers/{}/documents - Fetching KYC documents", customerId);
        return ResponseEntity.ok(customerService.getCustomerDocuments(customerId));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Customer>>> getAllCustomers() {
        log.info("GET /api/v1/customers - Fetching all customers");
        return ResponseEntity.ok(customerService.getAllCustomers());
    }
}
