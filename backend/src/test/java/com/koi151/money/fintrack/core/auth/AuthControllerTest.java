package com.koi151.money.fintrack.core.auth;

import com.koi151.money.fintrack.common.AppResponse;
import com.koi151.money.fintrack.common.exception.AppException;
import com.koi151.money.fintrack.common.exception.ErrorCode;
import com.koi151.money.fintrack.core.user.UserService;
import com.koi151.money.fintrack.core.user.payload.UserRegisterRequest;
import com.koi151.money.fintrack.core.user.payload.UserResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.client.MockMvcWebTestClient;

import java.util.UUID;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    private WebTestClient webTestClient;

    private static final String BASE_URL = "/api/v1/auth";
    private static final String REGISTER_URL = BASE_URL + "/register";

    private UUID userId;
    private String validUsername;
    private String validEmail;
    private String validPassword;

    @BeforeEach
    void setUp() {
        this.webTestClient = MockMvcWebTestClient.bindTo(mockMvc).build();
        this.userId = UUID.randomUUID();
        this.validUsername = "johndoe";
        this.validEmail = "john.doe@example.com";
        this.validPassword = "SecurePass123";
    }

    @Nested
    @DisplayName("POST " + BASE_URL + "/register")
    class RegisterTests {

        @Test
        @DisplayName("Should register user successfully with valid data")
        void register_ValidRequest_ReturnsCreated() {
            // Given
            var request = buildRequest().build();
            var response = buildResponse().build();

            given(userService.register(any(UserRegisterRequest.class)))
                .willReturn(response);

            // When & Then
            webTestClient.post()
                .uri(REGISTER_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()

                .jsonPath("$.code").isEqualTo(AppResponse.SUCCESS_CODE)
                .jsonPath("$.message").isEqualTo("User registered successfully")

                .jsonPath("$.result").isNotEmpty()
                .jsonPath("$.result.id").isEqualTo(response.id().toString())
                .jsonPath("$.result.username").isEqualTo(response.username())
                .jsonPath("$.result.email").isEqualTo(response.email());
        }

        @DisplayName("Should return 409 Conflict for duplicate resource scenarios")
        @ParameterizedTest(name = "Scenario: {0}") // Customizes display name in test results
        @EnumSource(value = ErrorCode.class, names = {
            "USER_EMAIL_EXISTED",
            "USERNAME_EXISTED"
        })
        void register_WhenUserAlreadyExists_ReturnsConflict(ErrorCode errorCode) {
            // Given
            var request = buildRequest().build();

            // Dynamic mocking based on the passed error code
            given(userService.register(any(UserRegisterRequest.class)))
                    .willThrow(new AppException(errorCode));

            // When & Then
            webTestClient.post()
                .uri(REGISTER_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()

                .expectStatus().isEqualTo(409)

                .expectBody()
                .jsonPath("$.code").isEqualTo(errorCode.getCode())
                .jsonPath("$.message").isNotEmpty();
        }

        // BLANK CHECKS
        @ParameterizedTest
        @DisplayName("Should return 400 when required fields are blank")
        @MethodSource("provideBlankFieldRequests")
        void register_BlankFields_ReturnsBadRequest(UserRegisterRequest request, String fieldName) {
            webTestClient.post()
                .uri(REGISTER_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isBadRequest()

                .expectBody()
                .jsonPath("$.code").isEqualTo(ErrorCode.INVALID_PARAM.getCode())
                .jsonPath("$.message").isNotEmpty();
        }

        // LENGTH EXCEEDED CHECKS
        @ParameterizedTest
        @DisplayName("Should return 400 when fields exceed max length")
        @MethodSource("provideExceedLengthRequests")
        void register_ExceedLength_ReturnsBadRequest(UserRegisterRequest request, String fieldName) {
            webTestClient.post()
                .uri(REGISTER_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isBadRequest()

                .expectBody()
                .jsonPath("$.code").isEqualTo(ErrorCode.INVALID_PARAM.getCode())
                .jsonPath("$.message").isNotEmpty();
        }

        // MIN LENGTH CHECKS
        @ParameterizedTest
        @DisplayName("Should return 400 when fields are too short")
        @MethodSource("provideShortFieldRequests")
        void register_ShortFields_ReturnsBadRequest(UserRegisterRequest request, String fieldName) {
            webTestClient.post()
                .uri(REGISTER_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isBadRequest()

                .expectBody()
                .jsonPath("$.code").isEqualTo(ErrorCode.INVALID_PARAM.getCode())
                .jsonPath("$.message").isNotEmpty();
        }

        @Test
        @DisplayName("Should return 400 when email format is invalid")
        void register_InvalidEmailFormat_ReturnsBadRequest() {
            // Given
            var request = buildRequest()
                .email("invalid-email")
                .build();

            // When & Then
            webTestClient.post()
                .uri(REGISTER_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isBadRequest()

                .expectBody()
                .jsonPath("$.code").isEqualTo(ErrorCode.INVALID_PARAM.getCode())
                .jsonPath("$.message").isNotEmpty();
        }

        @Test
        @DisplayName("Should return 400 when password lacks uppercase letter")
        void register_PasswordNoUppercase_ReturnsBadRequest() {
            // Given
            var request = buildRequest()
                .password("password123")  // No uppercase
                .build();

            // When & Then
            webTestClient.post()
                .uri(REGISTER_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isBadRequest()

                .expectBody()
                .jsonPath("$.code").isEqualTo(ErrorCode.INVALID_PARAM.getCode())
                .jsonPath("$.message").isNotEmpty();
        }

        @Test
        @DisplayName("Should return 400 when password lacks lowercase letter")
        void register_PasswordNoLowercase_ReturnsBadRequest() {
            // Given
            var request = buildRequest()
                .password("PASSWORD123")
                .build();

            // When & Then
            webTestClient.post()
                .uri(REGISTER_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isBadRequest()

                .expectBody()
                .jsonPath("$.code").isEqualTo(ErrorCode.INVALID_PARAM.getCode())
                .jsonPath("$.message").isNotEmpty();
        }

        @Test
        @DisplayName("Should return 400 when request body is null")
        void register_NullRequestBody_ReturnsBadRequest() {
            // When & Then
            webTestClient.post()
                .uri(REGISTER_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isBadRequest()

                .expectBody()
                .jsonPath("$.code").isEqualTo(ErrorCode.INVALID_PARAM.getCode())
                .jsonPath("$.message").isNotEmpty();
        }

        /**
         * Provides scenarios for the parameterized where mandatory fields are blank.
         */
        private static Stream<Arguments> provideBlankFieldRequests() {
            String validUsername = "johndoe";
            String validEmail = "john.doe@example.com";
            String validPassword = "SecurePass123";

            return Stream.of(
                Arguments.of(
                    new UserRegisterRequest("", validEmail, validPassword),
                    "username"
                ),
                Arguments.of(
                    new UserRegisterRequest("   ", validEmail, validPassword),
                    "username"
                ),
                Arguments.of(
                    new UserRegisterRequest(validUsername, "", validPassword),
                    "email"
                ),
                Arguments.of(
                    new UserRegisterRequest(validUsername, "   ", validPassword),
                    "email"
                ),
                Arguments.of(
                    new UserRegisterRequest(validUsername, validEmail, ""),
                    "password"
                ),
                Arguments.of(
                    new UserRegisterRequest(validUsername, validEmail, "   "),
                    "password"
                )
            );
        }

        /**
         * Provides scenarios where fields exceed their defined @Size(max)
         * Current constrain: Username <= 50, Email <= 100, Password <= 128
         */
        private static Stream<Arguments> provideExceedLengthRequests() {
            String validUsername = "johndoe";
            String validEmail = "john.doe@example.com";
            String validPassword = "SecurePass123";

            return Stream.of(
                // Username
                Arguments.of(
                    new UserRegisterRequest("a".repeat(51), validEmail, validPassword),
                    "username"
                ),

                // Email
                Arguments.of(
                    new UserRegisterRequest(validUsername, "a".repeat(90) + "@example.com", validPassword),
                    "email"
                ),

                // Password
                Arguments.of(
                    new UserRegisterRequest(validUsername, validEmail, "A".repeat(129)), // satisfied uppercase constraint
                    "password"
                )
            );
        }

        /**
         * Provides scenarios where fields are below their defined @Size(min).
         * Current constraints: Username >= 3, Password >= 8.
         */
        private static Stream<Arguments> provideShortFieldRequests() {
            String validUsername = "johndoe";
            String validEmail = "john.doe@example.com";
            String validPassword = "SecurePass123";

            return Stream.of(
                // Username
                Arguments.of(
                    new UserRegisterRequest("ab", validEmail, validPassword),
                    "username"
                ),

                // Password
                Arguments.of(
                    new UserRegisterRequest(validUsername, validEmail, "Pass1"),
                    "password"
                )
            );
        }
    }

    // ================= Helper Methods =================

    private UserRegisterRequest.UserRegisterRequestBuilder buildRequest() {
        return UserRegisterRequest.builder()
            .username(validUsername)
            .email(validEmail)
            .password(validPassword);
    }

    private UserResponse.UserResponseBuilder buildResponse() {
        return UserResponse.builder()
            .id(userId)
            .username(validUsername)
            .email(validEmail);
    }
}