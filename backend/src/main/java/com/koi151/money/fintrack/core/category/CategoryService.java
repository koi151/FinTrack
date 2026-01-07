package com.koi151.money.fintrack.core.category;

import com.koi151.money.fintrack.common.exception.AppException;
import com.koi151.money.fintrack.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;
    private final CategoryValidator categoryValidator;

    public List<CategoryResponse> getCategories() {
        return categoryRepository.findAll().stream()
            .map(categoryMapper::toResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public CategoryResponse getCategory(UUID id) {
        // Todo extract and filter by user ID too -> dynamic queries if needed
        return categoryRepository.findById(id)
            .map(categoryMapper::toResponse)
            .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));
    }

    @Transactional
    public CategoryResponse createCategory(CategoryRequest request) {
        if (categoryRepository.existsByName(request.name())) {
            throw new AppException(
                ErrorCode.CATEGORY_EXISTED,
                String.format("Category already exists with name: %s", request.name())
            );
        }
        // Todo: check user

        Category savedCategory = categoryRepository.save(
            categoryMapper.toEntity(request)
        );

        return categoryMapper.toResponse(savedCategory);
    }

    @Transactional
    public CategoryResponse updateCategory(UUID id, CategoryRequest request) {
        Category existingCategory = categoryRepository.findById(id)
            .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));

        categoryValidator.validateForUpdate(existingCategory, request);

        categoryMapper.updateEntity(existingCategory, request);
        return categoryMapper.toResponse(categoryRepository.save(existingCategory));
    }

    @Transactional
    public void deleteCategory(UUID id) {
        Category category = categoryRepository.findById(id)
            .orElseThrow(() -> new AppException(
                ErrorCode.CATEGORY_NOT_FOUND,
                String.format("Category not found with id: %s", id)));

        categoryRepository.delete(category);
    }
}
