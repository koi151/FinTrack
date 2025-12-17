package com.koi151.money.fintrack.core.transaction;

import com.koi151.money.fintrack.core.transaction.payload.TransactionRequest;
import com.koi151.money.fintrack.core.transaction.payload.TransactionResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TransactionMapper {

    // Todo: replace with auto date assign in common entity
    @Mapping(target = "transactionDate", expression = "java(request.getDateTime() != null ? request.getDateTime() : java.time.Instant.now())")
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "category.id", source = "categoryId")
    Transaction toEntity(TransactionRequest request);

    @Mapping(source = "category.name", target = "categoryName")
    @Mapping(source = "category.id", target = "categoryId")
    TransactionResponse toResponse(Transaction transaction);
}
