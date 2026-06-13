package com.banking.customer.repository;

import com.banking.customer.entity.KycDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface KycDocumentRepository extends JpaRepository<KycDocument, Long> {
    List<KycDocument> findByCustomerId(Long customerId);
}
