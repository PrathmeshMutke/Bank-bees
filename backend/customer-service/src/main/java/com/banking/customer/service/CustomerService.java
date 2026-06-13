package com.banking.customer.service;

import com.banking.customer.entity.Customer;
import com.banking.customer.entity.KycDocument;
import com.banking.customer.repository.CustomerRepository;
import com.banking.customer.repository.KycDocumentRepository;
import com.banking.core.dto.ApiResponse;
import com.banking.core.exception.CustomExceptions.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class CustomerService {

    private static final Logger log = LoggerFactory.getLogger(CustomerService.class);

    private final CustomerRepository customerRepository;
    private final KycDocumentRepository kycDocumentRepository;

    public CustomerService(CustomerRepository customerRepository, KycDocumentRepository kycDocumentRepository) {
        this.customerRepository = customerRepository;
        this.kycDocumentRepository = kycDocumentRepository;
    }

    @Transactional
    public ApiResponse<Customer> onboard(OnboardRequest request) {
        log.info("[CustomerService] Onboarding customer for userId={}, name={} {}", request.getUserId(), request.getFirstName(), request.getLastName());

        if (customerRepository.findByUserId(request.getUserId()).isPresent()) {
            log.warn("[CustomerService] Onboarding rejected - profile already exists for userId={}", request.getUserId());
            throw new InvalidTransactionException("Customer profile already exists for user ID: " + request.getUserId());
        }
        if (customerRepository.findByPanNumber(request.getPanNumber()).isPresent()) {
            log.warn("[CustomerService] Onboarding rejected - PAN already registered: {}", request.getPanNumber());
            throw new InvalidTransactionException("PAN number is already registered");
        }

        Customer customer = Customer.builder()
                .userId(request.getUserId())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .dateOfBirth(request.getDateOfBirth())
                .panNumber(request.getPanNumber().toUpperCase())
                .aadhaarNumber(request.getAadhaarNumber())
                .status("PENDING")
                .address(request.getAddress())
                .build();

        Customer savedCustomer = customerRepository.save(customer);
        log.info("[CustomerService] Customer onboarded with id={} for userId={}, status=PENDING", savedCustomer.getId(), request.getUserId());
        return ApiResponse.success(savedCustomer, "Customer onboarding initiated successfully");
    }

    @Transactional
    public ApiResponse<KycDocument> uploadDocument(Long customerId, KycUploadRequest request) {
        log.info("[CustomerService] KYC document upload for customerId={}, type={}", customerId, request.getDocumentType());

        customerRepository.findById(customerId)
                .orElseThrow(() -> {
                    log.warn("[CustomerService] KYC upload failed - customer not found: {}", customerId);
                    return new ResourceNotFoundException("Customer not found");
                });

        KycDocument doc = KycDocument.builder()
                .customerId(customerId)
                .documentType(request.getDocumentType())
                .documentUrl(request.getDocumentUrl())
                .status("PENDING")
                .build();

        KycDocument savedDoc = kycDocumentRepository.save(doc);
        log.info("[CustomerService] KYC document saved with id={} for customerId={}", savedDoc.getId(), customerId);
        return ApiResponse.success(savedDoc, "KYC Document uploaded successfully");
    }

    @Transactional
    public ApiResponse<String> verifyKyc(Long customerId, Long documentId, String status) {
        log.info("[CustomerService] KYC verification - customerId={}, documentId={}, decision={}", customerId, documentId, status);

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> {
                    log.warn("[CustomerService] KYC verify failed - customer not found: {}", customerId);
                    return new ResourceNotFoundException("Customer not found");
                });

        KycDocument doc = kycDocumentRepository.findById(documentId)
                .orElseThrow(() -> {
                    log.warn("[CustomerService] KYC verify failed - document not found: {}", documentId);
                    return new ResourceNotFoundException("Document not found");
                });

        if (!doc.getCustomerId().equals(customerId)) {
            log.warn("[CustomerService] Document {} does not belong to customer {}", documentId, customerId);
            throw new InvalidTransactionException("Document does not belong to customer");
        }

        doc.setStatus(status);
        kycDocumentRepository.save(doc);

        if ("VERIFIED".equals(status)) {
            customer.setStatus("APPROVED");
            customerRepository.save(customer);
            log.info("[CustomerService] Customer {} KYC APPROVED - status set to APPROVED", customerId);
            return ApiResponse.success("Customer KYC verified and approved");
        } else if ("REJECTED".equals(status)) {
            customer.setStatus("REJECTED");
            customerRepository.save(customer);
            log.info("[CustomerService] Customer {} KYC REJECTED - status set to REJECTED", customerId);
            return ApiResponse.success("Customer KYC rejected");
        }

        log.info("[CustomerService] Document {} status updated to {} for customerId={}", documentId, status, customerId);
        return ApiResponse.success("KYC Document status updated to: " + status);
    }

    public ApiResponse<Customer> getCustomerProfile(Long userId) {
        log.info("[CustomerService] Fetching customer profile for userId={}", userId);
        Customer customer = customerRepository.findByUserId(userId)
                .orElseThrow(() -> {
                    log.warn("[CustomerService] Customer profile not found for userId={}", userId);
                    return new ResourceNotFoundException("Customer profile not found");
                });
        log.info("[CustomerService] Found customer id={} for userId={}, status={}", customer.getId(), userId, customer.getStatus());
        return ApiResponse.success(customer);
    }

    public ApiResponse<List<KycDocument>> getCustomerDocuments(Long customerId) {
        log.info("[CustomerService] Fetching KYC documents for customerId={}", customerId);
        List<KycDocument> docs = kycDocumentRepository.findByCustomerId(customerId);
        log.info("[CustomerService] Found {} KYC document(s) for customerId={}", docs.size(), customerId);
        return ApiResponse.success(docs);
    }

    public ApiResponse<List<Customer>> getAllCustomers() {
        log.info("[CustomerService] Fetching all customers");
        List<Customer> customers = customerRepository.findAll();
        log.info("[CustomerService] Total customers fetched: {}", customers.size());
        return ApiResponse.success(customers);
    }

    public static class OnboardRequest {
        private Long userId;
        private String firstName;
        private String lastName;
        private LocalDate dateOfBirth;
        private String panNumber;
        private String aadhaarNumber;
        private String address;

        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        public String getFirstName() { return firstName; }
        public void setFirstName(String firstName) { this.firstName = firstName; }
        public String getLastName() { return lastName; }
        public void setLastName(String lastName) { this.lastName = lastName; }
        public LocalDate getDateOfBirth() { return dateOfBirth; }
        public void setDateOfBirth(LocalDate dateOfBirth) { this.dateOfBirth = dateOfBirth; }
        public String getPanNumber() { return panNumber; }
        public void setPanNumber(String panNumber) { this.panNumber = panNumber; }
        public String getAadhaarNumber() { return aadhaarNumber; }
        public void setAadhaarNumber(String aadhaarNumber) { this.aadhaarNumber = aadhaarNumber; }
        public String getAddress() { return address; }
        public void setAddress(String address) { this.address = address; }
    }

    public static class KycUploadRequest {
        private String documentType;
        private String documentUrl;

        public String getDocumentType() { return documentType; }
        public void setDocumentType(String documentType) { this.documentType = documentType; }
        public String getDocumentUrl() { return documentUrl; }
        public void setDocumentUrl(String documentUrl) { this.documentUrl = documentUrl; }
    }
}
