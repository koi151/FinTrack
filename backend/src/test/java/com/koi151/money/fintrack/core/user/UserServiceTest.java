package com.koi151.money.fintrack.core.user;

import com.koi151.money.fintrack.common.exception.AppException;
import com.koi151.money.fintrack.common.exception.ErrorCode;
import com.koi151.money.fintrack.core.user.domain.AuthProvider;
import com.koi151.money.fintrack.core.user.domain.Role;
import com.koi151.money.fintrack.core.user.domain.User;
import com.koi151.money.fintrack.core.user.domain.UserFactory;
import com.koi151.money.fintrack.core.user.payload.UserRegisterRequest;
import com.koi151.money.fintrack.core.user.payload.UserResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserFactory userFactory;

    @InjectMocks
    private UserService userService;

    // Common Test Values
    private static final String DEFAULT_USERNAME = "testuser";
    private static final String DEFAULT_EMAIL = "test@example.com";
    private static final String DEFAULT_PASSWORD = "Password123";
    private static final String ENCODED_PASSWORD = "encodedPassword123";

    @Nested
    @DisplayName("Tests for register")
    class RegisterTests {

        @Test
        @DisplayName("Should register user successfully with valid and unique credentials")
        void register_ValidAndUnique_Success() {
            // Given
            UUID userId = UUID.randomUUID();

            UserRegisterRequest request = UserRegisterRequest.builder()
                .username(DEFAULT_USERNAME)
                .email(DEFAULT_EMAIL)
                .password(DEFAULT_PASSWORD)
                .build();

            User newUser = User.builder()
                .username(DEFAULT_USERNAME.toLowerCase())
                .email(DEFAULT_EMAIL.toLowerCase())
                .password(ENCODED_PASSWORD)
                .provider(AuthProvider.LOCAL)
                .role(Role.USER)
                .enabled(true)
                .accountNonLocked(true)
                .build();

            User savedUser = User.builder()
                .id(userId)
                .username(DEFAULT_USERNAME.toLowerCase())
                .email(DEFAULT_EMAIL.toLowerCase())
                .password(ENCODED_PASSWORD)
                .provider(AuthProvider.LOCAL)
                .role(Role.USER)
                .enabled(true)
                .accountNonLocked(true)
                .build();

            UserResponse expectedResponse = UserResponse.builder()
                .id(userId)
                .username(DEFAULT_USERNAME.toLowerCase())
                .email(DEFAULT_EMAIL.toLowerCase())
                .build();

            given(passwordEncoder.encode(DEFAULT_PASSWORD)).willReturn(ENCODED_PASSWORD);
            given(userFactory.createLocalUser(DEFAULT_USERNAME, DEFAULT_EMAIL, ENCODED_PASSWORD))
                .willReturn(newUser);
            given(userRepository.save(newUser)).willReturn(savedUser);
            given(userMapper.toResponse(savedUser)).willReturn(expectedResponse);

            // When
            UserResponse result = userService.register(request);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.id()).isEqualTo(userId);
            assertThat(result.username()).isEqualTo(DEFAULT_USERNAME.toLowerCase());
            assertThat(result.email()).isEqualTo(DEFAULT_EMAIL.toLowerCase());

            verify(passwordEncoder, times(1)).encode(DEFAULT_PASSWORD);
            verify(userFactory, times(1)).createLocalUser(DEFAULT_USERNAME, DEFAULT_EMAIL, ENCODED_PASSWORD);
            verify(userRepository, times(1)).save(newUser);
            verify(userMapper, times(1)).toResponse(savedUser);
        }

        @Test
        @DisplayName("Should throw exception when username already exists")
        void register_DuplicateUsername_ThrowsException() {
            // Given
            UserRegisterRequest request = UserRegisterRequest.builder()
                .username(DEFAULT_USERNAME)
                .email(DEFAULT_EMAIL)
                .password(DEFAULT_PASSWORD)
                .build();

            User newUser = User.builder()
                .username(DEFAULT_USERNAME.toLowerCase())
                .email(DEFAULT_EMAIL.toLowerCase())
                .password(ENCODED_PASSWORD)
                .build();

            given(passwordEncoder.encode(DEFAULT_PASSWORD)).willReturn(ENCODED_PASSWORD);
            given(userFactory.createLocalUser(DEFAULT_USERNAME, DEFAULT_EMAIL, ENCODED_PASSWORD))
                .willReturn(newUser);

            // Simulate unique constraint violation for username
            Throwable rootCause = new RuntimeException("duplicate key value violates unique constraint \"uk_user_username\"");
            DataIntegrityViolationException dbException = mock(DataIntegrityViolationException.class);
            given(dbException.getMostSpecificCause()).willReturn(rootCause);
            given(userRepository.save(newUser)).willThrow(dbException);

            // When & Then
            assertThatThrownBy(() -> userService.register(request))
                .isInstanceOf(AppException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USERNAME_EXISTED);

            verify(userRepository, times(1)).save(newUser);
            verify(userMapper, never()).toResponse(any());
        }

        @Test
        @DisplayName("Should throw exception when email already exists")
        void register_DuplicateEmail_ThrowsException() {
            // Given
            UserRegisterRequest request = UserRegisterRequest.builder()
                .username(DEFAULT_USERNAME)
                .email(DEFAULT_EMAIL)
                .password(DEFAULT_PASSWORD)
                .build();

            User newUser = User.builder()
                .username(DEFAULT_USERNAME.toLowerCase())
                .email(DEFAULT_EMAIL.toLowerCase())
                .password(ENCODED_PASSWORD)
                .build();

            given(passwordEncoder.encode(DEFAULT_PASSWORD)).willReturn(ENCODED_PASSWORD);
            given(userFactory.createLocalUser(DEFAULT_USERNAME, DEFAULT_EMAIL, ENCODED_PASSWORD))
                .willReturn(newUser);

            // Simulate unique constraint violation for email
            Throwable rootCause = new RuntimeException("duplicate key value violates unique constraint \"uk_user_email\"");
            DataIntegrityViolationException dbException = mock(DataIntegrityViolationException.class);
            given(dbException.getMostSpecificCause()).willReturn(rootCause);
            given(userRepository.save(newUser)).willThrow(dbException);

            // When & Then
            assertThatThrownBy(() -> userService.register(request))
                .isInstanceOf(AppException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_EMAIL_EXISTED);

            verify(userRepository, times(1)).save(newUser);
            verify(userMapper, never()).toResponse(any());
        }

        @Test
        @DisplayName("Should throw exception when OAuth account already linked")
        void register_OAuthAccountAlreadyLinked_ThrowsException() {
            // Given
            UserRegisterRequest request = UserRegisterRequest.builder()
                .username(DEFAULT_USERNAME)
                .email(DEFAULT_EMAIL)
                .password(DEFAULT_PASSWORD)
                .build();

            User newUser = User.builder()
                .username(DEFAULT_USERNAME.toLowerCase())
                .email(DEFAULT_EMAIL.toLowerCase())
                .password(ENCODED_PASSWORD)
                .build();

            given(passwordEncoder.encode(DEFAULT_PASSWORD)).willReturn(ENCODED_PASSWORD);
            given(userFactory.createLocalUser(DEFAULT_USERNAME, DEFAULT_EMAIL, ENCODED_PASSWORD))
                .willReturn(newUser);

            // Simulate unique constraint violation for provider identity
            Throwable rootCause = new RuntimeException("duplicate key value violates unique constraint \"uk_user_provider_identity\"");
            DataIntegrityViolationException dbException = mock(DataIntegrityViolationException.class);
            given(dbException.getMostSpecificCause()).willReturn(rootCause);
            given(userRepository.save(newUser)).willThrow(dbException);

            // When & Then
            assertThatThrownBy(() -> userService.register(request))
                .isInstanceOf(AppException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.OAUTH_ACCOUNT_ALREADY_LINKED);

            verify(userRepository, times(1)).save(newUser);
            verify(userMapper, never()).toResponse(any());
        }

        @Test
        @DisplayName("Should encode password before saving")
        void register_ValidRequest_EncodesPassword() {
            // Given
            UUID userId = UUID.randomUUID();

            UserRegisterRequest request = UserRegisterRequest.builder()
                .username(DEFAULT_USERNAME)
                .email(DEFAULT_EMAIL)
                .password(DEFAULT_PASSWORD)
                .build();

            User newUser = User.builder()
                .username(DEFAULT_USERNAME.toLowerCase())
                .email(DEFAULT_EMAIL.toLowerCase())
                .password(ENCODED_PASSWORD)
                .build();

            User savedUser = User.builder()
                .id(userId)
                .username(DEFAULT_USERNAME.toLowerCase())
                .email(DEFAULT_EMAIL.toLowerCase())
                .password(ENCODED_PASSWORD)
                .build();

            UserResponse expectedResponse = UserResponse.builder()
                .id(userId)
                .username(DEFAULT_USERNAME.toLowerCase())
                .email(DEFAULT_EMAIL.toLowerCase())
                .build();

            given(passwordEncoder.encode(DEFAULT_PASSWORD)).willReturn(ENCODED_PASSWORD);
            given(userFactory.createLocalUser(DEFAULT_USERNAME, DEFAULT_EMAIL, ENCODED_PASSWORD))
                .willReturn(newUser);
            given(userRepository.save(newUser)).willReturn(savedUser);
            given(userMapper.toResponse(savedUser)).willReturn(expectedResponse);

            // When
            userService.register(request);

            // Then
            verify(passwordEncoder, times(1)).encode(DEFAULT_PASSWORD);
            verify(userFactory, times(1)).createLocalUser(
                DEFAULT_USERNAME,
                DEFAULT_EMAIL,
                ENCODED_PASSWORD // Verify encoded password is used
            );
        }

        @Test
        @DisplayName("Should create user with default values via UserFactory")
        void register_ValidRequest_SetsDefaultValues() {
            // Given
            UUID userId = UUID.randomUUID();

            UserRegisterRequest request = UserRegisterRequest.builder()
                .username(DEFAULT_USERNAME)
                .email(DEFAULT_EMAIL)
                .password(DEFAULT_PASSWORD)
                .build();

            User newUser = User.builder()
                .username(DEFAULT_USERNAME.toLowerCase())
                .email(DEFAULT_EMAIL.toLowerCase())
                .password(ENCODED_PASSWORD)
                .provider(AuthProvider.LOCAL)
                .role(Role.USER)
                .enabled(true)
                .accountNonLocked(true)
                .build();

            User savedUser = User.builder()
                .id(userId)
                .username(DEFAULT_USERNAME.toLowerCase())
                .email(DEFAULT_EMAIL.toLowerCase())
                .password(ENCODED_PASSWORD)
                .provider(AuthProvider.LOCAL)
                .role(Role.USER)
                .enabled(true)
                .accountNonLocked(true)
                .build();

            UserResponse expectedResponse = UserResponse.builder()
                .id(userId)
                .username(DEFAULT_USERNAME.toLowerCase())
                .email(DEFAULT_EMAIL.toLowerCase())
                .build();

            given(passwordEncoder.encode(DEFAULT_PASSWORD)).willReturn(ENCODED_PASSWORD);
            given(userFactory.createLocalUser(DEFAULT_USERNAME, DEFAULT_EMAIL, ENCODED_PASSWORD))
                .willReturn(newUser);
            given(userRepository.save(newUser)).willReturn(savedUser);
            given(userMapper.toResponse(savedUser)).willReturn(expectedResponse);

            // When
            userService.register(request);

            // Then
            verify(userFactory, times(1)).createLocalUser(DEFAULT_USERNAME, DEFAULT_EMAIL, ENCODED_PASSWORD);
            assertThat(newUser.getProvider()).isEqualTo(AuthProvider.LOCAL);
            assertThat(newUser.getRole()).isEqualTo(Role.USER);
            assertThat(newUser.isEnabled()).isTrue();
            assertThat(newUser.isAccountNonLocked()).isTrue();
        }

        @Test
        @DisplayName("Should handle null password by throwing exception")
        void register_NullPassword_ThrowsException() {
            // Given
            UserRegisterRequest request = UserRegisterRequest.builder()
                .username(DEFAULT_USERNAME)
                .email(DEFAULT_EMAIL)
                .password(null)
                .build();

            given(passwordEncoder.encode(null)).willThrow(new IllegalArgumentException("Password cannot be null"));

            // When & Then
            assertThatThrownBy(() -> userService.register(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Password cannot be null");

            verify(userFactory, never()).createLocalUser(anyString(), anyString(), anyString());
            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should normalize username and email through UserFactory")
        void register_MixedCaseCredentials_NormalizesToLowerCase() {
            // Given
            UUID userId = UUID.randomUUID();
            String mixedCaseUsername = "TestUser";
            String mixedCaseEmail = "Test@Example.COM";

            UserRegisterRequest request = UserRegisterRequest.builder()
                .username(mixedCaseUsername)
                .email(mixedCaseEmail)
                .password(DEFAULT_PASSWORD)
                .build();

            User newUser = User.builder()
                .username(mixedCaseUsername.toLowerCase())
                .email(mixedCaseEmail.toLowerCase())
                .password(ENCODED_PASSWORD)
                .build();

            User savedUser = User.builder()
                .id(userId)
                .username(mixedCaseUsername.toLowerCase())
                .email(mixedCaseEmail.toLowerCase())
                .password(ENCODED_PASSWORD)
                .build();

            UserResponse expectedResponse = UserResponse.builder()
                .id(userId)
                .username(mixedCaseUsername.toLowerCase())
                .email(mixedCaseEmail.toLowerCase())
                .build();

            given(passwordEncoder.encode(DEFAULT_PASSWORD)).willReturn(ENCODED_PASSWORD);
            given(userFactory.createLocalUser(mixedCaseUsername, mixedCaseEmail, ENCODED_PASSWORD))
                .willReturn(newUser);
            given(userRepository.save(newUser)).willReturn(savedUser);
            given(userMapper.toResponse(savedUser)).willReturn(expectedResponse);

            // When
            UserResponse result = userService.register(request);

            // Then
            verify(userFactory, times(1)).createLocalUser(mixedCaseUsername, mixedCaseEmail, ENCODED_PASSWORD);
            assertThat(result.username()).isEqualTo(mixedCaseUsername.toLowerCase());
            assertThat(result.email()).isEqualTo(mixedCaseEmail.toLowerCase());
        }
    }
}

