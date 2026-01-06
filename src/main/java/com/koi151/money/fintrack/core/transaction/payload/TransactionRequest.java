package com.koi151.money.fintrack.core.transaction.payload;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Builder
public record TransactionRequest (

    @Schema(description = "Amount of the transaction", example = "150.75", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be greater than zero")
    BigDecimal amount,

    @Schema(description = "ID of the category this transaction belongs to", example = "550e8400-e29b-41d4-a716-446655440000", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Category ID is required")
    UUID categoryId,

    @Schema(description = "ID of the user who owns the transaction", example = "7b23315a-5927-466d-8e68-3605658e23f0", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "User ID is required")
    UUID userId,

    @Schema(description = "Optional note or description", example = "Grocery shopping at VinMart")
    String note,

    @Schema(description = "The date and time the transaction occurred", example = "2025-05-20T14:30:00Z", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Date is required")
    @PastOrPresent(message = "Transaction date cannot be in the future")
    Instant transactionDate
) {}