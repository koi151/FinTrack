package com.koi151.money.fintrack.core.transaction;

import com.koi151.money.fintrack.common.AppResponse;
import com.koi151.money.fintrack.core.transaction.payload.TransactionRequest;
import com.koi151.money.fintrack.core.transaction.payload.TransactionResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
@Tag(name = "Transaction Management", description = "Endpoints for managing financial transactions (Income/Expense)")
public class TransactionController {

    private final TransactionService transactionService;

    @Operation(summary = "Create a new transaction", description = "Record a new expense or income. Validates category and user existence.")
    @ApiResponse(responseCode = "200", description = "Transaction created successfully")
    @ApiResponse(responseCode = "400", description = "Invalid input or business logic violation", content = @Content)
    @ApiResponse(responseCode = "404", description = "Category or User not found", content = @Content)
    @PostMapping
    public AppResponse<TransactionResponse> createTransaction(@RequestBody @Valid TransactionRequest request) {
        return AppResponse.success(transactionService.createTransaction(request), "Successfully created transaction");
    }

    @Operation(summary = "Update an existing transaction", description = "Modify details of a specific transaction by its UUID.")
    @ApiResponse(responseCode = "200", description = "Transaction updated successfully")
    @ApiResponse(responseCode = "400", description = "Invalid update data", content = @Content)
    @ApiResponse(responseCode = "404", description = "Transaction, Category, or User not found", content = @Content)
    @PutMapping("/{id}")
    public AppResponse<TransactionResponse> updateTransaction(@PathVariable UUID id, @RequestBody @Valid TransactionRequest request) {
        return AppResponse.success(transactionService.updateTransaction(id, request), "Successfully updated transaction");
    }

    @Operation(summary = "Delete a transaction", description = "Permanently remove a transaction from the records.")
    @ApiResponse(responseCode = "200", description = "Transaction deleted successfully")
    @ApiResponse(responseCode = "404", description = "Transaction not found", content = @Content)
    @DeleteMapping("/{id}")
    public AppResponse<TransactionResponse> deleteTransaction(@PathVariable UUID id) {
        transactionService.deleteTransaction(id);
        return AppResponse.success(204, "Successfully deleted transaction");
    }
}