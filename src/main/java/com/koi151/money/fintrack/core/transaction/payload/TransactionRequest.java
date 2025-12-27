package com.koi151.money.fintrack.core.transaction.payload;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class TransactionRequest {

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be greater than zero")
    private BigDecimal amount;

    @NotNull(message = "Category ID is required")
    private UUID categoryId;

    @NotNull(message = "User ID is required")
    private UUID userId;

    private String note;

    private Instant dateTime;
}