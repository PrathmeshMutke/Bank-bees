package com.banking.customer.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "KYC_DOCUMENTS")
public class KycDocument {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    @Column(name = "document_type", nullable = false)
    private String documentType; // AADHAAR, PAN, PASSPORT, DRIVING_LICENSE

    @Column(name = "document_url", nullable = false)
    private String documentUrl;

    @Column(nullable = false)
    private String status; // PENDING, VERIFIED, REJECTED

    @Column(name = "uploaded_at", nullable = false)
    private LocalDateTime uploadedAt;

    @PrePersist
    protected void onCreate() {
        uploadedAt = LocalDateTime.now();
        if (status == null) status = "PENDING";
    }

    public KycDocument() {}

    public KycDocument(Long id, Long customerId, String documentType, String documentUrl, String status, LocalDateTime uploadedAt) {
        this.id = id;
        this.customerId = customerId;
        this.documentType = documentType;
        this.documentUrl = documentUrl;
        this.status = status;
        this.uploadedAt = uploadedAt;
    }

    public static KycDocumentBuilder builder() {
        return new KycDocumentBuilder();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }
    public String getDocumentType() { return documentType; }
    public void setDocumentType(String documentType) { this.documentType = documentType; }
    public String getDocumentUrl() { return documentUrl; }
    public void setDocumentUrl(String documentUrl) { this.documentUrl = documentUrl; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getUploadedAt() { return uploadedAt; }
    public void setUploadedAt(LocalDateTime uploadedAt) { this.uploadedAt = uploadedAt; }

    public static class KycDocumentBuilder {
        private Long id;
        private Long customerId;
        private String documentType;
        private String documentUrl;
        private String status;
        private LocalDateTime uploadedAt;

        public KycDocumentBuilder id(Long id) { this.id = id; return this; }
        public KycDocumentBuilder customerId(Long customerId) { this.customerId = customerId; return this; }
        public KycDocumentBuilder documentType(String documentType) { this.documentType = documentType; return this; }
        public KycDocumentBuilder documentUrl(String documentUrl) { this.documentUrl = documentUrl; return this; }
        public KycDocumentBuilder status(String status) { this.status = status; return this; }
        public KycDocumentBuilder uploadedAt(LocalDateTime uploadedAt) { this.uploadedAt = uploadedAt; return this; }

        public KycDocument build() {
            return new KycDocument(id, customerId, documentType, documentUrl, status, uploadedAt);
        }
    }
}
