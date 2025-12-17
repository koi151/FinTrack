package com.koi151.money.fintrack.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {

    /** General Errors -------------------------------------------- */
    UNCATEGORIZED_EXCEPTION(1001, "Uncategorized error", HttpStatus.INTERNAL_SERVER_ERROR),

    // Specifically for Form Validation (@NotNull, @Size, @Email)
    INVALID_PARAMETERS(1003, "Invalid input parameters", HttpStatus.BAD_REQUEST),

    RESOURCE_NOT_FOUND(1004, "Resource not found",HttpStatus.NOT_FOUND),

    /** 2xxx: Category Module -------------------------------------- */
    CATEGORY_EXISTED(2001, "Category already exists", HttpStatus.CONFLICT);

    private final int code;
    private final String message;
    private final HttpStatus httpStatus;
}
