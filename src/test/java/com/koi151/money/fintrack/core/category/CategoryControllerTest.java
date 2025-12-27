package com.koi151.money.fintrack.core.category;

import com.koi151.money.fintrack.common.ApiResponse;
import com.koi151.money.fintrack.common.exception.ErrorCode;
import com.koi151.money.fintrack.core.transaction.TransactionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.client.MockMvcWebTestClient;

import java.time.Instant;
import java.util.UUID;

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
            var request = buildRequest(DEFAULT_NAME);
            var response = buildResponse(categoryId);

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
                .jsonPath("$.message").isEqualTo("Successfully created category") // Message từ Controller

                // Assert Payload
                .jsonPath("$.result.id").isEqualTo(categoryId.toString())
                .jsonPath("$.result.name").isEqualTo(DEFAULT_NAME)
                .jsonPath("$.result.type").isEqualTo(DEFAULT_TYPE.name());
        }

        @Test
        @DisplayName("Should return 400 and validation error map when request is invalid")
        void createCategory_InvalidRequest_Returns400() {
            // Given
            CategoryRequest invalidRequest = buildRequest("");

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
                .jsonPath("$.result.name").isEqualTo("Category name is required");
        }
    }

    @Nested
    @DisplayName("GET " + BASE_URL + "/{id}")
    class GetCategoryTests {

        @Test
        @DisplayName("Should return category detail inside result object")
        void getCategory_Exists_Returns200() {
            // Given
            var response = buildResponse(categoryId);
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
                .jsonPath("$.message").isEqualTo("Successfully deleted category")
                .jsonPath("$.result").doesNotExist();
        }
    }

    // --- Helpers ---

    private CategoryRequest buildRequest(String name) {
        return CategoryRequest.builder()
            .name(name)
            .userId(userId)
            .type(DEFAULT_TYPE)
            .iconCode(DEFAULT_ICON)
            .colorCode(DEFAULT_COLOR)
            .build();
    }

    private CategoryResponse buildResponse(UUID id) {
        return CategoryResponse.builder()
            .id(id)
            .name(DEFAULT_NAME)
            .type(DEFAULT_TYPE)
            .iconCode(DEFAULT_ICON)
            .colorCode(DEFAULT_COLOR)
            .createdAt(fixedNow)
            .build();
    }
}