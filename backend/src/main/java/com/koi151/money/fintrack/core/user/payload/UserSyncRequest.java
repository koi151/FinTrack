package com.koi151.money.fintrack.core.user.payload;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

import java.util.UUID;

/**
 * DTO carrying user identity information extracted from the OAuth2/OIDC Token.
 */
@Builder
public record UserSyncRequest(

    @NotBlank(message = "User ID (Subject) is required")
    UUID id, // Maps to Keycloak 'sub'

    @NotBlank(message = "Username is required")
    String username, // Maps to 'preferred_username'

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    String email,

    String fullName, // Maps to 'name'

    String avatarUrl
) {}