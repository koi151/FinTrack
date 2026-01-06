package com.koi151.money.fintrack.core.category;

import com.koi151.money.fintrack.common.AppResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
@Tag(name = "Category Management", description = "APIs for managing expense/income categories")
public class CategoryController {

    private final CategoryService categoryService;

    @Operation(summary = "Create a new category", description = "Create a new transaction category. Name must be unique.")
    @ApiResponse(responseCode = "200", description = "Category created successfully")
    @ApiResponse(responseCode = "200", description = "Invalid input (e.g., missing name, invalid color code)", content = @Content)
    @ApiResponse(responseCode = "409", description = "Category name already exists", content = @Content)
    @PostMapping
    public AppResponse<CategoryResponse> createCategory(@RequestBody @Valid CategoryRequest request) {
        return AppResponse.success(categoryService.createCategory(request), "Successfully created category");
    }

    @Operation(summary = "Update an existing category", description = "Modify details of an existing category by its ID.")
    @ApiResponse(responseCode = "200", description = "Category updated successfully")
    @ApiResponse(responseCode = "400", description = "Invalid update data", content = @Content)
    @ApiResponse(responseCode = "404", description = "Category not found", content = @Content)
    @PutMapping("/{id}")
    public AppResponse<CategoryResponse> updateCategory(
        @Parameter(description = "The unique UUID of the category", example = "550e8400-e29b-41d4-a716-446655440000")
        @PathVariable UUID id, @RequestBody @Valid CategoryRequest request)
    {
        return AppResponse.success(categoryService.updateCategory(id, request), "Successfully updated category");
    }

    @Operation(summary = "Get category details", description = "Retrieve full details of a specific category.")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved category")
    @ApiResponse(responseCode = "404", description = "Category not found", content = @Content)
    @GetMapping("/{id}")
    public AppResponse<CategoryResponse> getCategory(
        @Parameter(description = "The UUID of the category to retrieve")
        @PathVariable UUID id)
    {
        return AppResponse.success(categoryService.getCategory(id), "Successfully get category");
    }

    @Operation(summary = "Delete a category", description = "Permanently remove a category from the system.")
    @ApiResponse(responseCode = "200", description = "Category deleted successfully")
    @ApiResponse(responseCode = "404", description = "Category not found", content = @Content)
    @DeleteMapping("/{id}")
    public AppResponse<CategoryResponse> deleteCategory(
        @Parameter(description = "The UUID of the category to be deleted")
        @PathVariable UUID id
    ) {
        categoryService.deleteCategory(id);
        return AppResponse.success(204, "Successfully deleted category");
    }
}