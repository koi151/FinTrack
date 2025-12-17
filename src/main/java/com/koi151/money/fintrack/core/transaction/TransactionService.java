package com.koi151.money.fintrack.core.transaction;

import com.koi151.money.fintrack.common.exception.AppException;
import com.koi151.money.fintrack.common.exception.ErrorCode;
import com.koi151.money.fintrack.core.category.CategoryRepository;
import com.koi151.money.fintrack.core.transaction.payload.TransactionRequest;
import com.koi151.money.fintrack.core.transaction.payload.TransactionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final CategoryRepository categoryRepository;
    private final TransactionMapper transactionMapper;

    public TransactionResponse createTransaction(TransactionRequest request) {
        categoryRepository.findById(request.getCategoryId())
            .orElseThrow(() -> new AppException(
                ErrorCode.CATEGORY_NOT_FOUND, String.format("Category not found with id %s", request.getCategoryId()))
            );

        // Todo: check user

        Transaction transaction = transactionMapper.toEntity(request);
        transactionRepository.save(transaction);
        return transactionMapper.toResponse(transaction);
    }
}