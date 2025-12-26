package com.koi151.money.fintrack.core.transaction;

import com.koi151.money.fintrack.common.exception.AppException;
import com.koi151.money.fintrack.common.exception.ErrorCode;
import com.koi151.money.fintrack.core.category.Category;
import com.koi151.money.fintrack.core.category.CategoryRepository;
import com.koi151.money.fintrack.core.transaction.payload.TransactionRequest;
import com.koi151.money.fintrack.core.transaction.payload.TransactionResponse;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class TransactionMapper {

    @Autowired
    protected CategoryRepository categoryRepository;

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "category", ignore = true) // ignore for @AfterMapping handling it
    @Mapping(target = "transactionDate", expression = "java(request.getDateTime() != null ? request.getDateTime() : java.time.Instant.now())")
    public abstract Transaction toEntity(TransactionRequest request);

    @AfterMapping
    protected void enrichEntity(TransactionRequest request, @MappingTarget Transaction transaction) {
        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new AppException(
                    ErrorCode.CATEGORY_NOT_FOUND,
                    "Category not found with id: " + request.getCategoryId()
                ));
            transaction.setCategory(category);
        }
    }

    @Mapping(source = "category.name", target = "categoryName")
    @Mapping(source = "category.id", target = "categoryId")
    public abstract TransactionResponse toResponse(Transaction transaction);
}
