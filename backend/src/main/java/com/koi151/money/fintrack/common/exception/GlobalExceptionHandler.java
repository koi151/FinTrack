package com.koi151.money.fintrack.common.exception;

import com.koi151.money.fintrack.common.AppResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * Global exception handler to standardize API error responses across the application.
 * Captures exceptions thrown by Controllers/Services and converts them into {@link AppResponse}
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles business logic exceptions thrown intentionally by the application.
     * Example: "User not found", "Email already exists"
     * @param ex The custom business exception
     * @return 4xx/5xx response with a specific error code
     */
    @ExceptionHandler(AppException.class)
    public ResponseEntity<AppResponse<Object>> handleAppException(AppException ex) {
        ErrorCode errorCode = ex.getErrorCode();
        log.warn("Business Error: [Code: {}] {}", errorCode.getCode(), errorCode.getMessage());

        return ResponseEntity
            .status(errorCode.getHttpStatus())
            .body(AppResponse.error(
                errorCode.getCode(),
                errorCode.getMessage()
            ));
    }

    /**
     * Handles validation errors when @Valid fails on a DTO.
     * Captures field-specific errors (e.g., "email: Invalid format") and returns them in a Map
     * @param ex The validation exception
     * @return 400 Bad Request with a detailed map of field errors
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<AppResponse<Map<String, String>>> handleValidationException(MethodArgumentNotValidException ex) {
        log.warn("Validation Error: Request validation failed");

        // collects FieldErrors into a Map<Field, Message>
        Map<String, String> errors = ex.getBindingResult().getFieldErrors().stream()
            .collect(Collectors.toMap(
                FieldError::getField,
                error -> error.getDefaultMessage() != null ? error.getDefaultMessage() : "Invalid value",
                (existing, replacement) -> existing // Conflict resolution: keep the first error message if multiple exist
            ));

        ErrorCode errorCode = ErrorCode.INVALID_PARAM;

        return ResponseEntity
            .status(errorCode.getHttpStatus())
            .body(AppResponse.<Map<String, String>>builder()
                .code(errorCode.getCode())
                .message(errorCode.getMessage())
                .result(errors) // returns the map of errors to the client
                .build());
    }

    /**
     * Handles malformed JSON requests or missing request bodies.
     * Example: Sending invalid JSON syntax or null body to a POST endpoint.
     * @param ex The parsing exception
     * @return 400 Bad Request
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<AppResponse<Object>> handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
        log.warn("Malformed Request: {}", ex.getMessage());

        ErrorCode errorCode = ErrorCode.INVALID_PARAM;

        return ResponseEntity
            .status(errorCode.getHttpStatus())
            .body(AppResponse.error(
                errorCode.getCode(),
                "Request body is missing or malformed"
            ));
    }

    /**
     * Catch-all handler for unexpected system exceptions.
     * Prevents stack traces from leaking to the client.
     * @param ex The unexpected exception
     * @param request The HTTP request context
     * @return 500 Internal Server Error
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<AppResponse<?>> handleUnexpectedException(Exception ex, HttpServletRequest request) {
        log.error("Unexpected Error at {}: ", request.getRequestURI(), ex);

        return ResponseEntity
            .status(ErrorCode.SYSTEM_ERROR.getHttpStatus())
            .body(AppResponse.error(
                ErrorCode.SYSTEM_ERROR.getCode(),
                "Internal server error. Please contact admin."
            ));
    }
}