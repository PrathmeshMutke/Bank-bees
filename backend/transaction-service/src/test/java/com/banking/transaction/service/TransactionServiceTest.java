package com.banking.transaction.service;

import com.banking.transaction.entity.Transaction;
import com.banking.transaction.repository.TransactionHistoryRepository;
import com.banking.transaction.repository.TransactionRepository;
import com.banking.transaction.repository.OutboxRepository;
import com.banking.transaction.service.TransactionService.DepositRequest;
import com.banking.transaction.service.TransactionService.TransferRequest;
import com.banking.core.dto.ApiResponse;
import com.banking.core.exception.CustomExceptions.InvalidTransactionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private TransactionHistoryRepository historyRepository;

    @Mock
    private OutboxRepository outboxRepository;

    @Mock
    private JdbcTemplate jdbcTemplate;

    @InjectMocks
    private TransactionService transactionService;

    private TransferRequest transferRequest;
    private DepositRequest depositRequest;

    @BeforeEach
    public void setup() {
        transferRequest = new TransferRequest();
        transferRequest.setSourceAccountId(1L);
        transferRequest.setDestinationAccountId(2L);
        transferRequest.setAmount(new BigDecimal("5000.00"));
        transferRequest.setTransactionType("TRANSFER");
        transferRequest.setChannel("IMPS");
        transferRequest.setDescription("Monthly Rent Payment");

        depositRequest = new DepositRequest();
        depositRequest.setAccountId(1L);
        depositRequest.setAmount(new BigDecimal("1000.00"));
        depositRequest.setDescription("Cash Deposit");
    }

    @Test
    public void testDeposit_Success() {
        // Arrange
        when(jdbcTemplate.update(anyString(), any(), any(), any())).thenReturn(1);
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> {
            Transaction txn = invocation.getArgument(0);
            txn.setId(10L);
            return txn;
        });
        when(jdbcTemplate.queryForObject(anyString(), eq(BigDecimal.class), eq(1L)))
                .thenReturn(new BigDecimal("11000.00"));

        // Act
        ApiResponse<Transaction> response = transactionService.deposit(depositRequest);

        // Assert
        assertTrue(response.isSuccess());
        assertNotNull(response.getData());
        assertEquals(10L, response.getData().getId());
        assertEquals(new BigDecimal("1000.00"), response.getData().getAmount());
        assertEquals("DEPOSIT", response.getData().getTransactionType());

        verify(jdbcTemplate, times(1)).update(anyString(), eq(new BigDecimal("1000.00")), eq(new BigDecimal("1000.00")), eq(1L));
        verify(transactionRepository, times(1)).save(any(Transaction.class));
        verify(historyRepository, times(1)).save(any());
        verify(outboxRepository, times(1)).save(any());
    }

    @Test
    public void testDeposit_Failure_ThrowsException() {
        // Arrange
        when(jdbcTemplate.update(anyString(), any(), any(), any())).thenThrow(new RuntimeException("Database error"));

        // Act & Assert
        assertThrows(InvalidTransactionException.class, () -> {
            transactionService.deposit(depositRequest);
        });

        verify(transactionRepository, never()).save(any());
        verify(historyRepository, never()).save(any());
        verify(outboxRepository, never()).save(any());
    }
}
