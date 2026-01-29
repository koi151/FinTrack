package com.koi151.money.fintrack.core.user.domain;

import com.koi151.money.fintrack.common.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@Entity
@Table(name = "app_users", uniqueConstraints = { // "user" is a reserved keyword in Postgres/SQL
    @UniqueConstraint(name = "uk_user_username", columnNames = "username"),
    @UniqueConstraint(name = "uk_user_email", columnNames = "email"),
    @UniqueConstraint(
        name = User.UK_USER_PROVIDER_IDENTITY,
        columnNames = {"provider", "provider_id"}
    )
})
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class User extends BaseEntity {

    public static final String UK_USER_USERNAME = "uk_user_username";
    public static final String UK_USER_EMAIL = "uk_user_email";
    public static final String UK_USER_PROVIDER_IDENTITY = "uk_user_provider_identity";

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(nullable = false, length = 50)
    private String username;

    @Column(nullable = false)
    private String email;

    // Nullable because users logging in via Google/Apple won't have a password
    private String password;

    @Column(length = 500)
    private String avatarUrl;

    // --- Social Login Fields ---

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private AuthProvider provider = AuthProvider.LOCAL;

    // Stores the unique ID returned by Google/Facebook/Apple...
    private String providerId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private Role role = Role.USER;

    @Column(nullable = false)
    @Builder.Default
    private boolean enabled = true;

    @Column(nullable = false)
    @Builder.Default
    private boolean accountNonLocked = true;
}