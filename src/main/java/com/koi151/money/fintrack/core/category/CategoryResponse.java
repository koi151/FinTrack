package com.koi151.money.fintrack.core.category;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.koi151.money.fintrack.core.transaction.TransactionType;

import java.time.Instant;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record CategoryResponse(

    UUID id,

    String name,

    TransactionType type,

    String iconCode,

    String colorCode,

    String description,

    Instant createdAt
) {}