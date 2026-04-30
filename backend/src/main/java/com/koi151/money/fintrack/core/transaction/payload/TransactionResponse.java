package com.koi151.money.fintrack.core.transaction.payload;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class TransactionResponse {

    @Schema(description = "Unique identifier of the transaction", example = "a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11")
    private UUID id;

    @Schema(description = "Transaction amount", example = "150.75")
    private BigDecimal amount;

    @Schema(description = "Transaction note", example = "Grocery shopping at VinMart")
    private String note;

    @Schema(description = "Timestamp of the transaction", example = "2025-05-20T14:30:00Z")
    private Instant transactionDate;

    @Schema(description = "Name of the associated category", example = "Food & Beverages")
    private String categoryName;

    @Schema(description = "UUID of the associated category", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID categoryId;

    @Schema(description = "UUID of the user", example = "7b23315a-5927-466d-8e68-3605658e23f0")
    private UUID userId;

    @Schema(description = "Category Type", example = "EXPENSE")
    private String categoryType;
}