package com.koi151.money.fintrack.core.category;

import com.koi151.money.fintrack.common.exception.AppException;
import com.koi151.money.fintrack.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CategoryValidator {

    private CategoryRepository categoryRepository;

    public void validateForUpdate(Category existingCategory, CategoryRequest request) {
        String newCategoryName = request.name();

        // Skip DB call if name hasn't changed
        if (existingCategory.getName().equals(newCategoryName)) {
            return;
        }

        // Check duplicate name
        if (categoryRepository.existsByNameAndIdNot(newCategoryName, existingCategory.getId())) {
            throw new AppException(
                ErrorCode.CATEGORY_EXISTED,
                String.format("Category name '%s' already taken", newCategoryName)
            );
        }
    }
}
