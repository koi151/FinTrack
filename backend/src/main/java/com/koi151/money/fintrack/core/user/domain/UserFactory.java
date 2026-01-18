package com.koi151.money.fintrack.core.user.domain;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class UserFactory {

    /**
     * Creates a new User entity for local registration.
     */
    public User createLocalUser(String username, String email, String encodedPassword) {
        return User.builder()
            .username(normalize(username))
            .email(normalize(email))
            .password(encodedPassword)
            .provider(AuthProvider.LOCAL)
            .role(Role.USER)       // Default Role
            .enabled(true)         // Default Status
            .accountNonLocked(true)
            .build();
    }

    /**
     * Helper to normalize strings
     */
    private String normalize(String input) {
        return StringUtils.hasText(input) ? input.trim().toLowerCase() : null;
    }
}
