package com.koi151.money.fintrack.common.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

    private final Instant timestamp;
    private final int status;
    private final String error;
    private final int code;
    private final String message;
    private final String path;

    public static ErrorResponse of(
        ErrorCode errorCode,
        String path
    ) {
        return ErrorResponse.builder()
            .timestamp(Instant.now())
            .status(errorCode.getHttpStatus().value())
            .error(errorCode.getHttpStatus().getReasonPhrase())
            .code(errorCode.getCode())
            .message(errorCode.getMessage())
            .path(path)
            .build();
    }
}
