package com.koi151.money.fintrack.core.transaction;

import com.koi151.money.fintrack.common.ApiResponse;
import com.koi151.money.fintrack.common.exception.ErrorCode;
import com.koi151.money.fintrack.core.transaction.payload.TransactionRequest;
import com.koi151.money.fintrack.core.transaction.payload.TransactionResponse;
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

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;

@WebMvcTest(TransactionController.class)
@AutoConfigureMockMvc(addFilters = false)
class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TransactionService transactionService;

    private WebTestClient webTestClient;

    private static final String BASE_URL = "/api/v1/transactions";
    private static final BigDecimal DEFAULT_AMOUNT = BigDecimal.valueOf(199.99);

    private UUID userId;
    private UUID categoryId;
    private UUID transactionId;
    private Instant transactionDate;

    @BeforeEach
    void setUp() {
        this.webTestClient = MockMvcWebTestClient.bindTo(mockMvc).build();
        this.userId = UUID.randomUUID();
        this.categoryId = UUID.randomUUID();
        this.transactionId = UUID.randomUUID();
    }

    @Nested
    @DisplayName("POST " + BASE_URL)
    class CreateTransactionTests {

        @Test
        @DisplayName("Should create transaction and return result when valid")
        void createTransaction_ValidRequest_ReturnsSuccess() {
            // Given
            var request = buildRequest().build();
            var response = buildResponse().build();

            given(transactionService.createTransaction(any(TransactionRequest.class)))
                    .willReturn(response);

            // When & Then
            webTestClient.post()
                .uri(BASE_URL)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk()
                .expectBody()

                .jsonPath("$.code").isEqualTo(ApiResponse.SUCCESS_CODE)
                .jsonPath("$.message").exists()

                .jsonPath("$.result.id").isEqualTo(transactionId)
                .jsonPath("$.result.userId").isEqualTo(userId)
                .jsonPath("$.result.amount").isEqualTo(DEFAULT_AMOUNT)
                .jsonPath("$.result.transactionDate").isEqualTo(transactionDate);
        }

        @ParameterizedTest
        @MethodSource("provideInvalidRequests")
        @DisplayName("Should return 400 and list all field errors when request is invalid")
        void createTransaction_InvalidRequests_Returns400(TransactionRequest invalidRequest, String[] expectedFields) {
            var actions = webTestClient.post()
                .uri(BASE_URL)
                .bodyValue(invalidRequest)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()

                .jsonPath("$.code").isEqualTo(ErrorCode.INVALID_PARAM.getCode())
                .jsonPath("$.message").isEqualTo(ErrorCode.INVALID_PARAM.getMessage())

                .jsonPath("$.result").isMap();

            for (String field : expectedFields) { // check if message warning for each field exists
                actions.jsonPath("$.result." + field).exists();
            }
        }

        // Provider for single & multiple invalid field testing
        private static Stream<Arguments> provideInvalidRequests() {
            UUID uId = UUID.randomUUID();
            UUID cId = UUID.randomUUID();
            return Stream.of(
                // Case 1: lack one field (categoryId)
                Arguments.of(TransactionRequest.builder()
                    .categoryId(cId)
                    .userId(uId)
                    .categoryId(null)
                    .build(),
                    new String[]{"categoryId"}),

                // Case 2: lack multiple fields
                Arguments.of(TransactionRequest.builder().build(),
                    new String[]{"amount", "categoryId", "userId"})
            );
        }
    }

    @Nested
    @DisplayName("DELETE " + BASE_URL + "/{id}")
    class DeleteTransactionTests {

        @Test
        @DisplayName("Should delete and return custom 204 code in body")
        void deleteTransaction_Exists_ReturnsSuccessWrapper() {
            // Given
            doNothing().when(transactionService).deleteTransaction(transactionId);

            // When & Then
            webTestClient.delete()
                .uri(BASE_URL + "/{id}", transactionId)
                .exchange()
                .expectStatus().isOk()
                .expectBody()

                .jsonPath("$.message").exists()
                .jsonPath("$.result").doesNotExist();
        }
    }

    // ================= Helper Methods =================

    private TransactionRequest.TransactionRequestBuilder buildRequest() {
        return TransactionRequest.builder()
            .amount(DEFAULT_AMOUNT)
            .categoryId(categoryId)
            .userId(userId)
            .transactionDate(transactionDate)
            .note("Grocery shopping");
    }

    private TransactionResponse.TransactionResponseBuilder buildResponse() {
        return TransactionResponse.builder()
            .id(transactionId)
            .amount(DEFAULT_AMOUNT)
            .categoryId(categoryId)
            .userId(userId)
            .transactionDate(transactionDate)
            .note("Grocery shopping");
    }
}