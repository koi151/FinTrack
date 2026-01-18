package com.koi151.money.fintrack.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {

    /** General Errors -------------------------------------------- */
    RESOURCE_NOT_FOUND(1001, "Resource not found",HttpStatus.NOT_FOUND),
    INVALID_PARAM(1003, "Invalid input parameters", HttpStatus.BAD_REQUEST),
    SYSTEM_ERROR(1005, "Uncategorized system error", HttpStatus.INTERNAL_SERVER_ERROR),

    /** 2xxx: Category Module -------------------------------------- */
    CATEGORY_NOT_FOUND(2001, "Category not found", HttpStatus.NOT_FOUND),
    CATEGORY_EXISTED(2002, "Category already exists", HttpStatus.CONFLICT),

    /** 3xxx: Transaction Module -------------------------------------- */
    TRANSACTION_NOT_FOUND(3001, "Transaction not found", HttpStatus.NOT_FOUND),
    TRANSACTION_EXISTED(3002, "Transaction already exists", HttpStatus.CONFLICT),

    /** 4xxx: User Module -------------------------------------- */
    USER_NOT_FOUND(4001, "User not found", HttpStatus.NOT_FOUND),

    // conflict
    USERNAME_EXISTED(4002, "User name already exists", HttpStatus.CONFLICT),
    USER_EMAIL_EXISTED(4002, "Email already exists", HttpStatus.CONFLICT),
    OAUTH_ACCOUNT_ALREADY_LINKED(4004, "This social account is already linked to another user", HttpStatus.CONFLICT);

    private final int code;
    private final String message;
    private final HttpStatus httpStatus;
}
