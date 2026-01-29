package com.koi151.money.fintrack.core.user.payload;

import lombok.Builder;

import java.util.UUID;

@Builder
public record UserResponse(
    UUID id,
    String username,
    String email
) {}
