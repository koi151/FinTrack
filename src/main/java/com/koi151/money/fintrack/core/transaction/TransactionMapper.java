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

    @Autowired // no constructor injection to prevent conflict with mapstruct
    protected CategoryRepository categoryRepository;

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "category", ignore = true) // ignore for @AfterMapping handling it
    @Mapping(target = "transactionDate", expression = "java(request.transactionDate() != null ? request.transactionDate() : java.time.Instant.now())")
    public abstract Transaction toEntity(TransactionRequest request);

    @AfterMapping
    protected void enrichEntity(TransactionRequest request, @MappingTarget Transaction transaction) {
        if (request.categoryId() != null) {
            Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new AppException(
                    ErrorCode.CATEGORY_NOT_FOUND,
                    "Category not found with id: " + request.categoryId()
                ));
            transaction.setCategory(category);
        }
    }

    @Mapping(source = "category.name", target = "categoryName")
    @Mapping(source = "category.id", target = "categoryId")
    public abstract TransactionResponse toResponse(Transaction transaction);
}
