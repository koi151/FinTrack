package com.koi151.money.fintrack.core.auth.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.UUID;

/**
 * Custom event published when a user is authenticated via JWT
 * This event carries the JWT token and User ID to allow the EventListener
 * to synchronize user data from Keycloak to the local database
 */
@Getter
public class UserAuthenticationEvent extends ApplicationEvent {

  private final Jwt jwt;
  private final UUID userId;

  /**
   * @param source The object on which the event initially occurred
   * @param jwt    The decoded JWT token containing user claims (email, name, etc.)
   * @param userId The UUID extracted from the JWT 'sub' claim.
   */
  public UserAuthenticationEvent(Object source, Jwt jwt, UUID userId) {
    super(source);
    this.jwt = jwt;
    this.userId = userId;
  }
}
