package com.banking.account.repository;

import com.banking.account.entity.DebitCard;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface DebitCardRepository extends JpaRepository<DebitCard, Long> {
    Optional<DebitCard> findByCardNumber(String cardNumber);
    List<DebitCard> findByAccountId(Long accountId);
}
