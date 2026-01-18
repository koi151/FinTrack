package com.koi151.money.fintrack.common.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Map;

/**
 * Helper class to translate DB constraints into AppExceptions.
 */
@Slf4j
public final class DbExceptionHelper {

    private DbExceptionHelper() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    public static RuntimeException handleDataIntegrityViolation(
        DataIntegrityViolationException e,
        Map<String, ErrorCode> constraintMap
    ) {
        String rootMsg = e.getMostSpecificCause().getMessage();

        if (rootMsg != null) {
            for (Map.Entry<String, ErrorCode> entry : constraintMap.entrySet()) {
                if (rootMsg.contains(entry.getKey())) {
                    log.warn("Database constraint violated: {}", entry.getKey());
                    return new AppException(entry.getValue());
                }
            }
        }

        log.error("Unexpected database integrity violation", e);
        return e;
    }
}
