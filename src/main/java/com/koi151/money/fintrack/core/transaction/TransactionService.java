package com.koi151.money.fintrack.core.transaction;

import com.koi151.money.fintrack.common.exception.ResourceNotFoundException;
import com.koi151.money.fintrack.core.category.Category;
import com.koi151.money.fintrack.core.category.CategoryRepository;
import com.koi151.money.fintrack.core.transaction.payload.TransactionRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final CategoryRepository categoryRepository;
    private final TransactionMapper transactionMapper;

    public Transaction createTransaction(TransactionRequest request) {
        Category category = categoryRepository.findById(request.getCategoryId())
            .orElseThrow(ResourceNotFoundException::new);
//            .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + request.getCategoryId()));

        Transaction transaction = transactionMapper.toEntity(request);
        transaction.setCategory(category);

        return transactionRepository.save(transaction);
    }
}