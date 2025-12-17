package com.koi151.money.fintrack.core.transaction;

import com.koi151.money.fintrack.core.transaction.payload.TransactionRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TransactionMapper {

    @Mapping(target = "dateTime", expression = "java(request.getDateTime() != null ? request.getDateTime() : java.time.Instant.now())")
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "id", ignore = true)
    Transaction toEntity(TransactionRequest request);
}
