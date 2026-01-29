package com.koi151.money.fintrack.core.transaction;

import com.koi151.money.fintrack.common.exception.AppException;
import com.koi151.money.fintrack.common.exception.ErrorCode;
import com.koi151.money.fintrack.core.category.Category;
import com.koi151.money.fintrack.core.category.CategoryRepository;
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
import java.util.List;
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

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private TransactionValidator transactionValidator;

    @InjectMocks
    private TransactionService transactionService;

    // Common Test Values
    private static final BigDecimal DEFAULT_AMOUNT = BigDecimal.valueOf(100.0);
    private static final String DEFAULT_CATEGORY_NAME = "Food & Dining";

    @Nested
    @DisplayName("Tests for getTransactions")
    class GetTransactionsTests {

        @Test
        @DisplayName("Should return a list of transactions when records exist")
        void getTransactions_RecordsExist_ReturnsList() {
            // Given -------------------
            // existing transactions
            Transaction transaction1 = Transaction.builder()
                .id(UUID.randomUUID())
                .build();
            Transaction transaction2 = Transaction.builder()
                .id(UUID.randomUUID())
                .build();

            List<Transaction> transactions = List.of(transaction1, transaction2);

            // mapped responses
            TransactionResponse response1 = TransactionResponse.builder()
                .id(transaction1.getId())
                .build();
            TransactionResponse response2 = TransactionResponse.builder()
                .id(transaction2.getId())
                .build();

            given(transactionRepository.findAll()).willReturn(transactions);
            given(transactionMapper.toResponse(transaction1)).willReturn(response1);
            given(transactionMapper.toResponse(transaction2)).willReturn(response2);

            // When ----------------
            List<TransactionResponse> result = transactionService.getTransactions();

            // Then ----------------
            assertThat(result)
                .hasSize(2)
                .containsExactly(response1, response2);

            verify(transactionRepository, times(1)).findAll();
        }

        @Test
        @DisplayName("Should return an empty list when no records exist")
        void getTransactions_NoRecords_ReturnsEmptyList() {
            // Given
            given(transactionRepository.findAll()).willReturn(List.of());

            // When
            List<TransactionResponse> result = transactionService.getTransactions();

            // Then
            assertThat(result).isNotNull().isEmpty();
            verify(transactionRepository, times(1)).findAll();
            verify(transactionMapper, never()).toResponse(any());
        }
    }

    @Nested
    @DisplayName("Tests for get single transaction")
    class GetTransactionDetailTests {

        @Test
        @DisplayName("Should return transaction response when ID exists")
        void getTransaction_IdExists_ReturnsResponse() {
            // Given
            UUID id = UUID.randomUUID();
            Transaction transaction = Transaction.builder().id(id).build();
            TransactionResponse response = TransactionResponse.builder().id(id).build();

            given(transactionRepository.findById(id)).willReturn(Optional.of(transaction));
            given(transactionMapper.toResponse(transaction)).willReturn(response);

            // When & Then
            TransactionResponse result = transactionService.getTransaction(id);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(id);
        }

        @Test
        @DisplayName("Should throw exception when transaction ID is not found")
        void getTransaction_NotFound_ThrowsException() {
            // Given
            UUID id = UUID.randomUUID();
            given(transactionRepository.findById(id)).willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> transactionService.getTransaction(id))
                .isInstanceOf(AppException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.TRANSACTION_NOT_FOUND);
        }
    }

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

            doThrow(new AppException(ErrorCode.INVALID_PARAM))
                .when(transactionValidator).validateForCreate(request);

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

            doThrow(new AppException(ErrorCode.INVALID_PARAM))
                .when(transactionValidator).validateForCreate(request);

            // When & Then
            assertThatThrownBy(() -> transactionService.createTransaction(request))
                .isInstanceOf(AppException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_PARAM);

            verify(transactionRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Tests for updateTransaction")
    class UpdateTransactionTests {

        @Test
        @DisplayName("Should update simple fields only (category unchanged) -> Skip proxy fetch")
        void updateTransaction_NoCategoryChange_Success() {
            // Given ---------------
            UUID transactionId = UUID.randomUUID();
            UUID categoryId = UUID.randomUUID();

            TransactionRequest request = TransactionRequest.builder()
                .amount(BigDecimal.valueOf(200.0))
                .categoryId(categoryId) // the same categoryId as existing
                .build();

            Category currentCategory = Category.builder()
                .id(categoryId)
                .build();

            Transaction existingTransaction = Transaction.builder()
                .id(transactionId)
                .category(currentCategory)
                .amount(DEFAULT_AMOUNT)
                .build();

            // updated after save
            Transaction updatedTransaction = Transaction.builder()
                .id(transactionId)
                .amount(BigDecimal.valueOf(200.0))
                .build();

            TransactionResponse expectedResponse = TransactionResponse.builder()
                .id(transactionId)
                .amount(BigDecimal.valueOf(200.0))
                .build();

            given(transactionRepository.findById(transactionId))
                .willReturn(Optional.of(existingTransaction));

            doNothing().when(transactionValidator).validateForUpdate(existingTransaction, request);

            // Mock save & response
            given(transactionRepository.save(existingTransaction))
                .willReturn(updatedTransaction);
            given(transactionMapper.toResponse(updatedTransaction))
                .willReturn(expectedResponse);

            // When ----------------
            TransactionResponse result = transactionService.updateTransaction(transactionId, request);

            // Then ----------------
            assertThat(result).isEqualTo(expectedResponse);

            verify(categoryRepository, never()).getReferenceById(any());
            verify(transactionMapper).updateEntity(existingTransaction, request);
            verify(transactionRepository).save(existingTransaction);
        }

        @Test
        @DisplayName("Should update relationship when Category changed -> Fetch Proxy")
        void updateTransaction_CategoryChanged_Success() {
            // Given --------------
            UUID transactionId = UUID.randomUUID();
            UUID oldCategoryId = UUID.randomUUID();
            UUID newCategoryId = UUID.randomUUID();

            TransactionRequest request = TransactionRequest.builder()
                .categoryId(newCategoryId) // Request new categoryId
                .amount(DEFAULT_AMOUNT)
                .build();

            Category oldCategory = Category.builder()
                .id(oldCategoryId)
                .build();

            Transaction existingTransaction = Transaction.builder()
                .id(transactionId)
                .category(oldCategory)
                .build();

            Category newCategoryProxy = Category.builder()
                .id(newCategoryId)
                .build();

            // Stubbing
            given(transactionRepository.findById(transactionId))
                .willReturn(Optional.of(existingTransaction));
            doNothing().when(transactionValidator)
                .validateForUpdate(existingTransaction, request);

            // Mock the get proxy behavior
            given(categoryRepository.getReferenceById(newCategoryId)).willReturn(newCategoryProxy);

            given(transactionRepository.save(existingTransaction)).willReturn(existingTransaction);
            given(transactionMapper.toResponse(any())).willReturn(TransactionResponse.builder().build());

            // When ----------------
            transactionService.updateTransaction(transactionId, request);

            // Then ----------------
            // Verify got Proxy and set into transaction
            verify(categoryRepository).getReferenceById(newCategoryId);

            assertThat(existingTransaction.getCategory())
                .isEqualTo(newCategoryProxy);
        }

        @Test
        @DisplayName("Should throw exception when Transaction ID not found")
        void updateTransaction_NotFound_ThrowsException() {
            // Given
            UUID transactionId = UUID.randomUUID();
            TransactionRequest request = TransactionRequest.builder().build();

            given(transactionRepository.findById(transactionId))
                .willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> transactionService.updateTransaction(transactionId, request))
                .isInstanceOf(AppException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.TRANSACTION_NOT_FOUND);

            verify(transactionValidator, never()).validateForUpdate(any(), any());
            verify(transactionRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should propagate exception when Validator fails")
        void updateTransaction_ValidationFails_ThrowsException() {
            // Given --------------
            UUID transactionId = UUID.randomUUID();
            TransactionRequest request = TransactionRequest.builder().build();
            Transaction existingTransaction = new Transaction();

            given(transactionRepository.findById(transactionId))
                .willReturn(Optional.of(existingTransaction));

            // Mock Validator & thrown exception (Category not found)
            doThrow(new AppException(ErrorCode.CATEGORY_NOT_FOUND))
                .when(transactionValidator).validateForUpdate(existingTransaction, request);

            // When & Then -------------
            assertThatThrownBy(() -> transactionService.updateTransaction(transactionId, request))
                .isInstanceOf(AppException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.CATEGORY_NOT_FOUND);

            verify(categoryRepository, never()).getReferenceById(any());
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