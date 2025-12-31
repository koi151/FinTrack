package com.koi151.money.fintrack.core.transaction;

import com.koi151.money.fintrack.common.exception.AppException;
import com.koi151.money.fintrack.common.exception.ErrorCode;
import com.koi151.money.fintrack.core.category.CategoryRepository;
import com.koi151.money.fintrack.core.transaction.payload.TransactionRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class TransactionValidator {

    private final CategoryRepository categoryRepository;

    public void validateForCreate(TransactionRequest request) {
        if (!categoryRepository.existsById(request.categoryId())) {
            throw new AppException(ErrorCode.CATEGORY_NOT_FOUND);
        }
    }

    public void validateForUpdate(Transaction existingTransaction, TransactionRequest request) {
        UUID currentCategoryId = existingTransaction.getCategory().getId();
        UUID newCategoryId = request.categoryId();

        // only check DB if category ID changed
        if (!currentCategoryId.equals(newCategoryId)) {
            boolean categoryExists = categoryRepository.existsById(request.categoryId());
            if (!categoryExists) {
                throw new AppException(
                    ErrorCode.CATEGORY_NOT_FOUND,
                    String.format("Category not found with id: %s", request.categoryId())
                );
            }
        }

        // Todo: Check if user owns the transaction/category)
    }
}
