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

    @Mock
    private CategoryValidator categoryValidator;

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
    @DisplayName("Tests for createCategory")
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

    @Nested
    @DisplayName("Tests for updateCategory")
    class UpdateCategoryTests {

        @Test
        @DisplayName("Should update successfully when category exists and name is unique")
        void updateCategory_ValidRequest_Success() {
            // Given
            UUID id = UUID.randomUUID();
            CategoryRequest request = buildRequest().name("New Unique Name").build(); // User wants to change name

            Category existingEntity = buildEntity().id(id).name("Old Name").build();
            Category updatedEntity = buildEntity().id(id).name("New Unique Name").build();
            CategoryResponse expectedResponse = buildResponse().id(id).name("New Unique Name").build();

            // 1. Found existing
            given(categoryRepository.findById(id)).willReturn(Optional.of(existingEntity));

            // 2. Name changed, so check DB for duplicates (excluding current ID) -> Returns false (Unique)
            given(categoryRepository.existsByNameAndIdNot(request.name(), id)).willReturn(false);

            // 3. Save returns the updated entity
            given(categoryRepository.save(existingEntity)).willReturn(updatedEntity);
            given(categoryMapper.toResponse(updatedEntity)).willReturn(expectedResponse);

            // When
            CategoryResponse result = categoryService.updateCategory(id, request);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.name()).isEqualTo("New Unique Name");

            // Verify mapper was called to update state
            verify(categoryMapper).updateEntity(existingEntity, request);
            verify(categoryRepository).save(existingEntity);
        }

        @Test
        @DisplayName("Should throw exception when updating non existent category")
        void updateCategory_NotFound_ThrowsException() {
            // Given
            UUID id = UUID.randomUUID();
            CategoryRequest request = buildRequest().build();

            given(categoryRepository.findById(id))
                .willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> categoryService.updateCategory(id, request))
                .isInstanceOf(AppException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.CATEGORY_NOT_FOUND);

            verify(categoryRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw exception when new name conflicts with another category")
        void updateCategory_DuplicateName_ThrowsException() {
            // Given --------------------
            UUID id = UUID.randomUUID();
            CategoryRequest request = buildRequest().name("Duplicate Name").build();

            Category existingEntity = buildEntity().id(id).name("Original Name").build();

            given(categoryRepository.findById(id)).willReturn(Optional.of(existingEntity));

            // Simulating conflict: Name IS found on a DIFFERENT ID
            doThrow(new AppException(ErrorCode.CATEGORY_EXISTED, "Category name already exists"))
                .when(categoryValidator).validateForUpdate(existingEntity, request);

            // When & Then --------------------
            assertThatThrownBy(() -> categoryService.updateCategory(id, request))
                .isInstanceOf(AppException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.CATEGORY_EXISTED);

            verify(categoryRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should skip duplicate check if name remains unchanged")
        void updateCategory_SameName_SkipsCheckAndSaves() {
            // Given
            UUID id = UUID.randomUUID();
            String currentName = "Same Name";

            // Request has same name but other fields may differ
            CategoryRequest request = buildRequest().name(currentName).colorCode("#000000").build();
            Category existingEntity = buildEntity().id(id).name(currentName).build();

            given(categoryRepository.findById(id)).willReturn(Optional.of(existingEntity));

            // Mocking successful save
            given(categoryRepository.save(existingEntity)).willReturn(existingEntity);
            given(categoryMapper.toResponse(existingEntity)).willReturn(buildResponse().build());

            // When & Then
            categoryService.updateCategory(id, request);

            // Verify: NEVER checked for duplicates (because name didn't change)
            verify(categoryRepository, never()).existsByNameAndIdNot(anyString(), any());
            verify(categoryRepository).save(existingEntity);
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