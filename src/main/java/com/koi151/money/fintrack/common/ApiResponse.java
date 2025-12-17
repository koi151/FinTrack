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

    @Builder.Default
    private int code = 1000; // Default success code

    private String message;
    private Instant timestamp;
    private T result;

    // Factory method for Success
    public static <T> ApiResponse<T> success(T result) {
        return ApiResponse.<T>builder()
            .code(1000)
            .message("Success")
            .result(result)
            .timestamp(Instant.now())
            .build();
    }

    // Factory method for Error
    public static <T> ApiResponse<T> error(int code, String message) {
        return ApiResponse.<T>builder()
            .code(code)
            .message(message)
            .timestamp(Instant.now())
            .build();
    }
}