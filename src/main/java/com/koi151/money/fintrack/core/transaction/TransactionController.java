package com.koi151.money.fintrack.core.transaction;

import com.koi151.money.fintrack.common.ApiResponse;
import com.koi151.money.fintrack.core.transaction.payload.TransactionRequest;
import com.koi151.money.fintrack.core.transaction.payload.TransactionResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping
    public ApiResponse<TransactionResponse> createTransaction(@RequestBody @Valid TransactionRequest request) {
        return ApiResponse.success(transactionService.createTransaction(request), "Successfully created transaction");
    }

    @PutMapping("/{id}")
    public ApiResponse<TransactionResponse> updateTransaction(@PathVariable UUID id, @RequestBody @Valid TransactionRequest request) {
        return ApiResponse.success(transactionService.updateTransaction(id, request), "Successfully updated transaction");
    }

    @DeleteMapping("/{id}")
    public ApiResponse<TransactionResponse> deleteTransaction(@PathVariable UUID id) {
        transactionService.deleteTransaction(id);
        return ApiResponse.success(204, "Successfully deleted transaction");
    }
}