package com.koi151.money.fintrack.core.transaction.payload;

import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
public class TransactionRequest {

    private BigDecimal amount;
    private String note;
    private Instant dateTime;
    private UUID categoryId;
    private UUID userId;
}