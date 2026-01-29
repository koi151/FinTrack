package com.koi151.money.fintrack.common.config;

import com.koi151.money.fintrack.core.auth.converter.KeycloakJwtAuthenticationConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

  @Bean
  public PasswordEncoder passwordEncoder() {
      return new BCryptPasswordEncoder(12); // Strength 12 is industry standard (2024)
  }

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) {
      http
          // CORS configuration, allow frontend to access
          .cors(cors -> cors.configurationSource(corsConfigurationSource()))
          .csrf(AbstractHttpConfigurer::disable)

          // stateless (no HttpSession created), rely on JWT sent in Authorization header
          .sessionManagement(session -> session
            .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
          )

          // URL Authorization Rules
          .authorizeHttpRequests(auth -> auth
            // Public endpoints
            .requestMatchers(
              "/v3/api-docs/**",
              "/swagger-ui/**",
              "/swagger-ui.html",
              "/actuator/health"
            ).permitAll()

          // All other requests require a valid JWT
            .anyRequest().authenticated()
          )

          // OAuth2 Resource Server configuration
          .oauth2ResourceServer(oauth2 -> oauth2
            .jwt(jwtConfigurer -> jwtConfigurer
              // Register the custom converter to map Keycloak roles to Spring authorities
              .jwtAuthenticationConverter(jwtAuthenticationConverter())
            )
          );

      return http.build();
  }

  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
      CorsConfiguration configuration = new CorsConfiguration();

      configuration.setAllowedOrigins(List.of("http://localhost:5173"));

      configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));

      // Allow all headers (Authorization, Content-Type, etc.)
      configuration.setAllowedHeaders(List.of("*"));

      // Allow credentials (cookies, authorization headers)
      configuration.setAllowCredentials(true);

      UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
      source.registerCorsConfiguration("/**", configuration);
      return source;
  }

  /**
   * Defines the custom JWT converter bean
   * This instructs Spring Security to use our specific logic for extracting
   * 'realm_access.roles' from Keycloak tokens instead of the default behavior.
   */
  @Bean
  public Converter<Jwt, AbstractAuthenticationToken> jwtAuthenticationConverter() {
    return new KeycloakJwtAuthenticationConverter();
  }
}