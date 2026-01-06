package com.koi151.money.fintrack.core.transaction;

import com.koi151.money.fintrack.common.exception.AppException;
import com.koi151.money.fintrack.common.exception.ErrorCode;
import com.koi151.money.fintrack.core.category.Category;
import com.koi151.money.fintrack.core.category.CategoryRepository;
import com.koi151.money.fintrack.core.transaction.payload.TransactionRequest;
import com.koi151.money.fintrack.core.transaction.payload.TransactionResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final CategoryRepository categoryRepository;
    private final TransactionMapper transactionMapper;
    private final TransactionValidator transactionValidator;

    @Transactional(readOnly = true)
    public List<TransactionResponse> getTransactions() {
        // @Todo pagination, filters, find by user id

        return transactionRepository.findAll().stream()
            .map(transactionMapper::toResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public TransactionResponse getTransaction(UUID id) {
        return transactionMapper.toResponse(
            findTransactionOrThrow(id)
        );
    }

    @Transactional
    public TransactionResponse createTransaction(TransactionRequest request) {
        log.info("[CREATE_TRANSACTION] Start - categoryId: {}, userId: {}, amount: {}",
                request.categoryId(), request.userId(), request.amount());

        transactionValidator.validateForCreate(request);

        Transaction savedTransaction = transactionRepository.save(
            transactionMapper.toEntity(request)
        );

        log.info("[CREATE_TRANSACTION] Successfully saved transaction with ID: {}", savedTransaction.getId());
        return transactionMapper.toResponse(savedTransaction);
    }

    @Transactional
    public TransactionResponse updateTransaction(UUID transactionId, TransactionRequest request) {
        log.info("[UPDATE] Start id: {}", transactionId);

        Transaction existingTransaction = findTransactionOrThrow(transactionId);

        transactionValidator.validateForUpdate(existingTransaction, request);

        updateCategoryReference(existingTransaction, request.categoryId());

        transactionMapper.updateEntity(existingTransaction, request);

        return transactionMapper.toResponse(
            transactionRepository.save(existingTransaction)
        );
    }

    @Transactional
    public void deleteTransaction(UUID transactionId) {
        log.info("[DELETE_TRANSACTION] Start - id: {}", transactionId);
        Transaction transaction = findTransactionOrThrow(transactionId);

        transactionRepository.delete(transaction);
        log.info("[DELETE_TRANSACTION] Deleted - id: {}", transactionId);
    }

    // Helper methods ---------------------
    private Transaction findTransactionOrThrow(UUID transactionId) {
        return transactionRepository.findById(transactionId)
            .orElseThrow(() -> new AppException(
                ErrorCode.TRANSACTION_NOT_FOUND,
                String.format("Transaction not found with id: %s", transactionId)
            ));
    }

    private void updateCategoryReference(Transaction transaction, UUID newCategoryId) {
        if (newCategoryId == null) { // no category update request -> skip
            return;
        }

        UUID currentCategoryId = transaction.getCategory().getId();
        if (currentCategoryId.equals(newCategoryId)) { // no change in categoryId -> skip
            return;
        }

        // Use reference (proxy) to prevent redundant 1 SELECT query
        Category categoryProxy = categoryRepository.getReferenceById(newCategoryId);
        transaction.changeCategory(categoryProxy);
    }
}