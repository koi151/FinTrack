package com.koi151.money.fintrack.core.auth.converter;

import jakarta.validation.constraints.NotNull;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimNames;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Converts a Keycloak issued JWT into a Spring Security authentication token
 * Extracts realm roles from the JWT and maps them to Spring authorities with the "ROLE_" prefix.
 */
public class KeycloakJwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

  private static final String REALM_ACCESS = "realm_access";
  private static final String ROLES = "roles";
  private static final String ROLE_PREFIX = "ROLE_";
  private static final String USERNAME_CLAIM = "preferred_username";

  /**
   * Convert JSON (JWT) to Java object
   *
   * @param jwt The source JSON Web Token.
   * @return An authenticated token containing the user's authorities
   */
  @Override
  public AbstractAuthenticationToken convert(@NotNull Jwt jwt) {
    Collection<GrantedAuthority> authorities = extractRealmRoles(jwt);
    return new JwtAuthenticationToken(jwt, authorities, getPrincipalClaimName(jwt));
  }

  /**
   * Extracts roles from 'realm_access.roles' and formats them with prefix "ROLE_" (ROLE_ADMIN, etc.)
   *
   * @param jwt The JWT token containing user claims
   */
  @SuppressWarnings("unchecked")
  private Collection<GrantedAuthority> extractRealmRoles(Jwt jwt) {
    List<?> rawRoles = getRawRolesFromJwt(jwt);

    return rawRoles.stream()
      .map(Object::toString)
      .filter(role -> !role.isBlank()) // Filter out empty strings
      .map(role -> new SimpleGrantedAuthority(ROLE_PREFIX + role.toUpperCase())) // Normalize to uppercase
      .collect(Collectors.toSet());
  }

  /**
   * Safely retrieves the raw list of roles from the JWT claims.
   * Handles null checks and type safety
   *
   * @param jwt The source JSON Web Token.
   * @return A list of raw role objects, or an empty list if not found.
   */
  private List<?> getRawRolesFromJwt(Jwt jwt) {
    Map<String, Object> realmAccess = jwt.getClaim(REALM_ACCESS);

    // Check if the 'realm_access' claim exists and is not empty
    if (realmAccess == null || realmAccess.isEmpty()) {
      return Collections.emptyList();
    }

    Object rolesObj = realmAccess.get(ROLES);

    // Check if the 'roles' field exists and is indeed a List
    if (rolesObj instanceof List<?> roleList) {
      return roleList;
    }

    // Return empty list if data is missing or malformed
    return Collections.emptyList();
  }

  /**
   * Prioritizes preferred_username over sub (UUID) for better readability in logs/context
   */
  private String getPrincipalClaimName(Jwt jwt) {
    if (jwt.hasClaim(USERNAME_CLAIM)) {
      return jwt.getClaimAsString(USERNAME_CLAIM);
    }
    return jwt.getClaimAsString(JwtClaimNames.SUB);
  }
}