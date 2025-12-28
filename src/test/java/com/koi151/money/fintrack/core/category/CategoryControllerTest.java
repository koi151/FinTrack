package com.koi151.money.fintrack.core.category;

import com.koi151.money.fintrack.common.ApiResponse;
import com.koi151.money.fintrack.common.exception.ErrorCode;
import com.koi151.money.fintrack.core.transaction.TransactionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.client.MockMvcWebTestClient;

import java.time.Instant;
import java.util.UUID;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;

@WebMvcTest(CategoryController.class)
@AutoConfigureMockMvc(addFilters = false) // skip security filter, only test controller logic
class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CategoryService categoryService;

    private WebTestClient webTestClient; // assert JSON

    private static final String BASE_URL = "/api/v1/categories";
    private static final String DEFAULT_NAME = "Food & Dining";
    private static final String DEFAULT_ICON = "food-icon";
    private static final String DEFAULT_COLOR = "#FF5733";
    private static final TransactionType DEFAULT_TYPE = TransactionType.EXPENSE;

    private UUID userId;
    private UUID categoryId;
    private Instant fixedNow;

    @BeforeEach
    void setUp() {
        this.webTestClient = MockMvcWebTestClient.bindTo(mockMvc).build();
        this.userId = UUID.randomUUID();
        this.categoryId = UUID.randomUUID();
        this.fixedNow = Instant.parse("2025-01-01T10:00:00Z");
    }

    @Nested
    @DisplayName("POST " + BASE_URL)
    class CreateCategoryTests {

        @Test
        @DisplayName("Should create category and return wrapped result when valid")
        void createCategory_ValidRequest_ReturnsSuccess() {
            // Given
            var request = buildRequest().build();
            var response = buildResponse().build();

            given(categoryService.createCategory(any(CategoryRequest.class)))
                .willReturn(response);

            // When & Then
            webTestClient.post()
                .uri(BASE_URL)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk()
                .expectBody()

                // Assert Envelope Structure
                .jsonPath("$.code").isEqualTo(ApiResponse.SUCCESS_CODE)
                .jsonPath("$.message").isEqualTo("Successfully created category")

                // Assert Payload
                .jsonPath("$.result.id").isEqualTo(categoryId.toString())
                .jsonPath("$.result.name").isEqualTo(DEFAULT_NAME)
                .jsonPath("$.result.type").isEqualTo(DEFAULT_TYPE.name());
        }

        @Test
        @DisplayName("Should return 400 and validation error map when request is invalid")
        void createCategory_InvalidRequest_Returns400() {
            // Given
            var invalidRequest = buildRequest().name("").build();

            // When & Then
            webTestClient.post()
                .uri(BASE_URL)
                .bodyValue(invalidRequest)
                .exchange()
                .expectStatus().isBadRequest() // HTTP 400
                .expectBody()

                // Business code check
                .jsonPath("$.code").isEqualTo(ErrorCode.INVALID_PARAM.getCode())

                // message check
                .jsonPath("$.message").isEqualTo(ErrorCode.INVALID_PARAM.getMessage())

                .jsonPath("$.result").isMap()
                .jsonPath("$.result.name").exists();
        }

        @ParameterizedTest
        @MethodSource("provideInvalidCategoryRequests")
        @DisplayName("Should return 400 and list specific field errors for CategoryRequest")
        void createCategory_InvalidRequests_Returns400(CategoryRequest invalidRequest, String[] expectedFields) {
            var actions = webTestClient.post()
                    .uri(BASE_URL)
                    .bodyValue(invalidRequest)
                    .exchange()
                    .expectStatus().isBadRequest()
                    .expectBody()
                    .jsonPath("$.code").isEqualTo(ErrorCode.INVALID_PARAM.getCode())
                    .jsonPath("$.result").isMap();

            for (String field : expectedFields) { // check if message warning for each field exists
                actions.jsonPath("$.result." + field).exists();
            }
        }

        // Provider for single & multiple invalid field testing
        private static Stream<Arguments> provideInvalidCategoryRequests() {
            UUID validUser = UUID.randomUUID();

            return Stream.of(
                // Case 1: empty name
                Arguments.of(
                    CategoryRequest.builder()
                        .name("")
                        .userId(validUser)
                        .type(TransactionType.EXPENSE)
                        .build(),
                    new String[]{"name"}
                ),

                // Case 2: wrong color code format
                Arguments.of(
                    CategoryRequest.builder()
                        .name("Food")
                        .userId(validUser)
                        .type(TransactionType.EXPENSE)
                        .colorCode("ZZZ123")
                        .build(),
                    new String[]{"colorCode"}
                ),

                // Case 3: Multiple missing fields
                Arguments.of(
                    CategoryRequest.builder()
                        .name("Shopping")
                        .userId(null)
                        .type(null)
                        .build(),
                    new String[]{"userId", "type"}
                ),

                // Case 4: fields exceed max length
                Arguments.of(
                    CategoryRequest.builder()
                        .name("a".repeat(101)) // Max 100
                        .userId(validUser)
                        .type(TransactionType.EXPENSE)
                        .iconCode("b".repeat(51)) // Max 50
                        .build(),
                    new String[]{"name", "iconCode"}
                )
            );
        }
    }

    @Nested
    @DisplayName("GET " + BASE_URL + "/{id}")
    class GetCategoryTests {

        @Test
        @DisplayName("Should return category detail inside result object")
        void getCategory_Exists_Returns200() {
            // Given
            var response = buildResponse().build();
            given(categoryService.getCategory(categoryId)).willReturn(response);

            // When & Then
            webTestClient.get()
                .uri(BASE_URL + "/{id}", categoryId)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.code").isEqualTo(ApiResponse.SUCCESS_CODE)
                .jsonPath("$.result.id").isEqualTo(categoryId.toString())
                .jsonPath("$.result.name").isEqualTo(DEFAULT_NAME);
        }
    }

    @Nested
    @DisplayName("DELETE " + BASE_URL + "/{id}")
    class DeleteCategoryTests {

        @Test
        @DisplayName("Should delete and return custom 204 code in body")
        void deleteCategory_Exists_ReturnsSuccessWrapper() {
            // Given
            doNothing().when(categoryService).deleteCategory(categoryId);

            // When & Then
            webTestClient.delete()
                .uri(BASE_URL + "/{id}", categoryId)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.message").exists()
                .jsonPath("$.result").doesNotExist();
        }
    }

    // --- Helpers ---

    private CategoryRequest.CategoryRequestBuilder buildRequest() {
        return CategoryRequest.builder()
            .name(DEFAULT_NAME)
            .userId(userId)
            .type(DEFAULT_TYPE)
            .iconCode(DEFAULT_ICON)
            .colorCode(DEFAULT_COLOR);
    }

    private CategoryResponse.CategoryResponseBuilder buildResponse() {
        return CategoryResponse.builder()
            .id(categoryId)
            .name(DEFAULT_NAME)
            .type(DEFAULT_TYPE)
            .iconCode(DEFAULT_ICON)
            .colorCode(DEFAULT_COLOR)
            .createdAt(fixedNow);
    }
}