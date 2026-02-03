package com.koi151.money.fintrack.core.auth.event;

import com.koi151.money.fintrack.common.constant.JwtClaimConstants;
import com.koi151.money.fintrack.core.user.UserService;
import com.koi151.money.fintrack.core.user.payload.UserSyncRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Listener responsible for handling user synchronization events.
 * Decouples the HTTP Filter from the Domain Service logic.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UserSyncEventListener {

    private final UserService userService;

    @EventListener
    @Async
    public void handleUserSync(UserAuthenticationEvent event) {
        Jwt jwt = event.getJwt();

        try {
            UserSyncRequest userSyncRequest = UserSyncRequest.builder()
                .id(UUID.fromString(jwt.getSubject()))
                .username(jwt.getClaimAsString(JwtClaimConstants.PREFERRED_USERNAME))
                .email(jwt.getClaimAsString(JwtClaimConstants.EMAIL))
                .fullName(jwt.getClaimAsString(JwtClaimConstants.NAME))
                .build();

            userService.syncUser(userSyncRequest);

        } catch (Exception e) {
            log.error("Failed to sync user from token: {}", jwt.getSubject(), e);
        }
    }
}