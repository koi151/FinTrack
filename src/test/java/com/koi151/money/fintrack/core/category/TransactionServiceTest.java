package com.koi151.money.fintrack.core.category;

import com.koi151.money.fintrack.common.exception.AppException;
import com.koi151.money.fintrack.common.exception.ErrorCode;
import com.koi151.money.fintrack.core.transaction.Transaction;
import com.koi151.money.fintrack.core.transaction.TransactionMapper;
import com.koi151.money.fintrack.core.transaction.TransactionRepository;
import com.koi151.money.fintrack.core.transaction.TransactionService;
import com.koi151.money.fintrack.core.transaction.payload.TransactionRequest;
import com.koi151.money.fintrack.core.transaction.payload.TransactionResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private TransactionMapper transactionMapper;

    @InjectMocks
    private TransactionService transactionService;

    // Common Test Values
    private static final BigDecimal DEFAULT_AMOUNT = BigDecimal.valueOf(100.0);
    private static final String DEFAULT_CATEGORY_NAME = "Food & Dining";

    @Nested
    @DisplayName("Tests for createTransaction")
    class CreateTransactionTests {

        @Test
        @DisplayName("Should create transaction successfully when request is valid")
        void createTransaction_ValidRequest_ReturnsResponse() {
            // Given
            UUID categoryId = UUID.randomUUID();
            UUID transactionId = UUID.randomUUID();

            TransactionRequest request = TransactionRequest.builder()
                .amount(DEFAULT_AMOUNT)
                .categoryId(categoryId)
                .build();

            Transaction transaction = new Transaction();
            Transaction savedTransaction = Transaction.builder()
                    .id(transactionId)
                    .build();

            TransactionResponse expectedResponse = TransactionResponse.builder()
                .id(transactionId)
                .amount(DEFAULT_AMOUNT)
                .categoryId(categoryId)
                .categoryName(DEFAULT_CATEGORY_NAME)
                .build();

            given(transactionMapper.toEntity(request)).willReturn(transaction);
            given(transactionRepository.save(transaction)).willReturn(savedTransaction);
            given(transactionMapper.toResponse(savedTransaction)).willReturn(expectedResponse);

            // When
            TransactionResponse result = transactionService.createTransaction(request);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(transactionId);
            assertThat(result.getAmount()).isEqualTo(DEFAULT_AMOUNT);
            assertThat(result).isEqualTo(expectedResponse);

            verify(transactionRepository, times(1)).save(any(Transaction.class));
        }

        @Test
        @DisplayName("Should throw exception when Category is not found during mapping")
        void createTransaction_CategoryNotFound_ThrowsException() {
            // Given
            TransactionRequest request = TransactionRequest.builder()
                .amount(DEFAULT_AMOUNT)
                .categoryId(UUID.randomUUID())
                .build();

            given(transactionMapper.toEntity(request))
                .willThrow(new AppException(ErrorCode.CATEGORY_NOT_FOUND));

            // When & Then
            assertThatThrownBy(() -> transactionService.createTransaction(request))
                .isInstanceOf(AppException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.CATEGORY_NOT_FOUND);

            verify(transactionRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw exception when amount is null")
        void createTransaction_NullAmount_ThrowsException() {
            // Given
            TransactionRequest request = TransactionRequest.builder()
                .amount(null)
                .categoryId(UUID.randomUUID())
                .build();

            // When & Then
            assertThatThrownBy(() -> transactionService.createTransaction(request))
                .isInstanceOf(AppException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_PARAM);
        }

        @Test
        @DisplayName("Should throw exception when amount is negative")
        void createTransaction_NegativeAmount_ThrowsException() {
            // Given
            TransactionRequest request = TransactionRequest.builder()
                .amount(DEFAULT_AMOUNT.negate())
                .categoryId(UUID.randomUUID())
                .build();

            // When & Then
            assertThatThrownBy(() -> transactionService.createTransaction(request))
                .isInstanceOf(AppException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_PARAM);

            verify(transactionRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Tests for deleteTransaction")
    class DeleteTransactionTests {

        @Test
        @DisplayName("Should delete successfully when transaction exists")
        void deleteTransaction_Exists_Success() {
            // Given
            UUID id = UUID.randomUUID();
            Transaction transaction = new Transaction();
            given(transactionRepository.findById(id)).willReturn(Optional.of(transaction));

            // When
            transactionService.deleteTransaction(id);

            // Then
            verify(transactionRepository, times(1)).delete(transaction);
        }

        @Test
        @DisplayName("Should handle delete gracefully if transaction already gone")
        void deleteTransaction_AlreadyDeleted_ThrowsNotFound() {
            // Given
            UUID id = UUID.randomUUID();
            given(transactionRepository.findById(id)).willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> transactionService.deleteTransaction(id))
                .isInstanceOf(AppException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.TRANSACTION_NOT_FOUND);

            verify(transactionRepository, never()).delete(any());
        }
    }
}