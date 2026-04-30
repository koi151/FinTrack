package com.koi151.money.fintrack.common.constant;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Centralized definition of JWT Claims used across the application.
 * Prevents "Magic Strings" and ensures type safety.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class JwtClaimConstants {

    // Standard OIDC Claims
    public static final String USER_ID = "sub";
    public static final String EMAIL = "email";
    public static final String NAME = "name";
    public static final String PREFERRED_USERNAME = "preferred_username";
    public static final String GIVEN_NAME = "given_name";
    public static final String FAMILY_NAME = "family_name";

    // Keycloak Specific Claims
    public static final String REALM_ACCESS = "realm_access";
    public static final String RESOURCE_ACCESS = "resource_access";
    public static final String ROLES = "roles";
}