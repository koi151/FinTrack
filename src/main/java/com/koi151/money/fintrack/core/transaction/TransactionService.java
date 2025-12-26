package com.koi151.money.fintrack.core.transaction;

import com.koi151.money.fintrack.common.exception.AppException;
import com.koi151.money.fintrack.common.exception.ErrorCode;
import com.koi151.money.fintrack.core.category.CategoryRepository;
import com.koi151.money.fintrack.core.transaction.payload.TransactionRequest;
import com.koi151.money.fintrack.core.transaction.payload.TransactionResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final CategoryRepository categoryRepository;
    private final TransactionMapper transactionMapper;

    @Transactional
    public TransactionResponse createTransaction(TransactionRequest request) {
        log.info("[CREATE_TRANSACTION] Start - categoryId: {}, userId: {}, amount: {}",
                request.getCategoryId(), request.getUserId(), request.getAmount());

        Transaction savedTransaction = transactionRepository.save(
            transactionMapper.toEntity(request)
        );

        log.info("[CREATE_TRANSACTION] Successfully saved transaction with ID: {}", savedTransaction.getId());
        return transactionMapper.toResponse(savedTransaction);
    }

    @Transactional
    public void deleteTransaction(UUID id) {
        Transaction transaction = transactionRepository.findById(id)
            .orElseThrow(() -> new AppException(
                ErrorCode.TRANSACTION_NOT_FOUND,
                String.format("Transaction not found with id: %s", id))
            );
        transactionRepository.delete(transaction);
    }
}