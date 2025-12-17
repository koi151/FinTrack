package com.koi151.money.fintrack.common.exception;

import com.koi151.money.fintrack.common.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
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

    // Handle Validation Errors -> return Map to FE to easily create UI
    // Todo: Simplify it
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Object>> handleValidationException(MethodArgumentNotValidException ex) {
        // Create the detailed map of errors
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage())
        );

        // Use the specific ErrorCode for validation
        ErrorCode errorCode = ErrorCode.INVALID_PARAMETERS;
        ApiResponse<Object> response = ApiResponse.error(
            errorCode.getCode(),
            errorCode.getMessage()
        );
        response.setResult(errors);

        return ResponseEntity
            .status(errorCode.getHttpStatus())
            .body(response);
    }

    // Catch all & log stack trace
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<?>> handleUnexpectedException(Exception ex, HttpServletRequest request) {
        log.error("Unexpected Error at {}: ", request.getRequestURI(), ex);

        return ResponseEntity
            .status(ErrorCode.UNCATEGORIZED_EXCEPTION.getHttpStatus())
            .body(ApiResponse.error(
                ErrorCode.UNCATEGORIZED_EXCEPTION.getCode(),
                "Internal server error. Please contact admin."
            ));
    }
}