package com.koi151.money.fintrack.core.category;

import com.koi151.money.fintrack.common.exception.AppException;
import com.koi151.money.fintrack.common.exception.ErrorCode;
import com.koi151.money.fintrack.core.transaction.TransactionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private CategoryMapper categoryMapper;

    @InjectMocks
    private CategoryService categoryService;

    // --- Test Constants ---
    private static final String DEFAULT_NAME = "Food & Dining";
    private static final TransactionType DEFAULT_TYPE = TransactionType.EXPENSE;
    private static final String DEFAULT_ICON = "food-icon";
    private static final String DEFAULT_COLOR = "#FF5733";

    private UUID userId;
    private Instant fixedNow;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        fixedNow = Instant.now();
    }

    @Nested
    @DisplayName("Tests for createCategory Logic")
    class CreateCategoryTests {

        @Test
        @DisplayName("Should successfully create category when request is valid and unique")
        void createCategory_ValidAndUnique_Success() {
            // Given
            CategoryRequest request = buildRequest().build();

            Category entity = new Category();
            Category savedEntity = buildEntity().build();
            CategoryResponse expectedResponse = buildResponse()
                .id(savedEntity.getId())
                .build();

            given(categoryRepository.existsByName(DEFAULT_NAME)).willReturn(false);
            given(categoryMapper.toEntity(request)).willReturn(entity);
            given(categoryRepository.save(entity)).willReturn(savedEntity);
            given(categoryMapper.toResponse(savedEntity)).willReturn(expectedResponse);

            // When
            CategoryResponse result = categoryService.createCategory(request);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.name()).isEqualTo(DEFAULT_NAME);
            assertThat(result.createdAt()).isEqualTo(fixedNow);
            verify(categoryRepository).save(any(Category.class));
        }

        @Test
        @DisplayName("Should throw exception when category name already exists")
        void createCategory_DuplicateName_ThrowsException() {
            // Given
            CategoryRequest request = buildRequest().build();
            given(categoryRepository.existsByName(DEFAULT_NAME)).willReturn(true);

            // When & Then
            assertThatThrownBy(() -> categoryService.createCategory(request))
                .isInstanceOf(AppException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.CATEGORY_EXISTED);

            // Verify early exit to save resources
            verify(categoryRepository, never()).save(any());
            verify(categoryMapper, never()).toEntity(any());
        }

        @Test
        @DisplayName("Should ensure transactional integrity on unexpected DB failure")
        void createCategory_DatabaseError_PropagatesException() {
            // Given
            CategoryRequest request = buildRequest().build();
            given(categoryRepository.existsByName(DEFAULT_NAME)).willReturn(false);
            given(categoryMapper.toEntity(request)).willReturn(new Category());
            given(categoryRepository.save(any())).willThrow(new RuntimeException("Persistence Error"));

            // When & Then
            assertThatThrownBy(() -> categoryService.createCategory(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Persistence Error");
        }
    }

    @Nested
    @DisplayName("Test for deleteCategory")
    class DeleteCategoryTests {

        @Test
        @DisplayName("Should delete category when category exists")
        void deleteCategory_Exists_Success() {
            // Given
            UUID id = UUID.randomUUID();
            Category category = buildEntity()
                .id(id)
                .build();
            given(categoryRepository.findById(id)).willReturn(Optional.of(category));

            // When & Then
            categoryService.deleteCategory(id);

            verify(categoryRepository, times(1)).delete(category);
        }

        @Test
        @DisplayName("Should handle delete gracefully if category already soft deleted")
        void deleteTransaction_AlreadyDeleted_ThrowsNotFound() {
            // Given
            UUID id = UUID.randomUUID();
            given(categoryRepository.findById(id)).willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> categoryService.deleteCategory(id))
                .isInstanceOf(AppException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.CATEGORY_NOT_FOUND);

            verify(categoryRepository, never()).delete(any());
        }
    }

    // HELPERS ---------------------------------------------------------
    /**
     * Test Data Builder allow overriding fields
     */
    private CategoryRequest.CategoryRequestBuilder buildRequest() {
        return CategoryRequest.builder()
            .name(DEFAULT_NAME)
            .userId(userId)
            .type(DEFAULT_TYPE)
            .iconCode(DEFAULT_ICON)
            .colorCode(DEFAULT_COLOR);
    }

    /**
     * mock Entity with auditing data.
     */
    // Use wildcards to handle @SuperBuilder's recursive generics cleanly
    private Category.CategoryBuilder<?, ?> buildEntity() {
        return Category.builder()
            .id(UUID.randomUUID())
            .name(CategoryServiceTest.DEFAULT_NAME)
            .userId(userId)
            .type(DEFAULT_TYPE)
            .createdAt(fixedNow);
    }

    /**
     * mock Response for Mapper verification.
     */
    private CategoryResponse.CategoryResponseBuilder buildResponse() {
        return CategoryResponse.builder()
            .id(UUID.randomUUID())
            .name(DEFAULT_NAME)
            .type(DEFAULT_TYPE)
            .iconCode(DEFAULT_ICON)
            .colorCode(DEFAULT_COLOR)
            .createdAt(fixedNow);
    }
}