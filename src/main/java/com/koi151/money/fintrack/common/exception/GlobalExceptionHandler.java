package com.koi151.money.fintrack.common.exception;

import com.koi151.money.fintrack.common.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // Handle App Specific Exceptions
    @ExceptionHandler(AppException.class)
    public ResponseEntity<ApiResponse<Object>> handleAppException(AppException ex) {
        ErrorCode errorCode = ex.getErrorCode();
        log.warn("Business Error: {}", errorCode.getMessage());
        return ResponseEntity
            .status(errorCode.getHttpStatus())
            .body(ApiResponse.error(
                errorCode.getCode(),
                errorCode.getMessage()
            ));
    }

    // Handle Validation Errors (@Valid failure) -> Return Map for FE
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationException(MethodArgumentNotValidException ex) {
        log.warn("Validation Error: {}", ex.getMessage());

        // Collect errors into a Map (Field -> Message)
        Map<String, String> errors = new HashMap<>();

        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        ErrorCode errorCode = ErrorCode.INVALID_PARAM; // 1003

        // Return ApiResponse with the Map in 'result' field
        return ResponseEntity
            .status(errorCode.getHttpStatus())
            .body(ApiResponse.<Map<String, String>>builder()
                .code(errorCode.getCode())
                .message(errorCode.getMessage())
                .result(errors)
                .build());
    }


    // Catch all & log stack trace
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<?>> handleUnexpectedException(Exception ex, HttpServletRequest request) {
        log.error("Unexpected Error at {}: ", request.getRequestURI(), ex);

        return ResponseEntity
            .status(ErrorCode.SYSTEM_ERROR.getHttpStatus())
            .body(ApiResponse.error(
                ErrorCode.SYSTEM_ERROR.getCode(),
                "Internal server error. Please contact admin."
            ));
    }
}