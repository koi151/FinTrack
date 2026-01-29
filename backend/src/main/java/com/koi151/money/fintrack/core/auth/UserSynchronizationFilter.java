package com.koi151.money.fintrack.core.auth;

import com.koi151.money.fintrack.common.constant.CacheRegion;
import com.koi151.money.fintrack.core.auth.event.UserAuthenticationEvent;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * Filter to ensure users authenticated via Keycloak exist in the local database.
 * Uses Redis caching to minimize database hits on every request
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class UserSynchronizationFilter extends OncePerRequestFilter {

  private final ApplicationEventPublisher eventPublisher;
  private final CacheManager cacheManager;

  @Override
  protected void doFilterInternal(@NonNull HttpServletRequest request,
                                  @NonNull HttpServletResponse response,
                                  @NonNull FilterChain filterChain) throws ServletException, IOException {

    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    // Process only if the user is authenticated via JWT
    if (authentication instanceof JwtAuthenticationToken jwtAuth) {
      Jwt jwt = jwtAuth.getToken();

      UUID userId = UUID.fromString(jwt.getSubject());
      Cache userSyncCache = cacheManager.getCache(CacheRegion.USER_SYNC.getCacheName());

      if (userSyncCache != null && !isUserSynchronized(userSyncCache, userId)) {
        log.debug("User {} not found in sync cache. Triggering synchronization.", userId);

        // Publish event to trigger DB sync (handled by UserSyncEventListener)
        eventPublisher.publishEvent(new UserAuthenticationEvent(this, jwt, userId));

        // Update cache
        markUserAsSynchronized(userSyncCache, userId);
      }
    }

    // Continue the filter chain
    filterChain.doFilter(request, response);
  }

  /**
   * Checks if the user has already been synchronized in the current session/TTL.
   *
   * @return true If user is synchronized, false if cache is missing or user not found
   */
  private boolean isUserSynchronized(Cache cache, UUID userId) {
    return cache.get(userId) != null;
  }

  /**
   * Marks the user as synchronized in the cache to prevent redundant DB checks.
   */
  private void markUserAsSynchronized(Cache cache, UUID userId) {
      cache.put(userId, true);
  }
}