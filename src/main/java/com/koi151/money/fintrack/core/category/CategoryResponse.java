package com.koi151.money.fintrack.core.category;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.koi151.money.fintrack.core.transaction.TransactionType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.Instant;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
public record CategoryResponse(

    @Schema(description = "Unique identifier of the category", example = "550e8400-e29b-41d4-a716-446655440000")
    UUID id,

    @Schema(description = "Category name", example = "Groceries")
    String name,

    @Schema(description = "Type of category", example = "EXPENSE")
    TransactionType type,

    @Schema(description = "Assigned icon", example = "shopping-cart")
    String iconCode,

    @Schema(description = "Assigned color", example = "#FF5733")
    String colorCode,

    @Schema(description = "Category description", example = "Monthly grocery shopping")
    String description,

    @Schema(description = "Timestamp when the category was created", example = "2024-01-01T12:00:00Z")
    Instant createdAt
) {}