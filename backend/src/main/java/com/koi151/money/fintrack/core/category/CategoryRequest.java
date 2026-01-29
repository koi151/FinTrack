package com.koi151.money.fintrack.core.category;

import com.koi151.money.fintrack.core.transaction.TransactionType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;

import java.util.UUID;

@Builder
public record CategoryRequest(

    @Schema(description = "Name of the category", example = "Groceries", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Category name is required")
    @Size(max = 100, message = "Name must not exceed 100 characters")
    String name,

    @Schema(description = "The UUID of owners", example = "550e8400-e29b-41d4-a716-446655440000", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "User ID is mandatory")
    UUID userId,

    @Schema(description = "Transaction type the category belongs to", example = "EXPENSE", allowableValues = {"EXPENSE", "INCOME"})
    @NotNull(message = "Transaction type is required")
    TransactionType type,

    @Schema(description = "Icon identifier (e.g., from FontAwesome or internal set)", example = "shopping-cart")
    @Size(max = 50, message = "Icon code too long")
    String iconCode,

    @Schema(description = "HEX color code for UI display", example = "#FF5733")
    @Pattern(regexp = "^#([A-Fa-f0-9]{6})$", message = "Color must be a valid HEX code")
    String colorCode
) {}
