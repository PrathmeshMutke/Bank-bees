package com.banking.customer.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "CUSTOMERS")
public class Customer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", unique = true, nullable = false)
    private Long userId;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(name = "date_of_birth", nullable = false)
    private LocalDate dateOfBirth;

    @Column(name = "pan_number", unique = true, nullable = false)
    private String panNumber;

    @Column(name = "aadhaar_number", unique = true, nullable = false)
    private String aadhaarNumber;

    @Column(nullable = false)
    private String status; // PENDING, APPROVED, REJECTED, SUSPENDED

    @Column(nullable = false)
    private String address;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) status = "PENDING";
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Customer() {}

    public Customer(Long id, Long userId, String firstName, String lastName, LocalDate dateOfBirth, String panNumber, String aadhaarNumber, String status, String address, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.userId = userId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.dateOfBirth = dateOfBirth;
        this.panNumber = panNumber;
        this.aadhaarNumber = aadhaarNumber;
        this.status = status;
        this.address = address;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static CustomerBuilder builder() {
        return new CustomerBuilder();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
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
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public static class CustomerBuilder {
        private Long id;
        private Long userId;
        private String firstName;
        private String lastName;
        private LocalDate dateOfBirth;
        private String panNumber;
        private String aadhaarNumber;
        private String status;
        private String address;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public CustomerBuilder id(Long id) { this.id = id; return this; }
        public CustomerBuilder userId(Long userId) { this.userId = userId; return this; }
        public CustomerBuilder firstName(String firstName) { this.firstName = firstName; return this; }
        public CustomerBuilder lastName(String lastName) { this.lastName = lastName; return this; }
        public CustomerBuilder dateOfBirth(LocalDate dateOfBirth) { this.dateOfBirth = dateOfBirth; return this; }
        public CustomerBuilder panNumber(String panNumber) { this.panNumber = panNumber; return this; }
        public CustomerBuilder aadhaarNumber(String aadhaarNumber) { this.aadhaarNumber = aadhaarNumber; return this; }
        public CustomerBuilder status(String status) { this.status = status; return this; }
        public CustomerBuilder address(String address) { this.address = address; return this; }
        public CustomerBuilder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public CustomerBuilder updatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; return this; }

        public Customer build() {
            return new Customer(id, userId, firstName, lastName, dateOfBirth, panNumber, aadhaarNumber, status, address, createdAt, updatedAt);
        }
    }
}
