package com.koi151.money.fintrack.core.category;

import com.koi151.money.fintrack.core.transaction.TransactionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;

import java.util.UUID;

@Builder
public record CategoryRequest(

    @NotBlank(message = "Category name is required")
    @Size(max = 100, message = "Name must not exceed 100 characters")
    String name,

    @NotNull(message = "User ID is mandatory")
    UUID userId,

    @NotNull(message = "Transaction type is required")
    TransactionType type,

    @Size(max = 50, message = "Icon code too long")
    String iconCode,

    @Pattern(regexp = "^#([A-Fa-f0-9]{6})$", message = "Color must be a valid HEX code")
    String colorCode
) {}
