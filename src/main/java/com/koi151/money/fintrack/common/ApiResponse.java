package com.koi151.money.fintrack.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.time.Instant;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    public static final int SUCCESS_CODE = 1000;

    @Builder.Default
    private int code = SUCCESS_CODE;

    @Builder.Default
    private Instant timestamp = Instant.now();

    private String message;
    private T result;

    // Factory method for Success
    public static <T> ApiResponse<T> success(T result) {
        return ApiResponse.<T>builder()
            .message("Success")
            .result(result)
            .build();
    }

    public static <T> ApiResponse<T> success(T result, String message) {
        return ApiResponse.<T>builder()
            .message(message)
            .result(result)
            .build();
    }

    public static <T> ApiResponse<T> success(int code, String message) {
        return ApiResponse.<T>builder()
            .code(code)
            .message(message)
            .build();
    }

    // Factory method for Error
    public static <T> ApiResponse<T> error(int code, String message) {
        return ApiResponse.<T>builder()
            .code(code)
            .message(message)
            .build();
    }
}