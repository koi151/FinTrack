package com.koi151.money.fintrack.core.category;

import com.koi151.money.fintrack.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @PostMapping
    public ApiResponse<CategoryResponse> createCategory(@RequestBody @Valid CategoryRequest request) {
        return ApiResponse.success(categoryService.createCategory(request), "Successfully created category");
    }

    @PutMapping("/{id}")
    public ApiResponse<CategoryResponse> updateCategory(@PathVariable UUID id, @RequestBody @Valid CategoryRequest request) {
        return ApiResponse.success(categoryService.updateCategory(id, request), "Successfully updated category");
    }

    @GetMapping("/{id}")
    public ApiResponse<CategoryResponse> getCategory(@PathVariable UUID id) {
        return ApiResponse.success(categoryService.getCategory(id), "Successfully get category");
    }

    @DeleteMapping("/{id}")
    public ApiResponse<CategoryResponse> deleteCategory(@PathVariable UUID id) {
        categoryService.deleteCategory(id);
        return ApiResponse.success(204, "Successfully deleted category");
    }
}