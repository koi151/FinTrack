package com.koi151.money.fintrack.core.user;

import com.koi151.money.fintrack.core.user.domain.User;
import com.koi151.money.fintrack.core.user.payload.UserSyncRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    /**
     * Synchronizes the user identity from the Identity Provider (Keycloak) to the local database.
     * This method implements an "Upsert" strategy: Update if exists, Create if new.
     *
     * @param request The DTO containing identity claims extracted from the JWT.
     */
    @Transactional
    public void syncUser(UserSyncRequest request) {
        Optional<User> existingUserOpt = userRepository.findById(request.id());

        existingUserOpt.ifPresentOrElse(
            existingUser -> updateExistingUser(existingUser, request),
            () -> createNewUser(request)
        );
    }

    /**
     * Persists a new user entity derived from the sync request.
     */
    private void createNewUser(UserSyncRequest request) {
        // OOP: The mapper handles the object transformation, keeping this service method clean.
        User newUser = userMapper.toEntity(request);

        userRepository.save(newUser);
        log.info("New user provisioned successfully. ID: {}, Username: {}", request.id(), request.username());
    }

    /**
     * Updates mutable fields of an existing user if changes are detected.
     * This ensures the local profile stays consistent with Keycloak.
     */
    private void updateExistingUser(User existingUser, UserSyncRequest request) {
        boolean isUpdated = false;

        // Domain Logic: Check each field for changes to avoid unnecessary DB writes
        if (!existingUser.getEmail().equals(request.email())) {
            existingUser.setEmail(request.email());
            isUpdated = true;
        }

        if (!existingUser.getUsername().equals(request.username())) {
            existingUser.setUsername(request.username());
            isUpdated = true;
        }

        // Handle nullable fields carefully
        if (request.fullName() != null && !request.fullName().equals(existingUser.getFullName())) {
            existingUser.setFullName(request.fullName());
            isUpdated = true;
        }

        if (isUpdated) {
            userRepository.save(existingUser);
            log.debug("User profile synced and updated. ID: {}", request.id());
        }
    }

}