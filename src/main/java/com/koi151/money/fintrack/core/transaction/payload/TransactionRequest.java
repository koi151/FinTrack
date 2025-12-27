package com.koi151.money.fintrack.core.transaction.payload;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Builder
public record TransactionRequest (

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be greater than zero")
    BigDecimal amount,

    @NotNull(message = "Category ID is required")
    UUID categoryId,

    @NotNull(message = "User ID is required")
    UUID userId,

    String note,

    Instant dateTime
) {}