package com.koi151.money.fintrack.core.transaction.payload;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class TransactionResponse {

    private UUID id;

    private BigDecimal amount;

    private String note;

    private Instant transactionDate;

    private String categoryName;

    private UUID categoryId;
}