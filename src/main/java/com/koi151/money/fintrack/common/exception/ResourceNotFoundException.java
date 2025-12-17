package com.koi151.money.fintrack.common.exception;

import java.util.UUID;

public class ResourceNotFoundException extends AppException {

    public ResourceNotFoundException() {
        super(ErrorCode.RESOURCE_NOT_FOUND);
    }

    public ResourceNotFoundException(String resourceName, UUID id) {
        super(ErrorCode.RESOURCE_NOT_FOUND, resourceName + " not found with id: " + id);
    }
}